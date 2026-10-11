#include "mqtt_client.h"
#include "secrets.h"
#include "config.h"
#include "navigation.h"
#include "sensors.h"
#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>   // Cài qua Library Manager: "PubSubClient" by Nick O'Leary

// ============================================================
//  MQTT topic
// ============================================================
#define MQTT_TOPIC_COMMAND  "robot/command"   // Backend → Robot (subscribe)
#define MQTT_TOPIC_STATUS   "robot/status"    // Robot → Backend (publish)
#define MQTT_CLIENT_ID      "robot-hut-bui-001"
#define MQTT_STATUS_INTERVAL_MS  500          // Publish status mỗi 500ms

// ============================================================
//  Nội bộ
// ============================================================
static WiFiClient   wifiClient;
static PubSubClient mqttClient(wifiClient);
static uint32_t     lastStatusMs = 0;

// ============================================================
//  Callback: nhận lệnh từ Backend qua topic robot/command
//  Payload JSON ví dụ: {"command":"FORWARD"}
// ============================================================
static void onMqttMessage(char* topic, byte* payload, unsigned int length) {
    // Tạo chuỗi kết thúc bằng null
    char msg[length + 1];
    memcpy(msg, payload, length);
    msg[length] = '\0';

    Serial.printf("[MQTT] Nhận topic=%s payload=%s\n", topic, msg);

    // Parse lệnh đơn giản – tìm giá trị của key "command"
    // Payload: {"command":"FORWARD"}
    const char* key    = "\"command\":\"";
    const char* start  = strstr(msg, key);
    if (!start) {
        Serial.println("[MQTT] Không tìm thấy key 'command' trong payload");
        return;
    }
    start += strlen(key);
    const char* end = strchr(start, '"');
    if (!end) return;

    char cmdStr[32] = {};
    size_t cmdLen = min((size_t)(end - start), sizeof(cmdStr) - 1);
    strncpy(cmdStr, start, cmdLen);

    Serial.printf("[MQTT] Lệnh nhận được: %s\n", cmdStr);

    // Ánh xạ chuỗi → ManualCmd / action
    if      (strcmp(cmdStr, "FORWARD")  == 0) navManual(MANUAL_FORWARD);
    else if (strcmp(cmdStr, "BACKWARD") == 0) navManual(MANUAL_BACKWARD);
    else if (strcmp(cmdStr, "LEFT")     == 0) navManual(MANUAL_LEFT);
    else if (strcmp(cmdStr, "RIGHT")    == 0) navManual(MANUAL_RIGHT);
    else if (strcmp(cmdStr, "STOP")     == 0) navStop();
    else if (strcmp(cmdStr, "AUTO")     == 0) navStartAuto();
    else {
        Serial.printf("[MQTT] Lệnh không xác định: %s\n", cmdStr);
    }
}

// ============================================================
//  Kết nối / tái kết nối MQTT broker
// ============================================================
static bool mqttReconnect() {
    Serial.printf("[MQTT] Đang kết nối broker %s:%d ...\n",
                  MQTT_BROKER_HOST, MQTT_BROKER_PORT);

    bool ok;
    if (strlen(MQTT_USERNAME) > 0) {
        ok = mqttClient.connect(MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD);
    } else {
        ok = mqttClient.connect(MQTT_CLIENT_ID);
    }

    if (ok) {
        Serial.println("[MQTT] Kết nối broker thành công!");
        // Subscribe topic nhận lệnh từ Backend
        mqttClient.subscribe(MQTT_TOPIC_COMMAND, 1);
        Serial.printf("[MQTT] Subscribed: %s\n", MQTT_TOPIC_COMMAND);
    } else {
        Serial.printf("[MQTT] Kết nối thất bại, rc=%d\n", mqttClient.state());
    }
    return ok;
}

// ============================================================
//  Public API
// ============================================================
void mqttInit() {
    mqttClient.setServer(MQTT_BROKER_HOST, MQTT_BROKER_PORT);
    mqttClient.setCallback(onMqttMessage);
    mqttClient.setKeepAlive(30);
    mqttClient.setSocketTimeout(5);
    mqttReconnect();
}

void mqttUpdate() {
    if (!wifiConnected()) return;   // wifiConnected() từ wifi_manager.h

    if (!mqttClient.connected()) {
        static uint32_t lastReconnectMs = 0;
        uint32_t now = millis();
        if (now - lastReconnectMs >= 5000) {   // thử lại mỗi 5 giây
            lastReconnectMs = now;
            mqttReconnect();
        }
        return;
    }

    mqttClient.loop();   // xử lý inbound messages
}

void mqttPublishStatus() {
    if (!mqttClient.connected()) return;

    uint32_t now = millis();
    if (now - lastStatusMs < MQTT_STATUS_INTERVAL_MS) return;
    lastStatusMs = now;

    const Distances& d = sensorsGet();
    const CliffState& c = sensorsCliff();
    const char* modeName  = navModeName(navGetMode());
    const char* stateName = navStateName(navGetState());

    char payload[256];
    snprintf(payload, sizeof(payload),
        "{\"mode\":\"%s\",\"state\":\"%s\","
        "\"distLeft\":%.1f,\"distCenter\":%.1f,\"distRight\":%.1f,"
        "\"cliff\":%s,\"timestamp\":%lu}",
        modeName, stateName,
        d.left, d.center, d.right,
        c.detected ? "true" : "false",
        (unsigned long)now
    );

    bool ok = mqttClient.publish(MQTT_TOPIC_STATUS, payload, false);
    if (!ok) {
        Serial.println("[MQTT] Publish status thất bại (buffer đầy?)");
    }
}

bool mqttConnected() {
    return mqttClient.connected();
}

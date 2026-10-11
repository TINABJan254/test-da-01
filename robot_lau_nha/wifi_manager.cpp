#include "wifi_manager.h"
#include "secrets.h"
#include <Arduino.h>
#include <WiFi.h>

#define WIFI_MAX_RETRIES  20   // 20 x 500ms = 10 giây timeout

bool wifiInit() {
    Serial.printf("[WiFi] Đang kết nối đến: %s\n", WIFI_SSID);
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    for (int i = 0; i < WIFI_MAX_RETRIES; i++) {
        if (WiFi.status() == WL_CONNECTED) {
            Serial.printf("[WiFi] Kết nối thành công! IP: %s\n",
                          WiFi.localIP().toString().c_str());
            return true;
        }
        Serial.print(".");
        delay(500);
    }

    Serial.println("\n[WiFi] Kết nối THẤT BẠI!");
    return false;
}

bool wifiConnected() {
    return WiFi.status() == WL_CONNECTED;
}

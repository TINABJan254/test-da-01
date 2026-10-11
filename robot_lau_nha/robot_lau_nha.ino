// Robot hut bui / lau nha - ESP32 + L298N + 3x HC-SR04 + TCRT5000
// Giai doan 2: tich hop WiFi + MQTT de dieu khien qua Backend
//
// Bat nguon xong robot o che do IDLE (dung yen), cho chon che do.
//
// Lenh qua Serial Monitor (115200, gui tung ky tu):
//   g : chon che do AUTO (tu di, tranh vat can)
//   m : chon che do MANUAL (dung yen, cho lenh w/s/a/d)
//   x : ve IDLE, dung yen
//   w/s/a/d : (chi trong MANUAL) tien / lui / trai / phai,  space : dung banh
//   i : bat/tat in thong tin cam bien
//
// Lenh qua MQTT topic robot/command (JSON):
//   Chon che do: {"command":"AUTO"}  {"command":"MANUAL"}  {"command":"IDLE"}
//   Trong MANUAL: {"command":"FORWARD"} {"command":"BACKWARD"}
//                 {"command":"LEFT"}    {"command":"RIGHT"}   {"command":"STOP"}
//   STOP khi dang AUTO -> ve IDLE

#include <esp_system.h>
#include "config.h"
#include "types.h"
#include "motor.h"
#include "sensors.h"
#include "navigation.h"
#include "wifi_manager.h"
#include "mqtt_client.h"

static bool debugEnabled = true;
static uint32_t lastDebugMs = 0;
static bool autoStartPending = AUTO_START_ON_BOOT;

// Bo nho RTC giu nguyen qua reset (tru khi mat nguon hoan toan)
#define AUTO_MAGIC        0xA070A070
#define MAX_AUTO_RESUMES  3
RTC_NOINIT_ATTR static uint32_t rtcAutoMagic;
RTC_NOINIT_ATTR static uint32_t rtcResumeCount;

static bool isAbnormalReset(esp_reset_reason_t r) {
  return r == ESP_RST_BROWNOUT || r == ESP_RST_PANIC ||
         r == ESP_RST_INT_WDT  || r == ESP_RST_TASK_WDT || r == ESP_RST_WDT;
}

static void handleSerial() {
  while (Serial.available()) {
    char c = Serial.read();
    switch (c) {
      case 'g': case 'G': autoStartPending = false; rtcResumeCount = 0; navStartAuto(); break;
      case 'm': case 'M': autoStartPending = false; navStartManual(); break;
      case 'x': case 'X': autoStartPending = false; navStop();      break;
      case ' ':           navManual(MANUAL_STOP);     break;
      case 'w': case 'W': navManual(MANUAL_FORWARD);  break;
      case 's': case 'S': navManual(MANUAL_BACKWARD); break;
      case 'a': case 'A': navManual(MANUAL_LEFT);     break;
      case 'd': case 'D': navManual(MANUAL_RIGHT);    break;
      case 'i': case 'I': debugEnabled = !debugEnabled; break;
      default: break;
    }
  }
}

static void printDebug() {
  uint32_t now = millis();
  if (!debugEnabled || now - lastDebugMs < DEBUG_INTERVAL_MS) return;
  lastDebugMs = now;

  const Distances& d = sensorsGet();
  const CliffState& c = sensorsCliff();
  Serial.printf("L=%5.1f C=%5.1f R=%5.1f cm | Roi=%s D0=%d | %s/%s | motor L=%4d R=%4d | WiFi=%s MQTT=%s\n",
                d.left, d.center, d.right,
                c.detected ? "CO" : "--", digitalRead(PIN_CLIFF_DO),
                navModeName(navGetMode()), navStateName(navGetState()),
                motorGetLeft(), motorGetRight(),
                wifiConnected() ? "OK" : "NGAT",
                mqttConnected() ? "OK" : "NGAT");
}

void setup() {
  Serial.begin(SERIAL_BAUD);
  delay(200);
  Serial.println("\n=== Robot lau nha - khoi dong (v2 WiFi+MQTT) ===");

  esp_reset_reason_t reason = esp_reset_reason();
  Serial.printf("Ly do khoi dong: %d\n", (int)reason);
  if (reason == ESP_RST_BROWNOUT) {
    Serial.println("!!! CANH BAO: ESP32 vua bi reset do SUT AP (brownout).");
  }

  if (isAbnormalReset(reason) && rtcAutoMagic == AUTO_MAGIC) {
    if (rtcResumeCount < MAX_AUTO_RESUMES) {
      rtcResumeCount++;
      autoStartPending = true;
      Serial.printf("!!! Dang chay AUTO thi bi reset -> tu chay lai (lan %u/%d)\n",
                    (unsigned)rtcResumeCount, MAX_AUTO_RESUMES);
    } else {
      autoStartPending = false;
      Serial.println("!!! Reset lien tuc qua nhieu lan -> KHONG tu chay lai. Kiem tra nguon!");
    }
  } else {
    rtcResumeCount = 0;
  }
  rtcAutoMagic = 0;

  motorInit();
  sensorsInit();
  navInit();

  // Ket noi WiFi va MQTT (khong chan loop neu that bai - se thu lai trong mqttUpdate)
  if (wifiInit()) {
    mqttInit();
  } else {
    Serial.println("[WARN] Khong co WiFi - tiep tuc che do offline (chi dieu khien qua Serial).");
  }

  Serial.println("Robot dang IDLE (dung yen) - cho chon che do AUTO / MANUAL");
  Serial.println("Lenh Serial: g=AUTO, m=MANUAL, x=IDLE, w/s/a/d/space=tay, i=debug");
  Serial.println("Lenh MQTT:   {\"command\":\"AUTO/MANUAL/IDLE\"}, trong MANUAL: FORWARD/BACKWARD/LEFT/RIGHT/STOP");
}

void loop() {
  handleSerial();

  if (autoStartPending && millis() >= AUTO_START_DELAY_MS) {
    autoStartPending = false;
    navStartAuto();
  }

  sensorsUpdate();
  navUpdate();
  motorUpdate();

  // MQTT: xu ly message den va publish trang thai
  mqttUpdate();
  mqttPublishStatus();

  printDebug();

  rtcAutoMagic = navGetMode() == MODE_AUTO ? AUTO_MAGIC : 0;
}

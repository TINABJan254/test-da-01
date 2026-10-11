#pragma once

// ============================================================
//  CẤU HÌNH BÍ MẬT – KHÔNG đưa lên Git!
//  Đổi các giá trị bên dưới cho phù hợp với môi trường của bạn.
// ============================================================

// --- WiFi ---
#define WIFI_SSID     "Redmi_Note_11"
#define WIFI_PASSWORD "12345678"

// --- MQTT Broker (địa chỉ máy chạy Mosquitto) ---
// Nếu dùng Docker trên cùng máy với ESP32 cùng WiFi, dùng IP máy thật (không phải localhost)
#define MQTT_BROKER_HOST  "172.29.38.135"   // ← Thay bằng IP máy chủ của bạn
#define MQTT_BROKER_PORT  1883
#define MQTT_USERNAME     ""                // Để trống nếu không dùng auth
#define MQTT_PASSWORD     ""

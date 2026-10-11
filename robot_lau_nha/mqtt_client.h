#pragma once

// ============================================================
//  MQTT Client – giao tiếp với Backend qua MQTT broker
// ============================================================

/**
 * Khởi tạo MQTT client và kết nối đến broker.
 * Phải gọi sau wifiInit() thành công.
 */
void mqttInit();

/**
 * Gọi liên tục trong loop() để:
 *  - Duy trì kết nối MQTT (reconnect nếu bị mất)
 *  - Xử lý các message đến (callback)
 */
void mqttUpdate();

/**
 * Publish trạng thái robot lên topic robot/status.
 * Gọi định kỳ (ví dụ mỗi 500ms) trong loop().
 */
void mqttPublishStatus();

/** Trả về true nếu đang kết nối broker. */
bool mqttConnected();

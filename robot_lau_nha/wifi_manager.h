#pragma once

// ============================================================
//  WiFi Manager – kết nối WiFi khi khởi động
// ============================================================

/**
 * Khởi tạo và kết nối WiFi (blocking, retry tối đa WIFI_MAX_RETRIES giây).
 * Trả về true nếu kết nối thành công.
 */
bool wifiInit();

/** Trả về true nếu WiFi đang kết nối. */
bool wifiConnected();

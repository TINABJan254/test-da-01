package com.datn.robotbackend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Các lệnh điều khiển robot – phải khớp với onMqttMessage() trong robot_lau_nha (ESP32).
 * MQTT payload ví dụ: {"command":"FORWARD"}
 *
 * Robot khởi động ở IDLE (đứng yên). Chọn chế độ bằng AUTO / MANUAL / IDLE.
 * FORWARD/BACKWARD/LEFT/RIGHT chỉ có tác dụng khi robot đang ở MANUAL.
 * STOP: đang MANUAL → dừng bánh (vẫn ở MANUAL); đang AUTO → về IDLE.
 */
public enum RobotCommand {
    // --- Chọn chế độ ---
    AUTO,       // Chế độ tự động
    MANUAL,     // Chế độ điều khiển tay (đứng yên chờ lệnh)
    IDLE,       // Đứng yên

    // --- Điều khiển tay (chỉ trong MANUAL) ---
    FORWARD,    // Tiến
    BACKWARD,   // Lùi
    LEFT,       // Quay trái
    RIGHT,      // Quay phải
    STOP,       // Dừng
    ;

    /** Lệnh di chuyển – yêu cầu robot đang ở chế độ MANUAL. */
    public boolean isMovement() {
        return this == FORWARD || this == BACKWARD || this == LEFT || this == RIGHT;
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static RobotCommand fromJson(String value) {
        return RobotCommand.valueOf(value.toUpperCase());
    }
}

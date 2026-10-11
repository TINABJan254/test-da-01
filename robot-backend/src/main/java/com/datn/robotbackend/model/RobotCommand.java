package com.datn.robotbackend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Các lệnh điều khiển robot – phải khớp với switch-case trong robot_hut_bui (ESP32).
 * MQTT payload ví dụ: {"command":"FORWARD"}
 */
public enum RobotCommand {
    FORWARD,    // Tiến
    BACKWARD,   // Lùi
    LEFT,       // Quay trái
    RIGHT,      // Quay phải
    STOP,       // Dừng
    AUTO,       // Chế độ tự động
    ;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static RobotCommand fromJson(String value) {
        return RobotCommand.valueOf(value.toUpperCase());
    }
}

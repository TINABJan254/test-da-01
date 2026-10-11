package com.datn.robotbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Trạng thái robot nhận từ MQTT topic robot/status.
 * Robot publish JSON: {"mode":"MANUAL","state":"FORWARD","distL":30.5,"distC":50.0,"distR":28.1,"cliff":false}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotStatus {
    private String mode;        // IDLE | AUTO | MANUAL
    private String state;       // STOPPED | FORWARD | BACKWARD | TURN | ...
    private float distLeft;
    private float distCenter;
    private float distRight;
    private boolean cliff;       // cảnh báo nguy cơ rơi
    private long timestamp;      // millis từ robot
}

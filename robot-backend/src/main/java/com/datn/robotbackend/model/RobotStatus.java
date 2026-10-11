package com.datn.robotbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Trạng thái robot nhận từ MQTT topic robot/status.
 * Robot publish JSON: {"mode":"MANUAL","state":"STOPPED","manualCmd":"FORWARD",
 *                      "distLeft":30.5,"distCenter":50.0,"distRight":28.1,"cliff":false,"timestamp":12345}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotStatus {
    private String mode;        // IDLE | AUTO | MANUAL
    private String state;       // trạng thái AUTO: STOPPED | FORWARD | BRAKE | BACKUP | TURN | ESCAPE
    private String manualCmd;   // lệnh tay hiện tại (MANUAL): STOP | FORWARD | BACKWARD | LEFT | RIGHT
    private float distLeft;
    private float distCenter;
    private float distRight;
    private boolean cliff;       // cảnh báo nguy cơ rơi
    private long timestamp;      // millis từ robot
}

package com.datn.robotbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body nhận từ Mobile/Web App:
 * POST /api/robot/command
 * {
 *   "command": "FORWARD"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandRequest {
    private RobotCommand command;
}

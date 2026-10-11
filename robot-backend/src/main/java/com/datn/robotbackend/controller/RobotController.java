package com.datn.robotbackend.controller;

import com.datn.robotbackend.model.CommandRequest;
import com.datn.robotbackend.model.RobotStatus;
import com.datn.robotbackend.service.RobotCommandService;
import com.datn.robotbackend.service.RobotStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST API để điều khiển robot.
 *
 * POST /api/robot/command       – gửi lệnh
 *      chọn chế độ:             AUTO / MANUAL / IDLE
 *      điều khiển tay (MANUAL): FORWARD / BACKWARD / LEFT / RIGHT / STOP
 * GET  /api/robot/status        – lấy trạng thái mới nhất
 * GET  /api/robot/health        – kiểm tra backend còn sống không
 */
@Slf4j
@RestController
@RequestMapping("/api/robot")
@RequiredArgsConstructor
public class RobotController {

    private final RobotCommandService commandService;
    private final RobotStatusService  statusService;

    /**
     * Gửi lệnh điều khiển tới robot qua MQTT.
     * Body: { "command": "FORWARD" }
     */
    @PostMapping("/command")
    public ResponseEntity<Map<String, Object>> sendCommand(@RequestBody CommandRequest request) {
        if (request.getCommand() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Thiếu trường command"));
        }

        log.info("[API] Receive command request: {}", request.getCommand());

        // Lệnh di chuyển chỉ hợp lệ khi robot đang MANUAL (robot cũng tự bỏ qua nếu không phải)
        if (request.getCommand().isMovement()) {
            String mode = statusService.getCurrentStatus().map(RobotStatus::getMode).orElse(null);
            if (mode != null && !"MANUAL".equals(mode)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "error",   "Robot đang ở chế độ " + mode
                                 + ", gửi {\"command\":\"MANUAL\"} trước khi điều khiển tay",
                        "mode",    mode
                ));
            }
        }

        commandService.sendCommand(request.getCommand());

        return ResponseEntity.ok(Map.of(
                "success",   true,
                "command",   request.getCommand().name(),
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Lấy trạng thái mới nhất của robot.
     * Robot phải subscribe topic robot/status và publish status định kỳ.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return statusService.getCurrentStatus()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of(
                        "message", "Chưa nhận được trạng thái từ robot",
                        "timestamp", Instant.now().toString()
                )));
    }

    /** Health check đơn giản. */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status",    "UP",
                "timestamp", Instant.now().toString()
        ));
    }
}

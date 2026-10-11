package com.datn.robotbackend.service;

import com.datn.robotbackend.model.RobotStatus;
import tools.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service lắng nghe trạng thái robot từ MQTT topic robot/status.
 * Robot publish JSON status, backend lưu lại và cung cấp cho REST API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotStatusService {

    private final ObjectMapper objectMapper;

    /** Trạng thái mới nhất nhận được từ robot (thread-safe). */
    @Getter
    private final AtomicReference<RobotStatus> latestStatus = new AtomicReference<>();

    /**
     * Được gọi tự động khi có message đến mqttInboundChannel.
     */
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleStatusMessage(Message<String> message) {
        String payload = message.getPayload();
        log.debug("[MQTT] Received status payload: {}", payload);

        try {
            RobotStatus status = objectMapper.readValue(payload, RobotStatus.class);
            RobotStatus prev = latestStatus.getAndSet(status);

            // Robot gửi status mỗi 500ms -> chỉ log khi chế độ/trạng thái thay đổi
            boolean changed = prev == null
                    || !Objects.equals(prev.getMode(), status.getMode())
                    || !Objects.equals(prev.getState(), status.getState())
                    || !Objects.equals(prev.getManualCmd(), status.getManualCmd())
                    || prev.isCliff() != status.isCliff();
            if (changed) log.info("[MQTT] Robot status updated → mode={}, state={}, manualCmd={}, distC={}cm, cliff={}",
                    status.getMode(), status.getState(), status.getManualCmd(),
                    status.getDistCenter(), status.isCliff());

        } catch (Exception e) {
            log.warn("[MQTT] Cannot parse robot status: {}", payload, e);
        }
    }

    /** Trả về trạng thái hiện tại của robot (null nếu chưa nhận được). */
    public Optional<RobotStatus> getCurrentStatus() {
        return Optional.ofNullable(latestStatus.get());
    }
}

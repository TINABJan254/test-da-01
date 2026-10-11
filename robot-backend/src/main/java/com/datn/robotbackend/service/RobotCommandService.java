package com.datn.robotbackend.service;

import com.datn.robotbackend.config.MqttProperties;
import com.datn.robotbackend.model.RobotCommand;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service publish lệnh điều khiển đến robot qua MQTT.
 * Payload dạng JSON: {"command":"FORWARD"}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotCommandService {

    private final MessageChannel mqttOutboundChannel;
    private final MqttProperties mqttProperties;
    private final ObjectMapper objectMapper;

    /**
     * Gửi lệnh điều khiển đến robot qua MQTT topic robot/command.
     *
     * @param command lệnh cần gửi
     */
    public void sendCommand(RobotCommand command) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of("command", command.name())
            );

            mqttOutboundChannel.send(
                    MessageBuilder.withPayload(payload)
                            .setHeader("mqtt_topic", mqttProperties.getTopic().getCommand())
                            .setHeader("mqtt_qos", mqttProperties.getQos())
                            .build()
            );

            log.info("[MQTT] Sent command: {} → topic: {}", command, mqttProperties.getTopic().getCommand());

        } catch (Exception e) {
            log.error("[MQTT] Failed to send command: {}", command, e);
            throw new RuntimeException("Không thể gửi lệnh đến robot: " + e.getMessage(), e);
        }
    }
}

package com.datn.robotbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "robot.mqtt")
public class MqttProperties {

    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "robot-backend-001";
    private Topic topic = new Topic();
    private int qos = 1;
    private String username = "";
    private String password = "";

    @Data
    public static class Topic {
        private String command = "robot/command";
        private String status  = "robot/status";
    }
}

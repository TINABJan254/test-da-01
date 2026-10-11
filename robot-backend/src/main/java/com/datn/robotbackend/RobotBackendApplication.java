package com.datn.robotbackend;

import com.datn.robotbackend.config.MqttProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MqttProperties.class)
public class RobotBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RobotBackendApplication.class, args);
	}

}

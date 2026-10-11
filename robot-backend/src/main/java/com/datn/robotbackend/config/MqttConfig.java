package com.datn.robotbackend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    private final MqttProperties props;

    public MqttConfig(MqttProperties props) {
        this.props = props;
    }

    // ----------------------------------------------------------------
    //  Shared client factory (dùng cho cả publisher và subscriber)
    // ----------------------------------------------------------------
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{props.getBrokerUrl()});
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        if (!props.getUsername().isBlank()) {
            options.setUserName(props.getUsername());
            options.setPassword(props.getPassword().toCharArray());
        }

        factory.setConnectionOptions(options);
        return factory;
    }

    // ----------------------------------------------------------------
    //  OUTBOUND – Backend → MQTT broker (publish lệnh cho robot)
    // ----------------------------------------------------------------
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundHandler() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                props.getClientId() + "-pub",
                mqttClientFactory()
        );
        handler.setAsync(true);
        handler.setDefaultTopic(props.getTopic().getCommand());
        handler.setDefaultQos(props.getQos());
        return handler;
    }

    // ----------------------------------------------------------------
    //  INBOUND – MQTT broker → Backend (nhận trạng thái từ robot)
    // ----------------------------------------------------------------
    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        props.getClientId() + "-sub",
                        mqttClientFactory(),
                        props.getTopic().getStatus()
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(props.getQos());
        adapter.setOutputChannel(mqttInboundChannel());
        return adapter;
    }
}

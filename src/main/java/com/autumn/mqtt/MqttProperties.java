package com.autumn.mqtt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 读取 application.yml 中 emqx 前缀的配置。
@Data
@Component
@ConfigurationProperties(prefix = "emqx")
public class MqttProperties {

    // MQTT Broker 地址，例如 tcp://127.0.0.1:1883。
    private String broker = "tcp://127.0.0.1:1883";

    // MQTT 用户名，可为空。
    private String username;

    // MQTT 密码，可为空。
    private String password;

    // MQTT 5 连接时是否使用 clean start。
    private boolean cleanStart = true;

    // Spring Boot 启动时是否自动建立 MQTT 连接。
    private boolean autoStartup = true;

    // 连接断开后是否自动重连。
    private boolean automaticReconnect = true;

    // 连接超时时间，单位秒。
    private int connectionTimeout = 10;

    // 心跳保活时间，单位秒。
    private int keepAliveInterval = 20;

    // 共享连接使用的客户端 ID，发布和订阅共用这一份。
    private String clientId = "demo-client";

    // 发布端独立配置。
    private Publisher publisher = new Publisher();

    // 订阅端独立配置。
    private Subscriber subscriber = new Subscriber();

    // 发布端配置对象。
    @Data
    public static class Publisher {

        // 默认发布主题。
        private String topic = "topic/test";

        // 默认发布 QoS。
        private int qos = 1;
    }

    // 订阅端配置对象。
    @Data
    public static class Subscriber {

        // 默认订阅主题。
        private String topic = "topic/test";

        // 默认订阅 QoS。
        private int qos = 1;
    }

}

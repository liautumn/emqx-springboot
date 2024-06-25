package com.autumn.mqtt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "autumn.emqx")
public class MqttProperties {

    private String broker;

    private String username;

    private String password;

    private Integer qos;

    private Boolean cleanSession;

    private String clientId;

    private String subTopic;

    private Boolean reconnect;

    private Integer reconnectTime;

}

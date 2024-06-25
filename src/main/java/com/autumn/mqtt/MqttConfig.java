package com.autumn.mqtt;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqttConfig {

    @Resource
    private MqttProperties mqttProperties;
    @Resource
    private OnMessageCallback messageCallback;

    private static MqttClient client = null;

    @Bean
    protected void Init() {
        Boolean connect = connect();
        log.info("Connect mqtt server {}", connect);
    }

    public MqttClient getMqttClient() {
        return client;
    }

    public void destroy() {
        try {
            if (client != null) {
                client.disconnect();
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean connect() {
        Boolean flag = false;
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(mqttProperties.getBroker(), mqttProperties.getClientId(), persistence);
            // MQTT 连接选项
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(mqttProperties.getUsername());
            connOpts.setPassword(mqttProperties.getPassword().toCharArray());
            // 保留会话
            connOpts.setCleanSession(mqttProperties.getCleanSession());
            // 设置回调
            client.setCallback(messageCallback);
            // 建立连接
            client.connect(connOpts);
            // 订阅
            client.subscribe(mqttProperties.getSubTopic());
            flag = true;
        } catch (MqttException me) {
            destroy();
            log.error("============================= MQTT ERROR MSG =============================");
            log.error("reason {}", me.getReasonCode());
            log.error("msg {}", me.getMessage());
            log.error("loc {}", me.getLocalizedMessage());
            log.error("cause {}", me.getCause());
            log.error("excep {}", me);
            log.error("============================= MQTT ERROR END =============================");
            me.printStackTrace();
        }
        return flag;
    }

}

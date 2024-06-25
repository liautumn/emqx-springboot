package com.autumn.mqtt;

import jakarta.annotation.Resource;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Service
public class MqttPublish {

    @Resource
    private MqttConfig mqttConfig;
    @Resource
    private MqttProperties mqttProperties;

    public void publish(String pubTopic, String message) {
        try {
            MqttClient client = mqttConfig.getMqttClient();
            // 消息发布所需参数
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(mqttProperties.getQos());
            client.publish(pubTopic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}

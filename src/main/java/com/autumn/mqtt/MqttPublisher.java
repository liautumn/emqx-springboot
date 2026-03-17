package com.autumn.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

// 发布器服务，专门负责对外发布 MQTT 消息。
@Slf4j
@Service
@RequiredArgsConstructor
public class MqttPublisher {

    // 注入 MQTT 配置。
    private final MqttProperties mqttProperties;
    // 注入共享连接管理器，发布和订阅共用这一条连接。
    private final MqttConnectionManager mqttConnectionManager;

    // 使用默认主题和默认 QoS 发布消息。
    public void publish(String payload) {
        publish(mqttProperties.getPublisher().getTopic(), payload, mqttProperties.getPublisher().getQos());
    }

    // 使用指定主题和默认 QoS 发布消息。
    public void publish(String topic, String payload) {
        publish(topic, payload, mqttProperties.getPublisher().getQos());
    }

    // 使用指定主题、消息体和 QoS 发布消息。
    public synchronized void publish(String topic, String payload, int qos) {
        try {
            // 将字符串消息体转成 MQTT 消息对象。
            MqttMessage mqttMessage = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            // 设置消息 QoS。
            mqttMessage.setQos(qos);
            // 通过共享连接发送消息，确保发布和订阅使用同一个客户端。
            mqttConnectionManager.getClient().publish(topic, mqttMessage);
            // 打印发布成功日志。
            log.info("MQTT published, topic={}, qos={}, payload={}", topic, qos, payload);
        } catch (MqttException e) {
            // 抛出运行时异常，交给上层接口或业务处理。
            throw new IllegalStateException("Failed to publish MQTT message", e);
        }
    }
}

package com.autumn.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

// 订阅器组件，只负责订阅动作和消息处理，连接生命周期交给共享连接管理器统一维护。
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttSubscriber {

    // 注入 MQTT 配置。
    private final MqttProperties mqttProperties;

    // 在共享连接建立后执行订阅动作，首次连接和重连都复用这个方法。
    public synchronized void subscribe(MqttClient mqttClient) {
        // 如果共享客户端为空或尚未连接，则本次订阅直接跳过。
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("Skip MQTT subscribe because subscriber client is not connected");
            return;
        }

        try {
            // 根据配置中的主题和 QoS 发起订阅。
            mqttClient.subscribe(
                    mqttProperties.getSubscriber().getTopic(),
                    mqttProperties.getSubscriber().getQos()
            );
            // 订阅成功后打印日志。
            log.info(
                    "MQTT subscribed, topic={}, qos={}",
                    mqttProperties.getSubscriber().getTopic(),
                    mqttProperties.getSubscriber().getQos()
            );
        } catch (MqttException e) {
            // 订阅失败时直接抛错，让上层决定是否终止启动流程。
            throw new IllegalStateException("Failed to subscribe MQTT topic", e);
        }
    }

    // 收到 MQTT 消息后由共享连接管理器回调到这里处理。
    public void handleMessage(String topic, MqttMessage message) {
        // 打印主题、QoS 和消息内容，后续可以在这里接入业务处理逻辑。
        log.info(
                "MQTT message arrived, topic={}, qos={}, payload={}",
                topic,
                message.getQos(),
                new String(message.getPayload(), StandardCharsets.UTF_8)
        );
    }
}

package com.autumn.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.stereotype.Component;

// 共享连接管理器，整个项目只维护一个 MQTT 客户端，发布和订阅都复用这一条连接。
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttConnectionManager {

    // 注入 MQTT 配置。
    private final MqttProperties mqttProperties;
    // 注入客户端工厂。
    private final MqttClientFactory mqttClientFactory;
    // 注入订阅器，用于建立订阅和处理消息。
    private final MqttSubscriber mqttSubscriber;

    // 当前共享的 MQTT 客户端实例。
    private MqttClient mqttClient;

    // Spring Boot 启动完成后自动执行，按配置决定是否立即建立连接。
    @PostConstruct
    public void start() {
        // 如果显式关闭自动启动，则跳过启动期连接。
        if (!mqttProperties.isAutoStartup()) {
            log.info("MQTT auto startup is disabled");
            return;
        }
        // 启动阶段直接建立共享连接，并完成订阅。
        connectIfNecessary();
    }

    // 获取共享客户端，调用方发布消息时统一走这里。
    public synchronized MqttClient getClient() {
        // 如果客户端尚未连接，则先补连接。
        connectIfNecessary();
        // 返回共享客户端实例。
        return mqttClient;
    }

    // 确保共享连接已建立。
    public synchronized void connectIfNecessary() {
        try {
            // 首次使用时先创建客户端并注册统一回调。
            if (mqttClient == null) {
                initClient();
            }
            // 只有未连接时才真正发起连接。
            if (!mqttClient.isConnected()) {
                mqttClient.connect(mqttClientFactory.createConnectOptions());
                // 首次连接成功后立刻订阅主题。
                mqttSubscriber.subscribe(mqttClient);
                log.info("MQTT shared connection established, clientId={}", mqttProperties.getClientId());
            }
        } catch (MqttException e) {
            // 连接失败时抛出运行时异常，让启动或调用方感知失败。
            log.error("Failed to connect shared MQTT client", e);
            throw new IllegalStateException("Failed to connect shared MQTT client", e);
        }
    }

    // 初始化共享客户端并注册统一回调。
    private void initClient() throws MqttException {
        // 根据共享 clientId 创建唯一客户端。
        mqttClient = mqttClientFactory.createClient(mqttProperties.getClientId());
        // 统一注册回调，发布和订阅相关事件都在同一个连接上处理。
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // 连接建立或自动重连成功时记录日志。
                log.info("MQTT connected, reconnect={}, serverURI={}", reconnect, serverURI);
                // 自动重连成功后需要重新订阅主题。
                if (reconnect) {
                    try {
                        mqttSubscriber.subscribe(mqttClient);
                    } catch (IllegalStateException e) {
                        log.error("Failed to resubscribe after reconnect", e);
                    }
                }
            }

            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                // 连接断开时记录断开原因。
                log.warn("MQTT disconnected: {}", disconnectResponse == null ? "已断开连接" : disconnectResponse.getException().getMessage());
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                // 底层 MQTT 异常统一在这里记录。
                log.error("MQTT error occurred", exception);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // 收到消息后交给订阅器处理业务逻辑。
                mqttSubscriber.handleMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                // 发布消息投递完成后打印日志。
                log.info("MQTT delivery complete: {}", token != null && token.isComplete());
            }

            @Override
            public void authPacketArrived(int reasonCode, org.eclipse.paho.mqttv5.common.packet.MqttProperties properties) {
                // 收到认证包时只做日志记录。
                log.debug("MQTT auth packet arrived, reasonCode={}", reasonCode);
            }
        });
    }

    // Spring 容器销毁时关闭共享连接。
    @PreDestroy
    public void destroy() {
        // 统一关闭客户端资源。
        closeClient();
    }

    // 关闭共享客户端连接。
    private void closeClient() {
        // 客户端为空时无需处理。
        if (mqttClient == null) {
            return;
        }

        try {
            // 如果连接仍然存在，先断开连接。
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            // 关闭客户端释放资源。
            mqttClient.close();
        } catch (MqttException e) {
            // 关闭失败时记录警告日志。
            log.warn("Failed to close shared MQTT client", e);
        }
    }
}

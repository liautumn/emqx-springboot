package com.autumn.mqtt;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

// 统一负责创建 MQTT 客户端和连接参数，避免发布端和订阅端重复写连接逻辑。
@Component
@RequiredArgsConstructor
public class MqttClientFactory {

    // 注入项目中的 MQTT 配置。
    private final MqttProperties mqttProperties;

    // 根据传入的 clientId 创建一个 MQTT v5 客户端。
    public MqttClient createClient(String clientId) throws MqttException {
        // 使用内存持久化，适合当前这个轻量示例项目。
        return new MqttClient(mqttProperties.getBroker(), clientId, new MemoryPersistence());
    }

    // 生成通用连接参数，发布端和订阅端共用这一份配置。
    public MqttConnectionOptions createConnectOptions() {
        // 创建 MQTT 5 的连接参数对象。
        MqttConnectionOptions options = new MqttConnectionOptions();
        // 设置 clean start。
        options.setCleanStart(mqttProperties.isCleanStart());
        // 设置是否自动重连。
        options.setAutomaticReconnect(mqttProperties.isAutomaticReconnect());
        // 设置连接超时时间。
        options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
        // 设置心跳保活间隔。
        options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());

        // 如果配置了用户名，则写入连接参数。
        if (StringUtils.hasText(mqttProperties.getUsername())) {
            options.setUserName(mqttProperties.getUsername());
        }
        // 如果配置了密码，则转成 UTF-8 字节数组后写入连接参数。
        if (StringUtils.hasText(mqttProperties.getPassword())) {
            options.setPassword(mqttProperties.getPassword().getBytes(StandardCharsets.UTF_8));
        }
        // 返回组装完成的连接参数。
        return options;
    }
}

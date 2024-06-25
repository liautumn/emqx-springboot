package com.autumn.mqtt;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OnMessageCallback implements MqttCallback {

    @Resource
    private MqttConfig mqttConfig;
    @Resource
    private MqttProperties mqttProperties;

    @Override
    public void connectionLost(Throwable cause) {
        // 连接丢失后，一般在这里面进行重连
        log.error("mqtt 连接断开");
        if (mqttProperties.getReconnect()) {
            try {
                Boolean flag = true;
                while (flag) {
                    log.error("mqtt 连接断开，正在重连");
                    Boolean connect = mqttConfig.connect();
                    if (connect) {
                        log.info("mqtt 重连成功");
                        flag = false;
                    } else {
                        mqttConfig.destroy();
                        log.error("mqtt 重连失败");
                    }
                    Thread.sleep(mqttProperties.getReconnectTime());
                }
            } catch (Exception e) {
                mqttConfig.destroy();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // subscribe后得到的消息会执行到这里面
        System.out.println("接收消息主题:" + topic);
        System.out.println("接收消息Qos:" + message.getQos());
        System.out.println("接收消息内容:" + new String(message.getPayload()));
        System.out.println("接收消息内容:" + JSON.toJSONString(message));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}

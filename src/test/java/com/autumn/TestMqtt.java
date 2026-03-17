package com.autumn;

import com.autumn.mqtt.MqttPublisher;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 基础测试类，只校验 Spring 容器能够正常装配发布器 Bean。
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "emqx.autoStartup=false"
)
public class TestMqtt {

    // 注入发布服务。
    @Resource
    private MqttPublisher mqttPublisher;

    // 验证发布器 Bean 已成功创建。
    @Test
    public void contextLoads() {
        Assertions.assertNotNull(mqttPublisher);
    }

}

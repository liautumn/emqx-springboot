package com.autumn;

import com.autumn.mqtt.MqttPublish;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMqtt {

    @Resource
    private MqttPublish mqttPublish;

    @Test
    public void pubMsg() {
        mqttPublish.publish("testtopic/1", "Hello World");
    }

}

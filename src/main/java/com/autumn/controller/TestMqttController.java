package com.autumn.controller;


import com.autumn.mqtt.MqttPublish;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMqttController {

    @Resource
    private MqttPublish mqttPublish;

    @GetMapping("/test")
    public String send() {

        mqttPublish.publish("aiqc/1", "Hello World");

        return "ok";
    }

}

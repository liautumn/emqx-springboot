package com.autumn.controller;

import com.autumn.mqtt.MqttPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// 对外暴露 HTTP 接口，方便通过 Spring Boot 接口触发 MQTT 发布。
@RestController
@RequiredArgsConstructor
@RequestMapping("/mqtt")
public class MqttPublishController {

    // 注入发布服务。
    private final MqttPublisher mqttPublisher;

    // 通过 GET 请求发布 MQTT 消息。
    @GetMapping("/publish")
    public String publish() {
        mqttPublisher.publish("hello Q");
        return "success";
    }

}

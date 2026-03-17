package com.autumn.controller;

import com.autumn.mqtt.MqttPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// 对外暴露 HTTP 接口，方便通过 Spring Boot 接口触发 MQTT 发布。
@RestController
@RequiredArgsConstructor
@RequestMapping("/mqtt")
public class MqttPublishController {

    // 注入发布服务。
    private final MqttPublisher mqttPublisher;

    // 通过 POST 请求发布 MQTT 消息。
    @PostMapping("/publish")
    public String publish(@RequestBody PublishRequest request) {
        // 请求体为空或者 payload 为空时，直接返回 400。
        if (request == null || !StringUtils.hasText(request.payload())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "payload must not be blank");
        }

        // 如果同时传了 topic 和 qos，则按请求值发布。
        if (request.qos() != null && StringUtils.hasText(request.topic())) {
            mqttPublisher.publish(request.topic(), request.payload(), request.qos());
        // 如果只传了 topic，则使用默认 qos。
        } else if (StringUtils.hasText(request.topic())) {
            mqttPublisher.publish(request.topic(), request.payload());
        // 如果 topic 都没传，则使用配置中的默认 topic 和 qos。
        } else {
            mqttPublisher.publish(request.payload());
        }
        // 返回简单结果，表示消息已提交发布。
        return "published";
    }

    // 发布接口请求体。
    public record PublishRequest(String topic, String payload, Integer qos) {
    }
}

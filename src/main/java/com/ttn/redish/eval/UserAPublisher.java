package com.ttn.redish.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserAPublisher {

    private final StringRedisTemplate stringRedisTemplate;


    public UserAPublisher(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void publish(String message) {
        ObjectMapper objectMapper = new ObjectMapper();

        stringRedisTemplate.convertAndSend(PubSubChannels.USER_MESSAGES, message);
    }
}

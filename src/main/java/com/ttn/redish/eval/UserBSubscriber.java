package com.ttn.redish.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserBSubscriber {

    private static final Logger log = LoggerFactory.getLogger(UserBSubscriber.class);

    public void onMessage(String message) {
        log.info("User B received from channel '{}': {}", PubSubChannels.USER_MESSAGES, message);
    }
}

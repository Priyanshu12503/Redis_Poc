package com.ttn.redish.eval;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EvalPubSubRunner implements ApplicationRunner {

    private final UserAPublisher userAPublisher;

    public EvalPubSubRunner(UserAPublisher userAPublisher) {
        this.userAPublisher = userAPublisher;
    }

    @Override
    public void run(ApplicationArguments args) {
        userAPublisher.publish("User A publishes message to channel");
    }
}

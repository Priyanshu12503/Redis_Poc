package com.ttn.redish.eval;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EvalPubSubController {

    private final UserAPublisher userAPublisher;

    public EvalPubSubController(UserAPublisher userAPublisher) {
        this.userAPublisher = userAPublisher;
    }

    @PostMapping("/eval/pubsub/publish")
    public String publish(@RequestParam(defaultValue = "Hello from User A") String message) {
        userAPublisher.publish(message);
        return "Published: " + message;
    }
}

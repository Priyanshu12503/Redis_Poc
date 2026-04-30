package com.ttn.redish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RedishApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedishApplication.class, args);
    }

}

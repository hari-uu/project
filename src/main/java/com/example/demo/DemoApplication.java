package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        log.info("Starting Spring Boot application");
        SpringApplication.run(DemoApplication.class, args);
        log.info("Application started successfully");
    }

    @GetMapping("/hello")
    public String hello() {
        log.info("/hello endpoint called");
        return "hello";
    }
}

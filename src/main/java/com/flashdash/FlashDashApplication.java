package com.flashdash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FlashDashApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashDashApplication.class, args);
    }

}

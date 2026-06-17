package com.river.sand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SandMiningPermitApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandMiningPermitApplication.class, args);
    }
}

package com.softdreams.activityhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ActivityHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivityHubApplication.class, args);
    }
}

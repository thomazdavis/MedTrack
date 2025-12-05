package org.ooad.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application class.
 * We must add @EnableScheduling to allow the ReminderSystem's @Scheduled method to run.
 */
@SpringBootApplication
@EnableScheduling // REQUIRED for the ReminderSystem's scheduling to work
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}

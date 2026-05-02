package com.asheef.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    private static final Logger log = LoggerFactory.getLogger(AsyncService.class);

    @Async
    public void sendWelcomeEmail(String email) {
        try {
            log.info("Sending email to {}", email);

            Thread.sleep(3000); // simulate delay

            log.info("Email sent to {}", email);
        } catch (Exception e) {
            log.error("Error sending email", e);
        }
    }
}

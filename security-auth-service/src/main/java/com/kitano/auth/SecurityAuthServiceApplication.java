package com.kitano.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

public class SecurityAuthServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuthServiceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Security Auth Service");
        SpringApplication.run(SecurityAuthServiceApplication.class, args);
    }

}

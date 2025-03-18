package ktx.kitano.audit.service;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuditLoggingServiceApplication {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AuditLoggingServiceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting audit logging service");
        SpringApplication.run(AuditLoggingServiceApplication.class, args);
    }
}
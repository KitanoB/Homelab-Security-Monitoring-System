package ktx.kitano.audit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class AuditLoggingServiceApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuditLoggingServiceApplication.class);
    public static void main(String[] args) {
        LOGGER.info("Starting Audit Logging Service");
        SpringApplication.run(AuditLoggingServiceApplication.class, args);
    }
}
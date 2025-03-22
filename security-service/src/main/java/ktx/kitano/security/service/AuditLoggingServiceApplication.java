package ktx.kitano.security.service;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@EnableKafka
@SpringBootApplication(scanBasePackages = {
        "ktx.kitano.security.service",
        "com.kitano.core"
})
@EntityScan(basePackages = "com.kitano.core.model")
@EnableJpaRepositories(basePackages = "ktx.kitano.security.service.infrastructure.repository")
public class AuditLoggingServiceApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuditLoggingServiceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Audit Logging Service");
        SpringApplication.run(AuditLoggingServiceApplication.class, args);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        emf.setPackagesToScan("com.kitano.core.model", "ktx.kitano.security.service.domain");
        return emf;
    }
}
package ktx.kitano.security.service;

import ktx.kitano.security.service.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@EnableKafka
@SpringBootApplication(scanBasePackages = {"ktx.kitano.security.service", "com.kitano.core"})
@EntityScan(basePackages = "com.kitano.core.model")
@EnableJpaRepositories(basePackages = "ktx.kitano.security.service.infrastructure.repository")
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityServiceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Security Service");
        SpringApplication.run(SecurityServiceApplication.class, args);
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
package com.kitano.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@SpringBootApplication(scanBasePackages = {"com.kitano.auth", "com.kitano.core"})
@EntityScan(basePackages = "com.kitano.core.model")
@EnableJpaRepositories(basePackages = "com.kitano.auth.infrastructure.repository")
public class AuthServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Security Auth Service");
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(jpaVendorAdapter);
        emf.setPackagesToScan("com.kitano.core.model", "com.kitano.auth.model");
        return emf;
    }

}

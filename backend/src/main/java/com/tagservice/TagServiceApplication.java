package com.tagservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing
public class TagServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TagServiceApplication.class, args);
    }

    /**
     * Simple AuditorAware implementation for JPA auditing.
     * In a real application, this would pull the current user from the security context.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}


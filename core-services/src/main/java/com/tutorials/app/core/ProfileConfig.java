package com.tutorials.app.core;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ProfileConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
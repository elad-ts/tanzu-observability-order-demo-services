package com.tutorials.app.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TutorialsApplicationCoreServices {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TutorialsApplicationCoreServices.class, args);
    }
}

@Configuration
class ActuatorWebServerConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> actuatorWebServerFactoryCustomizer() {
        return factory -> factory.setContextPath("");
    }
}

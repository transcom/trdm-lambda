package com.milmove.trdmlambda.milmove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Config {
    @Bean
    WebClient webClient() {
        return WebClient.create();
    }
    
}

package com.milmove.trdmlambda.milmove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI api(){
        return new OpenAPI();
    }
    
}

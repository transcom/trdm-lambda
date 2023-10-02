package com.milmove.trdmlambda.milmove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "keystore")
@Configuration
@Data
public class KeyStoreConfig {

    private String keyStorePassword;

}

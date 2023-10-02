package com.milmove.trdmlambda.milmove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "keystore")
@Configuration
@EnableConfigurationProperties
@Data
public class KeyStoreProps {

    private String password;

}

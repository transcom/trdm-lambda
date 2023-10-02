package com.milmove.trdmlambda.milmove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "trdm")
@Configuration
@Data
public class TrdmProps {
   private String propsPath;
   private String encryptionUsername;
}

package com.milmove.trdmlambda.milmove.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@ConfigurationProperties(prefix = "apache")
@Configuration
@Data
public class ApacheProps {
    private String provider;

    @Value("${apache.keystore.type}")
    private String type;

    @Value("${apache.keystore.password}")
    private String password;

    @Value("${apache.keystore.alias}")
    private String alias;

    @Value("${apache.keystore.file}")
    private String keystoreFile;

    private String cryptoProvider = "org.apache.ws.security.crypto.provider";
    private String merlinKeystoreType = "org.apache.ws.security.crypto.merlin.keystore.type";
    private String merlinKeystorePassword = "org.apache.ws.security.crypto.merlin.keystore.password";
    private String merlinKeystoreAlias = "org.apache.ws.security.crypto.merlin.keystore.alias";
    private String merlinKeystoreFile = "org.apache.ws.security.crypto.merlin.keystore.file";

}

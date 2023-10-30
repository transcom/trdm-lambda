package com.milmove.trdmlambda.milmove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.milmove.trdmlambda.milmove.util.SecretFetcher;

import lombok.Data;

@ConfigurationProperties(prefix = "apache")
@Configuration
@Data
public class ApacheProps {
    private String provider = "org.apache.ws.security.components.crypto.Merlin";

    private String type;
    private String password;
    private String alias;
    private String keystoreFile;

    private String cryptoProvider = "org.apache.ws.security.crypto.provider";
    private String merlinKeystoreType = "org.apache.ws.security.crypto.merlin.keystore.type";
    private String merlinKeystorePassword = "org.apache.ws.security.crypto.merlin.keystore.password";
    private String merlinKeystoreAlias = "org.apache.ws.security.crypto.merlin.keystore.alias";
    private String merlinKeystoreFile = "org.apache.ws.security.crypto.merlin.keystore.file";

    public ApacheProps(SecretFetcher secretFetcher) {
        this.type = secretFetcher.getSecret("trdm_lambda_milmove_keypair_type");
        this.password = secretFetcher.getSecret("trdm_lambda_milmove_keypair_key");
        this.alias = secretFetcher.getSecret("trdm_lambda_milmove_keypair_alias");
        this.keystoreFile = secretFetcher.getSecret("trdm_lambda_milmove_keypair_filepath");
    }
}

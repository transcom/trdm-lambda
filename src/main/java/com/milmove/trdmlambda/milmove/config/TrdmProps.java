package com.milmove.trdmlambda.milmove.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.milmove.trdmlambda.milmove.util.SecretFetcher;

import lombok.Data;

@ConfigurationProperties(prefix = "trdm")
@Configuration
@Data
public class TrdmProps {
   private String propsPath = "/tmp/client_sign.properties";
   private String encryptionUsername;

   public TrdmProps(SecretFetcher secretFetcher) {
      this.encryptionUsername = secretFetcher.getSecret("trdm_lambda_milmove_keypair_alias");
   }
}

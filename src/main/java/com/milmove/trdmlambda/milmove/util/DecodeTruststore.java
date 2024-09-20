package com.milmove.trdmlambda.milmove.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.service.S3Service;

import ch.qos.logback.classic.Logger;

@Component
public class DecodeTruststore {
    private Logger logger = (Logger) LoggerFactory.getLogger(DecodeTruststore.class);

    private String filepath;
    private String password;

    public DecodeTruststore(SecretFetcher secretFetcher, S3Service s3Service) {
        this.filepath = secretFetcher.getSecret("trdm_lambda_milmove_truststore_filepath");
        this.password = secretFetcher.getSecret("trdm_lambda_milmove_truststore_password");

        File file = new File(filepath);

        if (file.exists()) {
            logger.info("Truststore file already exists. Not recreating");
            // Set system properties for truststore
            System.setProperty("javax.net.ssl.trustStore", filepath);
            System.setProperty("javax.net.ssl.trustStorePassword", password);
            return;
        }

        try {
            String trdmTruststoreContent = s3Service.getTRDMTruststore();
            byte[] decodedBytes = Base64.getDecoder().decode(trdmTruststoreContent);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }
            logger.info("Truststore file created successfully!");
        } catch (Exception e) {
            logger.error("Failed to decode Base64 truststore into file: " + e.getMessage());
        }

        // Set system properties for truststore
        System.setProperty("javax.net.ssl.trustStore", filepath);
        System.setProperty("javax.net.ssl.trustStorePassword", password);
    }
}

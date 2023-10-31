package com.milmove.trdmlambda.milmove.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;

@Component
public class DecodeKeystore {
    private Logger logger = (Logger) LoggerFactory.getLogger(DecodeKeystore.class);

    private String base64Content;
    private String filepath;

    public DecodeKeystore(SecretFetcher secretFetcher) {
        this.base64Content = secretFetcher.getSecret("trdm_lambda_milmove_keypair_base64");
        this.filepath = secretFetcher.getSecret("trdm_lambda_milmove_keypair_filepath");

        File file = new File(filepath);

        if (file.exists()) {
            logger.info("Keystore file already exists. Not recreating");
            return;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(decodedBytes);
            }
            logger.info("Keystore file created successfully!");
        } catch (Exception e) {
            logger.error("Failed to decode Base64 keystore into file: " + e.getMessage());
        }
    }
}

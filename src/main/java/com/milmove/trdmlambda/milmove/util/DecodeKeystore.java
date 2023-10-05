package com.milmove.trdmlambda.milmove.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.TrdmRestApplication;

import ch.qos.logback.classic.Logger;

@Component
public class DecodeKeystore {
    private Logger logger = (Logger) LoggerFactory.getLogger(TrdmRestApplication.class);

    public DecodeKeystore(@Value("${TRDM_LAMBDA_MILMOVE_KEYPAIR_BASE64}") String base64Content,
            @Value("${TRDM_LAMBDA_MILMOVE_KEYPAIR_FILEPATH}") String filepath) throws IOException {
        File file = new File(filepath);

        if (file.exists()) {
            logger.info("File already exists. Not recreating");
            return;
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64Content);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
        }
    }
}

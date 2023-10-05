package com.milmove.trdmlambda.milmove.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DecodeKeystore {

    public DecodeKeystore(@Value("${TRDM_LAMBDA_MILMOVE_KEYPAIR_BASE64}") String base64Content, @Value("${TRDM_LAMBDA_MILMOVE_KEYPAIR_FILEPATH}") String filepath) throws IOException {
        File file = new File(filepath);

        if (file.exists()) {
            System.out.println("File already exists. Not recreating.");
            return;
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64Content);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
        }
    }
}

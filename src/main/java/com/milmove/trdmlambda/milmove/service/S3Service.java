package com.milmove.trdmlambda.milmove.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class S3Service {

    private Logger logger = (Logger) LoggerFactory.getLogger(S3Service.class);

    private S3Client s3Client;
    private final String trdmBucketName = "transcom-gov-milmove-stg-lambda-trdm-soap-us-gov-west-1";
    private final String trdmTruststoreKeyName = "trdm_lambda_milmove_truststore_base64.txt";

    public S3Service() throws URISyntaxException {
        logger.info("S3Service::S3Service - starting initialization of S3 Service");

        this.s3Client = S3Client.builder()
                .region(Region.US_GOV_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        logger.info("S3Service::S3Service - finished initialization of S3 Service");
    }

    public String getTRDMTruststore() {
        logger.info("S3Service::getTRDMTruststore - getting TRDM Truststore base64 string from S3");
        String trdmTruststoreBase64String = getS3Object(trdmBucketName, trdmTruststoreKeyName);
        logger.info("S3Service::getTRDMTruststore - returning TRDM Truststore string");
        return trdmTruststoreBase64String;
    }

    private String getS3Object(String bucketName, String keyName) {
        try {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            ResponseInputStream<GetObjectResponse> object = this.s3Client.getObject(objectRequest);
            String trdmTruststoreBase64 = new String(object.readAllBytes()).trim();

            logger.info("successfully obtained object from bucket " + bucketName
                    + " S3 object " + keyName);

            object.close();
            return trdmTruststoreBase64;
        } catch (S3Exception ex) {
            logger.error("S3Service::getS3ObjectBytes - error with S3 client" + ex);
        } catch (IOException ex) {
            logger.error("S3Service::getS3ObjectBytes - IO error with S3 client" + ex);
        }
        return null;
    }
}
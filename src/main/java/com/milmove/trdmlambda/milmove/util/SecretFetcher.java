package com.milmove.trdmlambda.milmove.util;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Component
public class SecretFetcher {
    private Logger logger = (Logger) LoggerFactory.getLogger(SecretFetcher.class);
    private static final String ENVIRONMENT_CONTEXT = "/trdm-lambda/";
    private static final String AWS_REGION = "us-gov-west-1";
    private final SsmClient ssmClient;

    public SecretFetcher() {
        try {
            Region region = Region.of(AWS_REGION);
    
            this.ssmClient = SsmClient.builder()
                                      .region(region)
                                      .credentialsProvider(DefaultCredentialsProvider.create())
                                      .build();
        } catch (Exception e) {
            logger.error("Error initializing ssmClient: " + e.getMessage());
            throw new RuntimeException("Failed to initialize ssmClient", e);
        }
    }

    public String getSecret(String secretName) {
        try {
            String fullSecretName = ENVIRONMENT_CONTEXT + secretName;
            GetParameterRequest request = GetParameterRequest.builder()
                                                             .name(fullSecretName)
                                                             .withDecryption(true)
                                                             .build();
            GetParameterResponse response = ssmClient.getParameter(request);
            return response.parameter().value();
        } catch (Exception e) {
            logger.error("Error when fetching secret: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve secret", e);
        }
    }
    
}

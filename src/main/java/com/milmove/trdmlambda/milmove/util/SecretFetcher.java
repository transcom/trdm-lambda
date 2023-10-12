package com.milmove.trdmlambda.milmove.util;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Component
public class SecretFetcher {
    private static final String ENVIRONMENT_CONTEXT = "/trdm-lambda/";
    private static final String AWS_REGION = "us-gov-west-1";
    private final SsmClient ssmClient;

    public SecretFetcher() {
        Region region = Region.of(AWS_REGION);

        this.ssmClient = SsmClient.builder()
                                  .region(region)
                                  .credentialsProvider(DefaultCredentialsProvider.create())
                                  .build();
    }

    public String getSecret(String secretName) {
        String fullSecretName = ENVIRONMENT_CONTEXT + secretName;

        GetParameterRequest request = GetParameterRequest.builder()
                                                         .name(fullSecretName)
                                                         .withDecryption(true)
                                                         .build();

        GetParameterResponse response = ssmClient.getParameter(request);
        return response.parameter().value();
    }
}

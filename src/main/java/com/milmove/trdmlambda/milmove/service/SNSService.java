package com.milmove.trdmlambda.milmove.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;
import jakarta.mail.MessagingException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.utils.AttributeMap;
import java.net.URISyntaxException;
import java.util.ArrayList;

@Service
public class SNSService {

    private Logger logger = (Logger) LoggerFactory.getLogger(SNSService.class);
    private final String engGovTopicARN = "arn:aws-us-gov:sns:us-gov-west-1:015932076428:eng-gov-notification";

    public SNSService() throws URISyntaxException {
        logger.info("starting initialization of SNS Service");
        logger.info("finished initializing SNS Service");
    }

    public void sendMalformedDataSNSLOA(ArrayList<String> loaSysIds) throws MessagingException, URISyntaxException {
        if (loaSysIds.size() > 0) {
            logger.info("sending malformed LOA SNS");
            String msg = "Malformed LOA data has been detected when ingesting TGET Data.";
            String loaSysIdsListHeader = "The following loaSysIds had malformed rows: \n";
            String loaSysIdsListed = "";

            for (String loaSysId : loaSysIds) {
                loaSysIdsListed += loaSysId + "\n";
            }

            msg += "\n" + loaSysIdsListHeader + "\n" + loaSysIdsListed;

            send(engGovTopicARN, msg);
        } else {
            logger.info("not sending malformed LOA SNS. List of malformed loaSysIds is empty");
        }
    }

    public void sendMalformedDataSNSTAC(ArrayList<String> tacSysIds) throws MessagingException, URISyntaxException {
        if (tacSysIds.size() > 0) {
            logger.info("sending malformed TAC SNS");
            String msg = "Malformed TAC data has been detected when ingesting TGET Data.";
            String tacSysIdsListHeader = "The following tacSysIds had malformed rows: \n";
            String tacSysIdsListed = "";

            for (String tacSysId : tacSysIds) {
                tacSysIdsListed += tacSysId + "\n";
            }

            msg += "\n" + tacSysIdsListHeader + "\n" + tacSysIdsListed;

            send(engGovTopicARN, msg);
            logger.info("finished sending malformed TAC SNS");
        } else {
            logger.info("not sending malformed TAC SNS. List of malformed tacSysIds is empty");
        }
    }

    private void send(String topicArn, String msg) throws MessagingException, URISyntaxException {
        logger.info("sending SNS message");
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(msg)
                    .topicArn(topicArn)
                    .build();

            AttributeMap attributeMap = AttributeMap.builder()
                    .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true)
                    .build();

            SdkHttpClient sdkHttpClient = new DefaultSdkHttpClientBuilder().buildWithDefaults(attributeMap);

            SnsClient snsClient = SnsClient.builder()
                    .region(Region.US_GOV_WEST_1)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .httpClient(sdkHttpClient)
                    .build();

            PublishResponse result = snsClient.publish(request);
            logger.info("message successfully sent. With status code: "
                    + result.sdkHttpResponse().statusCode() + " and Message Id: "
                    + result.messageId());

            snsClient.close();
            sdkHttpClient.close();
            logger.info("finished sending SNS message");
        } catch (SnsException e) {
            logger.error("SnsException executing sns notification cron job", e);
        } catch (SdkClientException sdke) {
            logger.error("SdkClientException: " + sdke.getMessage());
        }
    }
}

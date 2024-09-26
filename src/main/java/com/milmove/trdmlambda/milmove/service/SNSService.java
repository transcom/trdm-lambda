package com.milmove.trdmlambda.milmove.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.util.SecretFetcher;

import ch.qos.logback.classic.Logger;
import jakarta.mail.MessagingException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import java.net.URISyntaxException;
import java.util.ArrayList;

@Service
public class SNSService {

    private Logger logger = (Logger) LoggerFactory.getLogger(SNSService.class);
    private String snsTopicARN;

    public SNSService(SecretFetcher secretFetcher) throws URISyntaxException {
        logger.info("starting initialization of SNS Service");
        this.snsTopicARN = secretFetcher.getSecret("sns_topic_arn");
        logger.info("finished initializing SNS Service");
    }

    public void sendMalformedData(ArrayList<String> sysIds, String msgType)
            throws MessagingException, URISyntaxException {

        String msg = "";
        String sysIdsListed = "";
        String tacMsg = "Malformed TAC data has been detected when ingesting TGET Data. \n";
        String tacSysIdsListHeader = "The following tacSysIds had malformed rows: \n";
        String loaMsg = "Malformed LOA data has been detected when ingesting TGET Data. \n";
        String loaSysIdsListHeader = "The following loaSysIds had malformed rows: \n";

        if (msgType == "TAC") {
            msg += tacMsg + tacSysIdsListHeader;
        } else if (msgType == "LOA") {
            msg += loaMsg + loaSysIdsListHeader;
        }

        for (String sysId : sysIds) {
            sysIdsListed += sysId + "\n";
        }

        msg += sysIdsListed;

        logger.info("sending malformed " + msgType + " SNS");
        send(snsTopicARN, msg);
        logger.info("finished sending malformed " + msgType + " SNS");

    }

    private void send(String topicArn, String msg) throws MessagingException, URISyntaxException {
        logger.info("sending SNS message");
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(msg)
                    .topicArn(topicArn)
                    .build();

            SnsClient snsClient = SnsClient.builder()
                    .region(Region.US_GOV_WEST_1)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            PublishResponse result = snsClient.publish(request);
            logger.info("message successfully sent. With status code: "
                    + result.sdkHttpResponse().statusCode() + " and Message Id: "
                    + result.messageId());

            snsClient.close();
            logger.info("finished sending SNS message");
        } catch (SnsException e) {
            logger.error("SnsException executing sns notification cron job", e);
        } catch (SdkClientException sdke) {
            logger.error("SdkClientException: " + sdke.getMessage());
        }
    }
}

package com.milmove.trdmlambda.milmove.service;

import org.glassfish.jaxb.runtime.v2.schemagen.xmlschema.List;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;
import jakarta.mail.MessagingException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.ArrayList;

@Service
public class EmailService {

    private Logger logger = (Logger) LoggerFactory.getLogger(EmailService.class);

    private SesClient sesClient;
    private String sender;
    private String recipient;
    private String subject;
    private String bodyHTML;

    public EmailService() {
        logger.info("starting initialization of Email Service");
        this.sesClient = SesClient.builder()
                .region(Region.of("us-gov-west-1"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.sender = "milmove-developers@caci.com";
        this.recipient = "milmove-developers@caci.com";

        logger.info("finished initializing Email Service");
    }

    // Prepare and send Malformed Data Email for Transportation Accounting Codes
    public void sendMalformedTACDataEmail(ArrayList<String> tacSysIds) {
        logger.info("starting to send Malformed TAC Data email through from: " + this.sender + " to " + this.recipient + " with " + tacSysIds.size() + " TACs");

        // Here for testing the email functionality. Test array of tacSysIds.
        ArrayList<String> testTacArray = new ArrayList<String>();
        testTacArray.add("TestTAC1");
        testTacArray.add("TestTAC2");

        this.bodyHTML = "<html>" + "<head></head>" + "<body>" + "<h1>Malformed TAC Data</h1>"
        + "<p> See all malformed TAC codes represented by their tacSysId: " + testTacArray + "</p>" + "</body>" + "</html>";

        this.subject = "Malformed Transportation Accounting Codes";

        try {
            send(this.sesClient);
            sesClient.close();
        } catch (MessagingException e) {
            logger.error("error in SendMessageEmailRequest: " + e.getStackTrace());
        }

        logger.info("finished sending Malformed TAC Data email through from: " + this.sender + " to " + this.recipient);
    }

    // Prepare and send Malformed Data Email for Line of Accounting Codes
    public void sendMalformedLOADataEmail(ArrayList<String> loaSysIds) {
        logger.info("starting to send Malformed LOA Data email through from: " + this.sender + " to " + this.recipient + " with " + loaSysIds.size() + " LOAs");

        // Here for testing the email functionality. Test array of loaSysIds.
        ArrayList<String> testLoaArray = new ArrayList<String>();
        testLoaArray.add("TestLOA1");
        testLoaArray.add("TestLOA2");
            
        this.bodyHTML = "<html>" + "<head></head>" + "<body>" + "<h1>Malformed LOA Data</h1>"
        + "<p> See all malformed LOA codes represented by their loaSysId: " + testLoaArray  + "</p>" + "</body>" + "</html>";

        this.subject = "Malformed Line of Accounting Codes";

        try {
            send(this.sesClient);
            sesClient.close();
        } catch (MessagingException e) {
            logger.error("error in SendMessageEmailRequest: " + e.getStackTrace());
        }

        logger.info("finished sending Malformed LOA Data email through from: " + this.sender + " to " + this.recipient);
    }
 
    public void send(SesClient client) throws MessagingException {
        logger.info("start of EmailService.send()");
        Destination destination = Destination.builder()
                .toAddresses(this.recipient)
                .build();

        Content content = Content.builder()
                .data(this.bodyHTML)
                .build();

        Content sub = Content.builder()
                .data(this.subject)
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(msg)
                .source(sender)
                .build();

        try {
            logger.info("EmailService.send() - sending email");
            client.sendEmail(emailRequest);
            logger.info("finished EmailService.send()");
        } catch (SesException e) {
            logger.error("error in Email Service: " + e.awsErrorDetails().errorMessage());
        }
    }
}

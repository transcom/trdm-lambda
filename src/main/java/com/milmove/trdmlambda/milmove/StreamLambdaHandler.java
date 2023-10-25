package com.milmove.trdmlambda.milmove;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(StreamLambdaHandler.class);

    static {
        try {
            logger.info("Initializing Spring Boot application...");
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(TrdmRestApplication.class);
            logger.info("Spring Boot application initialized successfully.");
        } catch (ContainerInitializationException e) {
            logger.error("Error initializing Spring Boot application", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        try {
            String inputContent = convertStreamToString(inputStream);
            logger.info("Incoming request content: {}", inputContent);

            InputStream reprocessedStream = new ByteArrayInputStream(inputContent.getBytes(StandardCharsets.UTF_8));
            handler.proxyStream(reprocessedStream, outputStream, context);

            logger.info("Lambda request handled successfully.");
        } catch (Exception e) {
            logger.error("Error processing Lambda request", e);
        }
    }

    private static String convertStreamToString(InputStream is) {
        try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
}
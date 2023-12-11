package com.milmove.trdmlambda.milmove;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.milmove.trdmlambda.milmove.handler.LinesOfAccountingHandler;
import com.milmove.trdmlambda.milmove.handler.TransportationAccountingCodesHandler;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import ch.qos.logback.classic.Logger;

public class TrdmCronLambdaHandler implements RequestHandler<Object, String> {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(TrdmCronLambdaHandler.class);
    private static final ApplicationContext context;

    static {
        logger.info("Initializing Spring Boot application...");
        context = SpringApplication.run(TrdmRestApplication.class);
        logger.info("Spring Boot application initialized successfully.");
    }

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        try {
            logger.info("trdm cron job triggered, starting TAC handler and TGET flow");
            TransportationAccountingCodesHandler tacHandler = context.getBean(TransportationAccountingCodesHandler.class);
            tacHandler.tacCron();
            logger.info("finished tac cron handler");

            logger.info("starting LOA handler");
            LinesOfAccountingHandler loaHandler = context.getBean(LinesOfAccountingHandler.class);
            loaHandler.loaCron();
            logger.info("finished loa cron handler");
            
            logger.info("trdm cron job finished execution");
            return "trdm cron job finished execution";
        } catch (Exception e) {
            logger.error("error executing cron job", e);
            return "error in executing cron job";
        }
    }
}

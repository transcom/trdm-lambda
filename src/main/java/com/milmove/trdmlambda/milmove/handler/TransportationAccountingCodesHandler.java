package com.milmove.trdmlambda.milmove.handler;

import com.milmove.trdmlambda.milmove.service.LastTableUpdateService;
import com.milmove.trdmlambda.milmove.util.TransportationAccountingCodeParser;

import ch.qos.logback.classic.Logger;

import com.milmove.trdmlambda.milmove.service.DatabaseService;
import com.milmove.trdmlambda.milmove.service.GetTableService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.exceptions.TableRequestException;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;

@Component
public class TransportationAccountingCodesHandler {
    private Logger logger = (Logger) LoggerFactory.getLogger(TransportationAccountingCodesHandler.class);

    private final LastTableUpdateService lastTableUpdateService;
    private final GetTableService getTableService;
    private final DatabaseService databaseService;
    private final TransportationAccountingCodeParser tacParser;

    public TransportationAccountingCodesHandler(
            LastTableUpdateService lastTableUpdateService,
            GetTableService getTableService,
            DatabaseService databaseService,
            TransportationAccountingCodeParser tacParser) {

        this.lastTableUpdateService = lastTableUpdateService;
        this.getTableService = getTableService;
        this.databaseService = databaseService;
        this.tacParser = tacParser;
    }

    // This cron job will handle the entirety of ensuring the RDS db
    // is up to date with proper TGET data.
    public void tacCron() {
        LastTableUpdateResponse response;
        // Create our lastTableUpdate body
        LastTableUpdateRequest requestBody = new LastTableUpdateRequest();
        requestBody.setPhysicalName("TRNSPRTN_ACNT");

        logger.info("calling TRDM lastTableUpdate with details: {}", requestBody);
        try {
            response = lastTableUpdateService.lastTableUpdateRequest(requestBody);
        } catch (TableRequestException e) {
            logger.error("error retrieving lastTableUpdate from TRDM in tac cron handler", e);
            throw new RuntimeException("error retrieving lastTableUpdate from TRDM in tac cron handler", e);
        }
        logger.info("received response without error, proceeding with db check...");

        // Compare the response from lastTableUpdate to
        // our most recent TAC code to see if our data is out of date.
        try (Connection conn = databaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT MAX(updated_at) AS rds_last_updated FROM transportation_accounting_codes");
                ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Timestamp lastUpdatedTimestamp = rs.getTimestamp("rds_last_updated");
                XMLGregorianCalendar ourLastUpdated = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(lastUpdatedTimestamp.toString());

                if (ourLastUpdated.compare(response.getLastUpdate()) < 0) {
                    // The data in RDS is older than the last update from TRDM
                    try {
                        // Create our lastTableUpdate body
                        // Request all TGET data since our last update
                        GetTableRequest getTableRequestBody = new GetTableRequest();
                        getTableRequestBody.setPhysicalName("TRNSPRTN_ACNT");
                        getTableRequestBody.setContentUpdatedSinceDateTime(ourLastUpdated.toString());
                        getTableRequestBody.setReturnContent(true);
                        GetTableResponse getTableResponse = getTableService.getTableRequest(getTableRequestBody);

                        // Parse the response attachment to get the codes
                        List<TransportationAccountingCode> codes = tacParser.parse(getTableResponse.getAttachment());

                        // Insert the codes into RDS
                        logger.info("inserting TACs into DB");
                        databaseService.insertTransportationAccountingCodes(codes);
                        logger.info("finished inserting TACs into DB");
                    } catch (TableRequestException e) {
                        logger.error("error retrieving getTable from TRDM in tac cron handler", e);
                        throw new RuntimeException("error retrieving getTable from TRDM in tac cron handler", e);
                    } catch (IOException e) {
                        logger.error("Error processing attachment for GetTable request", e);
                    } catch (DatatypeConfigurationException e) {
                        logger.error(
                                "Error processing XMLGregorianCalendar type for provided contentUpdatedSinceDateTime value for GetTable request",
                                e);
                    }
                } else {
                    // The data in RDS is up to date, no need to proceed
                }
            }
        } catch (Exception e) {
            logger.error("Error accessing the database", e);
            // Handle exception
        }
    }
}

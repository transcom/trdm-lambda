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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

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
    private final Connection rdsConnection;
    // TODO: Static table names
    private static final Set<String> allowedTableNames = Set.of("transportation_accounting_codes",
            "lines_of_accounting"); // RDS
    private static final Set<String> allowedTrdmTableNames = Set.of("TRNSPRTN_ACNT", "LN_OF_ACCT"); // TRDM
    private static final int yearsToReturnIfOurTableIsEmpty = 3;

    public TransportationAccountingCodesHandler(
            LastTableUpdateService lastTableUpdateService,
            GetTableService getTableService,
            DatabaseService databaseService,
            TransportationAccountingCodeParser tacParser) throws SQLException {

        this.lastTableUpdateService = lastTableUpdateService;
        this.getTableService = getTableService;
        this.databaseService = databaseService;
        this.tacParser = tacParser;

        rdsConnection = databaseService.getConnection();
    }

    // This cron job will handle the entirety of ensuring the RDS db
    // is up to date with proper TGET data.
    public void tacCron()
            throws SQLException, DatatypeConfigurationException, TableRequestException, IOException, ParseException {
        // Gather the last update from TRDM
        logger.info("getting lastTableUpdate response with physical name TRNSPRTN_ACNT");
        LastTableUpdateResponse response = lastTableUpdate("TRNSPRTN_ACNT");
        logger.info("received LastTableUpdateResponse, getting our latest TGET update now");
        XMLGregorianCalendar ourLastUpdate = getOurLastTGETUpdate("transportation_accounting_codes");
        logger.info("received out latest TGET update. Comparing the 2 values to see if our TGET data is out of date");
        boolean tgetOutOfDate = isTGETDataOutOfDate(ourLastUpdate, response.getLastUpdate());
        if (tgetOutOfDate) {
            logger.info("TAC TGET data is out of date. Starting updateTGETData flow");
            updateTGETData(ourLastUpdate, "TRNSPRTN_ACNT", "transportation_accounting_codes");
        } else {
            // The data in RDS is up to date, no need to proceed
            logger.info("Transportation Accounting Codes RDS Table TGET data already up to date");
        }
    }

    private LastTableUpdateResponse lastTableUpdate(String physicalName) throws TableRequestException {
        logger.info(" calling TRDM lastTableUpdate for table {}", physicalName);
        LastTableUpdateRequest requestBody = new LastTableUpdateRequest();
        requestBody.setPhysicalName(physicalName);
        return lastTableUpdateService.lastTableUpdateRequest(requestBody);
    }

    private XMLGregorianCalendar getOurLastTGETUpdate(String tableName)
            throws SQLException, DatatypeConfigurationException, ParseException {
        if (!allowedTableNames.contains(tableName)) {
            throw new IllegalArgumentException("Invalid table name");
        }

        String query = "SELECT MAX(updated_at) AS rds_last_updated FROM " + tableName;
        PreparedStatement pstmt = rdsConnection.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            Timestamp lastUpdatedTimestamp = rs.getTimestamp("rds_last_updated");

            // Convert our last update pulled from the db into XML Gregorian Calendar
            // friendly format
            SimpleDateFormat xmlUnfriendlyLastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            SimpleDateFormat xmlFriendlyLastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            Date date = xmlUnfriendlyLastUpdateFormat.parse(lastUpdatedTimestamp.toString());
            String xmlGregorianCalendarString = xmlFriendlyLastUpdateFormat.format(date);

            return DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlGregorianCalendarString);
        }

        // If table was empty, return default years to retrieve as we have no TGET data
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.YEAR, -yearsToReturnIfOurTableIsEmpty);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    }

    private boolean isTGETDataOutOfDate(XMLGregorianCalendar ourLastUpdate, XMLGregorianCalendar trdmLastUpdate) {
        return ourLastUpdate.compare(trdmLastUpdate) < 0;
    }

    private void updateTGETData(XMLGregorianCalendar ourLastUpdate, String trdmTable, String rdsTable)
            throws TableRequestException, DatatypeConfigurationException, IOException, SQLException {
        logger.info("checking if trdm table name provided is allowed..");
        if (!allowedTrdmTableNames.contains(trdmTable)) {
            throw new IllegalArgumentException("Invalid table name");
        }
        logger.info("table {} is allowed, proceeding", trdmTable);
        // Request all TGET data from TRDM since our last update
        GetTableRequest getTableRequestBody = new GetTableRequest();
        getTableRequestBody.setPhysicalName(trdmTable);
        getTableRequestBody.setContentUpdatedSinceDateTime(ourLastUpdate.toString());
        getTableRequestBody.setReturnContent(true);
        logger.info("calling TRDM getTable with provided body {}", getTableRequestBody);
        GetTableResponse getTableResponse = getTableService.getTableRequest(getTableRequestBody);
        logger.info("received response back from TRDM getTable, beginning to parse..");
        // Parse the response attachment to get the codes
        List<TransportationAccountingCode> codes = tacParser.parse(getTableResponse.getAttachment());

        // Insert the codes into RDS
        logger.info("inserting TACs into DB");
        switch (rdsTable) {
            case "transportation_accounting_codes":
                databaseService.insertTransportationAccountingCodes(codes);
                logger.info("finished inserting TACs into DB");
                break;
            case "lines_of_accounting":
                // TODO:
            default:
                throw new IllegalArgumentException("Invalid rds table name");
        }
    }
}

package com.milmove.trdmlambda.milmove.util;

import com.milmove.trdmlambda.milmove.service.LastTableUpdateService;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.exceptions.TableRequestException;
import com.milmove.trdmlambda.milmove.model.LineOfAccounting;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;

@Component
public class Trdm {
    private Logger logger = (Logger) LoggerFactory.getLogger(Trdm.class);

    private final LastTableUpdateService lastTableUpdateService;
    private final GetTableService getTableService;
    private final DatabaseService databaseService;
    private final TransportationAccountingCodeParser tacParser;
    private final LineOfAccountingParser loaParser;

    private final Connection rdsConnection;
    private static final Set<String> allowedTableNames = Set.of("transportation_accounting_codes",
            "lines_of_accounting"); // RDS
    private static final Set<String> allowedTrdmTableNames = Set.of("TRNSPRTN_ACNT", "LN_OF_ACCT"); // TRDM
    private static final int yearsToReturnIfOurTableIsEmpty = 3;

    public Trdm(LastTableUpdateService lastTableUpdateService,
            GetTableService getTableService,
            DatabaseService databaseService,
            TransportationAccountingCodeParser tacParser,
            LineOfAccountingParser loaParser) throws SQLException {

        this.lastTableUpdateService = lastTableUpdateService;
        this.getTableService = getTableService;
        this.databaseService = databaseService;
        this.tacParser = tacParser;
        this.loaParser = loaParser;

        rdsConnection = databaseService.getConnection();

    }

    public LastTableUpdateResponse LastTableUpdate(String physicalName) throws TableRequestException {
        logger.info(" calling TRDM lastTableUpdate for table {}", physicalName);
        LastTableUpdateRequest requestBody = new LastTableUpdateRequest();
        requestBody.setPhysicalName(physicalName);
        return lastTableUpdateService.lastTableUpdateRequest(requestBody);
    }

    public XMLGregorianCalendar GetOurLastTGETUpdate(String tableName)
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
            // friendly format ending in "Z" for Zulu.
            SimpleDateFormat xmlUnfriendlyLastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            SimpleDateFormat xmlFriendlyLastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            xmlFriendlyLastUpdateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = xmlUnfriendlyLastUpdateFormat.parse(lastUpdatedTimestamp.toString());
            String xmlGregorianCalendarString = xmlFriendlyLastUpdateFormat.format(date);

            return DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlGregorianCalendarString);
        }

        // If table was empty, return default years to retrieve as we have no TGET data
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.YEAR, -yearsToReturnIfOurTableIsEmpty);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    }

    public static XMLGregorianCalendar AddOneWeek(XMLGregorianCalendar originalDate)
            throws DatatypeConfigurationException {
        GregorianCalendar calendar = originalDate.toGregorianCalendar();
        calendar.add(GregorianCalendar.WEEK_OF_YEAR, 1);

        SimpleDateFormat xmlFriendlyLastUpdateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        xmlFriendlyLastUpdateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String xmlGregorianCalendarString = xmlFriendlyLastUpdateFormat.format(calendar.getTime());

        return DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlGregorianCalendarString);
    }

    public boolean IsTGETDataOutOfDate(XMLGregorianCalendar ourLastUpdate, XMLGregorianCalendar trdmLastUpdate) {
        return ourLastUpdate.compare(trdmLastUpdate) < 0;
    }

    public void UpdateTGETData(XMLGregorianCalendar ourLastUpdate, String trdmTable, String rdsTable)
            throws TableRequestException, DatatypeConfigurationException, IOException, SQLException {
        logger.info("checking if trdm table name provided is allowed..");
        if (!allowedTrdmTableNames.contains(trdmTable)) {
            throw new IllegalArgumentException("Invalid table name");
        }
        logger.info("table {} is allowed, proceeding", trdmTable);

        XMLGregorianCalendar oneWeekLater = AddOneWeek(ourLastUpdate);

        // Request all TGET data from TRDM since our last update
        GetTableRequest getTableRequestBody = new GetTableRequest();
        getTableRequestBody.setPhysicalName(trdmTable);
        getTableRequestBody.setContentUpdatedSinceDateTime(ourLastUpdate.toString());
        getTableRequestBody.setReturnContent(true);
        getTableRequestBody.setContentUpdatedOnOrBeforeDateTime(oneWeekLater.toString());
        logger.info("calling TRDM getTable with provided body {}", getTableRequestBody);
        GetTableResponse getTableResponse = getTableService.getTableRequest(getTableRequestBody);

        // Insert the codes into RDS
        switch (rdsTable) {
            case "transportation_accounting_codes":
                // Parse the response attachment to get the codes
                logger.info("parsing response back from TRDM getTable");
                List<TransportationAccountingCode> codes = tacParser.parse(getTableResponse.getAttachment(),
                        oneWeekLater);
                logger.info("inserting TACs into DB");
                databaseService.insertTransportationAccountingCodes(codes);
                logger.info("finished inserting TACs into DB");
                break;
            case "lines_of_accounting":
                // Parse the response attachment to get the loas
                logger.info("parsing response back from TRDM getTable");
                List<LineOfAccounting> loas = loaParser.parse(getTableResponse.getAttachment(),
                        oneWeekLater);
                logger.info("inserting LOAs into DB");
                databaseService.insertLinesOfAccounting(loas);
                logger.info("finished inserting LOAs into DB");
                break;
            default:
                throw new IllegalArgumentException("Invalid rds table name");
        }
    }

}

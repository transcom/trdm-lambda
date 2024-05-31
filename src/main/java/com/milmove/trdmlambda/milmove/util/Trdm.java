package com.milmove.trdmlambda.milmove.util;

import com.milmove.trdmlambda.milmove.service.LastTableUpdateService;

import ch.qos.logback.classic.Logger;

import com.milmove.trdmlambda.milmove.service.DatabaseService;
import com.milmove.trdmlambda.milmove.service.GetTableService;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

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

    public void UpdateTGETData(XMLGregorianCalendar ourLastUpdate, String trdmTable, String rdsTable,
            XMLGregorianCalendar trdmLastUpdate)
            throws TableRequestException, DatatypeConfigurationException, IOException, SQLException {
        logger.info("checking if trdm table name provided is allowed..");
        if (!allowedTrdmTableNames.contains(trdmTable)) {
            throw new IllegalArgumentException("Invalid table name");
        }
        logger.info("table {} is allowed, proceeding", trdmTable);

        boolean receivedData = false;

        while (ourLastUpdate.compare(trdmLastUpdate) <= 0 && !receivedData) {
            XMLGregorianCalendar oneWeekLater = AddOneWeek(ourLastUpdate);
            // Add check that our "One week later" addition doesn't go past the TRDM last
            // update. If it does, then just set the filter to their last update
            if (oneWeekLater.compare(trdmLastUpdate) > 0) {
                oneWeekLater = trdmLastUpdate;
            }
            // Request all TGET data from TRDM since our last update
            GetTableRequest getTableRequestBody = new GetTableRequest();
            getTableRequestBody.setPhysicalName(trdmTable);
            getTableRequestBody.setContentUpdatedSinceDateTime(ourLastUpdate.toString());
            getTableRequestBody.setReturnContent(true);
            getTableRequestBody.setContentUpdatedOnOrBeforeDateTime(oneWeekLater.toString());
            logger.info("calling TRDM getTable with provided body {}", getTableRequestBody);
            GetTableResponse getTableResponse = getTableService.getTableRequest(getTableRequestBody);

            // Check if the attachment has 0 rows.
            BigInteger rows = getTableResponse.getRowCount();
            if (rows.compareTo(BigInteger.ZERO) > 0) {
                // We received rows from the getTable request
                receivedData = true;
                // Insert the codes into RDS
                switch (rdsTable) {
                    case "transportation_accounting_codes":
                        // Parse the response attachment to get the codes
                        logger.info("parsing response back from TRDM getTable");
                        List<TransportationAccountingCode> codes = tacParser.parse(getTableResponse.getAttachment(),
                                oneWeekLater);
                        // Get all Tacs
                        ArrayList<TransportationAccountingCode> currentTacs = databaseService.getAllTacs();

                        // Generate list of TACs that needs to be updated. If TAC is in curentTacs then the
                        // TAC will be in updateTacs list because the TAC already exist
                        List<TransportationAccountingCode> updateTacs = identifyTacsToUpdate(codes, currentTacs);

                        // Generate list of TACs that needs to be created. If the TAC is not in updateTacs then it
                        // will be in createTacs because it does not exist and needs to be created.
                        List<TransportationAccountingCode> createTacs = identifyTacsToCreate(codes, updateTacs);

                        logger.info("updating TACs in DB");
                        databaseService.updateTransportationAccountingCodes(updateTacs);
                        logger.info("finished updating TACs in DB");

                        logger.info("inserting TACs into DB");
                        databaseService.insertTransportationAccountingCodes(createTacs);
                        logger.info("finished inserting TACs into DB");
                        break;
                    case "lines_of_accounting":
                        // Parse the response attachment to get the loas
                        logger.info("parsing response back from TRDM getTable");
                        List<LineOfAccounting> loas = loaParser.parse(getTableResponse.getAttachment(),
                                oneWeekLater);

                        // Remove unneeded duplicates based on having a non unique loa_sys_id and a id
                        // not referenced as a loa_id in the transportation_accounting_codes table
                        databaseService.deleteDuplicateLoas();

                        // Get all loas
                        ArrayList<LineOfAccounting> currentLoas = databaseService.getAllLoas();

                        // Generate list of loas that needs to be updated. If loas are in curentLoas then the
                        // loa will be in updateLoas list because the loa already exist
                        List<LineOfAccounting> updateLoas = identifyLoasToUpdate(loas, currentLoas);

                        // Build TAC codes needed to create. If the code is not in updateTacs then it
                        // will be in createTacs because it does not exist and needs to be created.
                        List<LineOfAccounting> createLoas = identifyLoasToCreate(loas, updateLoas);

                        logger.info("updating LOAs in DB");
                        databaseService.updateLinesOfAccountingCodes(updateLoas);
                        logger.info("finished updating LOAs in DB");
                        logger.info("inserting LOAs into DB");
                        databaseService.insertLinesOfAccounting(createLoas);
                        logger.info("finished inserting LOAs into DB");
                        break;
                    default:
                        throw new IllegalArgumentException("Data insertion for this table has not been configured");
                }
                // Exit the while loop
                break;
            } else if (oneWeekLater.equals(trdmLastUpdate)) {
                // We have reached the trdm last update and data was not found
                logger.info("reached TRDM last update date without finding any new data for {}", rdsTable);
                break;
            }
            // No data was found and oneWeekLater is not equal to TRDM's last update, loop
            // again and look for next week's data
            logger.info(
                    "no rows returned in the file attachment from TRDM for date range {} thru {}. Looping again, adding a week up to TRDM last table update",
                    ourLastUpdate, oneWeekLater);
            ourLastUpdate = oneWeekLater;
        }

    }

    // Identify TACS to update. If their tacSysId already exist then w eneed to
    // update it
    public List<TransportationAccountingCode> identifyTacsToUpdate(List<TransportationAccountingCode> newTacs,
            ArrayList<TransportationAccountingCode> currentTacs) {
        return newTacs.stream().filter(newTac -> currentTacs.stream().map(currentTac -> currentTac.getTacSysID())
                .collect(Collectors.toList()).contains(newTac.getTacSysID())).collect(Collectors.toList());
    }

    // Identify TACS to create. If the tacSysId is not in the update list then it it
    // doesnt exist and needs to be created.
    public List<TransportationAccountingCode> identifyTacsToCreate(List<TransportationAccountingCode> newTacs,
            List<TransportationAccountingCode> updatedTacs) {
        logger.info("identifying TACS to create");
        return newTacs.stream().filter(newTac -> !updatedTacs.stream().map(updatedTac -> updatedTac.getTacSysID())
                .collect(Collectors.toList()).contains(newTac.getTacSysID())).collect(Collectors.toList());
    }

    public List<LineOfAccounting> identifyLoasToUpdate(List<LineOfAccounting> newLoas,
            ArrayList<LineOfAccounting> currentLoas) {
        logger.info("identifying Loas to update");
        return newLoas.stream().filter(newLoa -> currentLoas.stream().map(currentLoa -> currentLoa.getLoaSysID())
                .collect(Collectors.toList()).contains(newLoa.getLoaSysID())).collect(Collectors.toList());
    }

    public List<LineOfAccounting> identifyLoasToCreate(List<LineOfAccounting> newLoas,
            List<LineOfAccounting> updatedLoas) {
        logger.info("identifying Loas to create");
        return newLoas.stream().filter(newLoa -> !updatedLoas.stream().map(updatedLoa -> updatedLoa.getLoaSysID())
                .collect(Collectors.toList()).contains(newLoa.getLoaSysID())).collect(Collectors.toList());
    }

    // Identify unneeded Loas based on their loaSysId being non unique in the loa DB
    // table and their ID not referenced in TAC table loa_id.
    public ArrayList<String> identifyDuplicateLinesOfAccounting(ArrayList<LineOfAccounting> loas) throws SQLException {
        logger.info("identifying duplicate LOAs");

        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<String> duplicateLoaSysIds = new ArrayList<String>();

        for (LineOfAccounting currentLoa : loas) {
            if (!temp.contains(currentLoa.getLoaSysID())) {
                temp.add(currentLoa.getLoaSysID());
            } else {
                duplicateLoaSysIds.add(currentLoa.getLoaSysID());
            }
        }

        logger.info("finished identifying duplicate loaSysIds");
        return duplicateLoaSysIds;
    }

}

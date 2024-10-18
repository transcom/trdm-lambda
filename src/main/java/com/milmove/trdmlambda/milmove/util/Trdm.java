package com.milmove.trdmlambda.milmove.util;

import com.milmove.trdmlambda.milmove.service.LastTableUpdateService;
import com.milmove.trdmlambda.milmove.service.SNSService;

import ch.qos.logback.classic.Logger;

import com.milmove.trdmlambda.milmove.service.DatabaseService;
import com.milmove.trdmlambda.milmove.service.GetTableService;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
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
import java.util.UUID;
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
    private final SNSService snsService;
    private String snsForcePublish;

    private final Connection rdsConnection;
    private static final Set<String> allowedTableNames = Set.of("transportation_accounting_codes",
            "lines_of_accounting"); // RDS
    private static final Set<String> allowedTrdmTableNames = Set.of("TRNSPRTN_ACNT", "LN_OF_ACCT"); // TRDM
    private static final int yearsToReturnIfOurTableIsEmpty = 3;

    public Trdm(LastTableUpdateService lastTableUpdateService,
            GetTableService getTableService,
            DatabaseService databaseService,
            SNSService snsService,
            TransportationAccountingCodeParser tacParser,
            SecretFetcher secretFetcher,
            LineOfAccountingParser loaParser) throws SQLException {

        this.lastTableUpdateService = lastTableUpdateService;
        this.getTableService = getTableService;
        this.databaseService = databaseService;
        this.snsService = snsService;
        this.tacParser = tacParser;
        this.loaParser = loaParser;
        this.snsForcePublish = secretFetcher.getSecret("sns_force_publish");

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
            throws TableRequestException, DatatypeConfigurationException, IOException, SQLException,
            URISyntaxException {
        logger.info("checking if trdm table name provided is allowed..");
        if (!allowedTrdmTableNames.contains(trdmTable)) {
            throw new IllegalArgumentException("Invalid table name");
        }
        logger.info("table {} is allowed, proceeding", trdmTable);

        boolean receivedData = false;

        boolean forcePublish = false;
        if (snsForcePublish != null && snsForcePublish.equals(Boolean.toString(true))) {
            forcePublish = true;
        }

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

                        logger.info("inserting TACs into DB");
                        databaseService.insertTransportationAccountingCodes(codes);
                        logger.info("finished inserting TACs into DB");

                        if (tacParser.getMalformedTacList().size() > 0 || forcePublish) {
                            try {
                                logger.info(
                                        "malformed TAC data detected when parsing. Sending malformed TAC data SNS notification");
                                snsService.sendMalformedData(tacParser.getMalformedTacList(), "TAC");
                                ;
                                logger.info("finished sending malformed TAC SNS notification");
                            } catch (Exception e) {
                                logger.error("failed to send malformed TAC SNS notification: " + e.getMessage());
                            }
                        }

                        break;
                    case "lines_of_accounting":
                        // Parse the response attachment to get the loas
                        logger.info("parsing response back from TRDM getTable");
                        List<LineOfAccounting> loas = loaParser.parse(getTableResponse.getAttachment(),
                                oneWeekLater);

                        logger.info("inserting LOAs into DB");
                        databaseService.insertLinesOfAccounting(loas);
                        logger.info("finished inserting LOAs into DB");

                        if (loaParser.getMalformedLoaList().size() > 0 || forcePublish) {
                            try {
                                logger.info(
                                        "malformed LOA data detected when parsing. Sending malformed LOA data SNS notification");
                                snsService.sendMalformedData(loaParser.getMalformedLoaList(), "LOA");
                                ;
                                logger.info("finished sending malformed LOA SNS notification");
                            } catch (Exception e) {
                                logger.error("failed to send malformed LOA SNS notification: " + e.getMessage());
                            }
                        }

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


    // Identify TACS to create. If the tacSysId is not in the update list then it it
    // doesn't exist and needs to be created.
    public List<TransportationAccountingCode> identifyTacsToCreate(List<TransportationAccountingCode> newTacs,
            List<TransportationAccountingCode> updatedTacs) {
        logger.info("identifying TACS to create");
        return newTacs.stream().filter(newTac -> !updatedTacs.stream().map(updatedTac -> updatedTac.getTacSysID())
                .collect(Collectors.toList()).contains(newTac.getTacSysID())).collect(Collectors.toList());
    }

    // Identify loas to update based on checking if the new loa loa_sys_id is in a
    // list of loa_sys_ids made from mapping out loa_sys_ids from a list of
    // currentLoas in the database
    public List<LineOfAccounting> identifyLoasToUpdate(List<LineOfAccounting> newLoas,
            ArrayList<LineOfAccounting> currentLoas) {
        logger.info("identifying Loas to update. new LOA count: " + newLoas.size() + ". Current LOA count: " + currentLoas.size());

        ArrayList<LineOfAccounting> loasToUpdate = new ArrayList<LineOfAccounting>();

        ArrayList<String> currentLoaSysIds = new ArrayList<String>();
        for (LineOfAccounting loa : currentLoas) {
            currentLoaSysIds.add(loa.getLoaSysID());
        }

        logger.info("built list of current loaSysIds");


        for (LineOfAccounting loa : newLoas) {
            if (currentLoaSysIds.contains(loa.getLoaSysID())) {
                loasToUpdate.add(loa);
            }
        }

        logger.info("built list of loas to update");

        return loasToUpdate;
    }

    // This method identifies loas to create based on filtering the newLoas by which
    // loa has a loaSysId that is in the update list
    public List<LineOfAccounting> identifyLoasToCreate(List<LineOfAccounting> newLoas,
            List<LineOfAccounting> updatedLoas) {
        logger.info("identifying Loas to create");
        return newLoas.stream()
                .filter(newLoa -> !updatedLoas.stream() // If the newLoa loaSysId is not in the list of loaSysIds to be
                                                        // updated then include it because it needs to be created
                        .map(updatedLoa -> updatedLoa.getLoaSysID()) // Map out loa_sys_ids that are going to be updated
                        .collect(Collectors.toList())
                        .contains(newLoa.getLoaSysID())) // Does the newLoa loa_sys_id exist in a list of loa_sys_ids
                .collect(Collectors.toList());
    }

    // Identify Loas to delete based on if their loaSysId is not unique, their
    // id/primary key is not referenced in TACS loa_id and the loa created_at is the
    // latest
    public ArrayList<LineOfAccounting> identifyDuplicateLoasToDelete(ArrayList<LineOfAccounting> loas,
            ArrayList<TransportationAccountingCode> tacs, ArrayList<String> duplicateLoaSysIds) throws SQLException {
        logger.info("identifying duplicate Line of Accounting codes to delete");
        logger.info("LOA codes count: " + loas.size());
        logger.info("TAC codes count: " + tacs.size());
        logger.info("Duplicate LOA codes loa_sys_ids count: " + duplicateLoaSysIds.size());

        // Remove any LOA with a null updated_at value from consideration for deletion.
        List<LineOfAccounting> loasNoNulls = loas.stream().filter(loa -> loa.getUpdatedAt() != null).toList();

        // Store loas that needs to be checked for deletion
        ArrayList<LineOfAccounting> duplicateLoas = new ArrayList<LineOfAccounting>();

        logger.info("starting to identify duplicate LOA codes");
        for (LineOfAccounting loa : loasNoNulls) {
            if (duplicateLoaSysIds.contains(loa.getLoaSysID())) {
                duplicateLoas.add(loa);
            }
        }
        logger.info("finished identifying duplicate LOA codes. Count: " + duplicateLoas.size());

        logger.info("starting to identify duplicate LOA codes that are not referenced by a TAC code");
        // Duplicate loas not referenced in TACS
        ArrayList<LineOfAccounting> duplicateUnreferencedLoas = new ArrayList<LineOfAccounting>();

        // Map all TAC loa_id and eliminate duplicates by putting them in a Set. This
        // will help speed up lookups
        Set<UUID> tacLoaIdList = tacs.stream().map(tac -> tac.getLoaID()).collect(Collectors.toSet());

        // Check if duplicateLoas ID, Primary Key, are being referenced in TACS
        for (LineOfAccounting loa : duplicateLoas) {
            if (!tacLoaIdList.contains(loa.getId())) {
                duplicateUnreferencedLoas.add(loa);
            }
        }

        logger.info("finished identifying duplicate LOA codes that are not referenced by a TAC code. Count: "
                + duplicateUnreferencedLoas.size());

        // Get a set of loaSysIds made from the list of unreferenced duplicate loas to
        // loop through to find the latest loa for deletion
        Set<String> setOfLoaSysIds = duplicateUnreferencedLoas.stream().map(loa -> loa.getLoaSysID())
                .collect(Collectors.toSet());

        logger.info("starting to identify which duplicate unreferenced LOA codes to delete based on updated_at value");
        ArrayList<LineOfAccounting> loasToDelete = new ArrayList<LineOfAccounting>();
        for (String loaSysId : setOfLoaSysIds) {

            // Get a sorted by date list of loas with same sysId in
            // duplicateUnreferencedLoas
            List<LineOfAccounting> sortedLoasByUpdatedAt = duplicateUnreferencedLoas.stream()
                    .filter(loa -> loa.getLoaSysID().equals(loaSysId))
                    .sorted((l1, l2) -> l1.getUpdatedAt().compareTo(l2.getUpdatedAt()))
                    .collect(Collectors.toList());

            loasToDelete.add(sortedLoasByUpdatedAt.get(0));
        }

        logger.info("finished identifying which duplicate unreferenced LOA codes to delete based on updated_at value");
        logger.info("Amount of LOA codes to be deleted: " + loasToDelete.size());
        logger.info("finished identifying duplicate Line of Accounting codes to delete");
        return loasToDelete;
    }
}

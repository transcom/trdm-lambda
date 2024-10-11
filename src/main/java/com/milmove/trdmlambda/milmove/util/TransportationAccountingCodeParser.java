package com.milmove.trdmlambda.milmove.util;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;

@Component
public class TransportationAccountingCodeParser {
    private Logger logger = (Logger) LoggerFactory.getLogger(TransportationAccountingCodeParser.class);

    ArrayList<String> malformedTacSysIds = new ArrayList<String>();

    // Create our map for TGET parsing
    Map<String, Integer> columnNamesAndLocations = new HashMap<>();

    private static final String[] expectedColumnNames = {
            "TAC_SYS_ID", "LOA_SYS_ID", "TRNSPRTN_ACNT_CD", "TAC_FY_TXT", "TAC_FN_BL_MOD_CD", "ORG_GRP_DFAS_CD",
            "TAC_MVT_DSG_ID", "TAC_TY_CD", "TAC_USE_CD", "TAC_MAJ_CLMT_ID", "TAC_BILL_ACT_TXT", "TAC_COST_CTR_NM",
            "BUIC", "TAC_HIST_CD", "TAC_STAT_CD", "TRNSPRTN_ACNT_TX", "TRNSPRTN_ACNT_BGN_DT", "TRNSPRTN_ACNT_END_DT",
            "DD_ACTVTY_ADRS_ID", "TAC_BLLD_ADD_FRST_LN_TX", "TAC_BLLD_ADD_SCND_LN_TX", "TAC_BLLD_ADD_THRD_LN_TX",
            "TAC_BLLD_ADD_FRTH_LN_TX", "TAC_FNCT_POC_NM", "ROW_STS_CD"
    };

    public List<TransportationAccountingCode> parse(byte[] fileContent, XMLGregorianCalendar trdmLastUpdate)
            throws RuntimeException {
        logger.info("beginning to parse TAC TGET data");
        List<TransportationAccountingCode> codes = new ArrayList<>();
        Scanner scanner = new Scanner(new ByteArrayInputStream(fileContent));
        logger.info("skipping the first line and then gathering headers");
        String[] columnHeaders = scanner.nextLine().split("\\|"); // Skip first line and gather headers immediately

        // Sort both the expectedColumnNames and columnHeaders before comparing
        String[] sortedExpectedColumnNames = Arrays.copyOf(expectedColumnNames, expectedColumnNames.length);
        String[] sortedColumnHeaders = Arrays.copyOf(columnHeaders, columnHeaders.length);
        Arrays.sort(sortedExpectedColumnNames);
        Arrays.sort(sortedColumnHeaders);

        if (!Arrays.equals(sortedExpectedColumnNames, sortedColumnHeaders)) {
            String message = String.format("Column headers do not match expected format. Received %s and expected %s",
                    Arrays.toString(columnHeaders), Arrays.toString(expectedColumnNames));
            throw new RuntimeException(message);
        }

        // Map their order for when processing the TAC values properly
        for (int i = 0; i < columnHeaders.length; i++) {
            columnNamesAndLocations.put(columnHeaders[i], i);
        }

        logger.info("headers received and mapped, beginning to process every other line");
        // Loop until the last line in the file is found

        int row = 1;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // "Unclassified" will always be the last line in the file
            if (line.equals("Unclassified")) {
                logger.info("finished parsing TGET data file from TRDM");
                break;
            }
            String[] values = line.split("\\|");
            TransportationAccountingCode code = processLineIntoTAC(values, columnNamesAndLocations, trdmLastUpdate);

            if (code != null) {
                codes.add(code);
            } else {
                logger.info("failed to parse TGET TAC data row: " + row);
            }
            row++;
        }
        logger.info("finished parsing every single line");

        return codes;
    }

    private TransportationAccountingCode processLineIntoTAC(String[] values, Map<String, Integer> columnHeaders,
            XMLGregorianCalendar trdmLastUpdate) throws RuntimeException {

        // Check if value length does not align with columns
        if (values.length != columnHeaders.size()) {
            logger.info("TGET file row is malformed. This row of TAC data will not be parsed.");
            // Add TAC_SYS_ID to skipped TACs array
            malformedTacSysIds.add((values[columnHeaders.get("TAC_SYS_ID")]));
            logger.info("TAC data with TAC_SYS_ID: " + values[columnHeaders.get("TAC_SYS_ID")]
                    + " skipped due to malformed data.");
            return null; // Skip this line
        }

        // Check if TAC is empty or if ROW_STS_CD is "DLT"
        if (values[columnHeaders.get("TRNSPRTN_ACNT_CD")].isEmpty()
                || "DLT".equals(values[columnHeaders.get("ROW_STS_CD")])) {
            return null; // Skip this line
        }

        try {
            LocalDateTime effectiveDate = LocalDateTime.parse(values[columnHeaders.get("TRNSPRTN_ACNT_BGN_DT")],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime expiredDate = LocalDateTime.parse(values[columnHeaders.get("TRNSPRTN_ACNT_END_DT")],
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            TransportationAccountingCode code = new TransportationAccountingCode();
            code.setTacSysID((String) getDelimitedValue(values[columnHeaders.get("TAC_SYS_ID")]));
            code.setLoaSysID((String) getDelimitedValue(values[columnHeaders.get("LOA_SYS_ID")]));
            code.setTac((String) getDelimitedValue(values[columnHeaders.get("TRNSPRTN_ACNT_CD")]));
            code.setTacFyTxt((String) getDelimitedValue(values[columnHeaders.get("TAC_FY_TXT")]));
            code.setTacFnBlModCd((String) getDelimitedValue(values[columnHeaders.get("TAC_FN_BL_MOD_CD")]));
            code.setOrgGrpDfasCd((String) getDelimitedValue(values[columnHeaders.get("ORG_GRP_DFAS_CD")]));
            code.setTacMvtDsgID((String) getDelimitedValue(values[columnHeaders.get("TAC_MVT_DSG_ID")]));
            code.setTacTyCd((String) getDelimitedValue(values[columnHeaders.get("TAC_TY_CD")]));
            code.setTacUseCd((String) getDelimitedValue(values[columnHeaders.get("TAC_USE_CD")]));
            code.setTacMajClmtID((String) getDelimitedValue(values[columnHeaders.get("TAC_MAJ_CLMT_ID")]));
            code.setTacBillActTxt((String) getDelimitedValue(values[columnHeaders.get("TAC_BILL_ACT_TXT")]));
            code.setTacCostCtrNm((String) getDelimitedValue(values[columnHeaders.get("TAC_COST_CTR_NM")]));
            code.setBuic((String) getDelimitedValue(values[columnHeaders.get("BUIC")]));
            code.setTacHistCd((String) getDelimitedValue(values[columnHeaders.get("TAC_HIST_CD")]));
            code.setTacStatCd((String) getDelimitedValue(values[columnHeaders.get("TAC_STAT_CD")]));
            code.setTrnsprtnAcntTx((String) getDelimitedValue(values[columnHeaders.get("TRNSPRTN_ACNT_TX")]));
            code.setDdActvtyAdrsID((String) getDelimitedValue(values[columnHeaders.get("DD_ACTVTY_ADRS_ID")]));
            code.setTacBlldAddFrstLnTx((String) getDelimitedValue(values[columnHeaders.get("TAC_BLLD_ADD_FRST_LN_TX")]));
            code.setTacBlldAddScndLnTx((String) getDelimitedValue(values[columnHeaders.get("TAC_BLLD_ADD_SCND_LN_TX")]));
            code.setTacBlldAddThrdLnTx((String) getDelimitedValue(values[columnHeaders.get("TAC_BLLD_ADD_THRD_LN_TX")]));
            code.setTacBlldAddFrthLnTx((String) getDelimitedValue(values[columnHeaders.get("TAC_BLLD_ADD_FRTH_LN_TX")]));
            code.setTacFnctPocNm((String) getDelimitedValue(values[columnHeaders.get("TAC_FNCT_POC_NM")]));
            code.setTrnsprtnAcntBgnDt(effectiveDate);
            code.setTrnsprtnAcntEndDt(expiredDate);
            code.setUpdatedAt(convertXMLGregorianCalendarToLocalDateTime(trdmLastUpdate));
            return code;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Error parsing dates: " + e.getMessage());
        }
    }

    private LocalDateTime convertXMLGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }
        GregorianCalendar gregorianCalendar = xmlGregorianCalendar.toGregorianCalendar();
        gregorianCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return gregorianCalendar.toZonedDateTime().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }

    public ArrayList<String> getMalformedTacList() {
        return malformedTacSysIds;
    }

    // Helper function to support null and trimming from the file
    private Object getDelimitedValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}

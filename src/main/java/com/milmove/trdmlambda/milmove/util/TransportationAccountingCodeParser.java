package com.milmove.trdmlambda.milmove.util;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;

@Component
public class TransportationAccountingCodeParser {
    private Logger logger = (Logger) LoggerFactory.getLogger(TransportationAccountingCodeParser.class);
    // Create our map for TGET parsing
    Map<String, Integer> columnNamesAndLocations = new HashMap<>();

    private static final String[] expectedColumnNames = {
        "TAC_SYS_ID", "LOA_SYS_ID", "TRNSPRTN_ACNT_CD", "TAC_FY_TXT", "TAC_FN_BL_MOD_CD", "ORG_GRP_DFAS_CD", "TAC_MVT_DSG_ID", "TAC_TY_CD", "TAC_USE_CD", "TAC_MAJ_CLMT_ID", "TAC_BILL_ACT_TXT", "TAC_COST_CTR_NM", "BUIC", "TAC_HIST_CD", "TAC_STAT_CD", "TRNSPRTN_ACNT_TX", "TRNSPRTN_ACNT_BGN_DT", "TRNSPRTN_ACNT_END_DT", "DD_ACTVTY_ADRS_ID", "TAC_BLLD_ADD_FRST_LN_TX", "TAC_BLLD_ADD_SCND_LN_TX", "TAC_BLLD_ADD_THRD_LN_TX", "TAC_BLLD_ADD_FRTH_LN_TX", "TAC_FNCT_POC_NM", "ROW_STS_CD"
    };

    public List<TransportationAccountingCode> parse(byte[] fileContent) throws RuntimeException {
        logger.info("beginning to parse TAC TGET data");
        List<TransportationAccountingCode> codes = new ArrayList<>();
        Scanner scanner = new Scanner(new ByteArrayInputStream(fileContent));
        logger.info("skipping the first line");
        scanner.nextLine(); // Skip first line

        logger.info("gathering headers");
        // Get the column headers from the line
        String[] columnHeaders = scanner.nextLine().split("\\|");

        // TODO: Possibly allow for unexpected column names and proceed with the columns we are familiar with. This will be a must for LOA.
        if (!Arrays.equals(expectedColumnNames, columnHeaders)) {
            throw new RuntimeException("Column headers do not match expected format.");
        }

        // Map their order for when processing the TAC values properly
        for (int i = 0; i < columnHeaders.length; i++) {
            columnNamesAndLocations.put(columnHeaders[i], i);
        }

        logger.info("headers received and mapped, beginning to process every other line");
        // Loop until the last line in the file is found
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // "Unclassified" will always be the last line in the file
            if (line.equals("Unclassified")) {
                logger.info("finished parsing TGET data file from TRDM");
                break;
            }
            String[] values = line.split("\\|");
            TransportationAccountingCode code = processLineIntoTAC(values, columnNamesAndLocations);

            if (code != null) {
                codes.add(code);
            }
        }
        logger.info("finished parsing every single line");

        scanner.close();

        return codes;
    }

    private TransportationAccountingCode processLineIntoTAC(String[] values, Map<String, Integer> columnHeaders) throws RuntimeException {
        // Check if TAC is empty or if ROW_STS_CD is "DLT"
        if (values[columnHeaders.get("TRNSPRTN_ACNT_CD")].isEmpty() || "DLT".equals(values[columnHeaders.get("ROW_STS_CD")])) {
            return null; // Skip this line
        }
    
        try {
            LocalDateTime effectiveDate = LocalDateTime.parse(values[columnHeaders.get("TRNSPRTN_ACNT_BGN_DT")], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime expiredDate = LocalDateTime.parse(values[columnHeaders.get("TRNSPRTN_ACNT_END_DT")], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            TransportationAccountingCode code = new TransportationAccountingCode();
            code.setTacSysID(values[columnHeaders.get("TAC_SYS_ID")]);
            code.setLoaSysID(values[columnHeaders.get("LOA_SYS_ID")]);
            code.setTac(values[columnHeaders.get("TRNSPRTN_ACNT_CD")]);
            code.setTacFyTxt(values[columnHeaders.get("TAC_FY_TXT")]);
            code.setTacFnBlModCd(values[columnHeaders.get("TAC_FN_BL_MOD_CD")]);
            code.setOrgGrpDfasCd(values[columnHeaders.get("ORG_GRP_DFAS_CD")]);
            code.setTacMvtDsgID(values[columnHeaders.get("TAC_MVT_DSG_ID")]);
            code.setTacTyCd(values[columnHeaders.get("TAC_TY_CD")]);
            code.setTacUseCd(values[columnHeaders.get("TAC_USE_CD")]);
            code.setTacMajClmtID(values[columnHeaders.get("TAC_MAJ_CLMT_ID")]);
            code.setTacBillActTxt(values[columnHeaders.get("TAC_BILL_ACT_TXT")]);
            code.setTacCostCtrNm(values[columnHeaders.get("TAC_COST_CTR_NM")]);
            code.setBuic(values[columnHeaders.get("BUIC")]);
            code.setTacHistCd(values[columnHeaders.get("TAC_HIST_CD")]);
            code.setTacStatCd(values[columnHeaders.get("TAC_STAT_CD")]);
            code.setTrnsprtnAcntTx(values[columnHeaders.get("TRNSPRTN_ACNT_TX")]);
            code.setDdActvtyAdrsID(values[columnHeaders.get("DD_ACTVTY_ADRS_ID")]);
            code.setTacBlldAddFrstLnTx(values[columnHeaders.get("TAC_BLLD_ADD_FRST_LN_TX")]);
            code.setTacBlldAddScndLnTx(values[columnHeaders.get("TAC_BLLD_ADD_SCND_LN_TX")]);
            code.setTacBlldAddThrdLnTx(values[columnHeaders.get("TAC_BLLD_ADD_THRD_LN_TX")]);
            code.setTacBlldAddFrthLnTx(values[columnHeaders.get("TAC_BLLD_ADD_FRTH_LN_TX")]);
            code.setTacFnctPocNm(values[columnHeaders.get("TAC_FNCT_POC_NM")]);
            code.setTrnsprtnAcntBgnDt(effectiveDate);
            code.setTrnsprtnAcntEndDt(expiredDate);
                
            return code;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Error parsing dates: " + e.getMessage());
        }
    }
}
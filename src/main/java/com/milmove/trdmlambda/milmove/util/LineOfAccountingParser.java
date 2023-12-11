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

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;

@Component
public class LineOfAccountingParser {
    private Logger logger = (Logger) LoggerFactory.getLogger(LineOfAccountingParser.class);
    // Create our map for TGET parsing
    Map<String, Integer> columnNamesAndLocations = new HashMap<>();

    private static final String[] expectedColumnNames = {
            "LOA_SYS_ID", "LOA_DPT_ID", "LOA_TNSFR_DPT_NM", "LOA_BAF_ID", "LOA_TRSY_SFX_TX", "LOA_MAJ_CLM_NM",
            "LOA_OP_AGNCY_ID", "LOA_ALLT_SN_ID", "LOA_PGM_ELMNT_ID", "LOA_TSK_BDGT_SBLN_TX",
            "LOA_DF_AGNCY_ALCTN_RCPNT_ID", "LOA_JB_ORD_NM", "LOA_SBALTMT_RCPNT_ID", "LOA_WK_CNTR_RCPNT_NM",
            "LOA_MAJ_RMBSMT_SRC_ID", "LOA_DTL_RMBSMT_SRC_ID", "LOA_CUST_NM", "LOA_OBJ_CLS_ID", "LOA_SRV_SRC_ID",
            "LOA_SPCL_INTR_ID", "LOA_BDGT_ACNT_CLS_NM", "LOA_DOC_ID", "LOA_CLS_REF_ID", "LOA_INSTL_ACNTG_ACT_ID",
            "LOA_LCL_INSTL_ID", "LOA_FMS_TRNSACTN_ID", "LOA_DSC_TX", "LOA_BGN_DT", "LOA_END_DT", "LOA_FNCT_PRS_NM",
            "LOA_STAT_CD", "LOA_HIST_STAT_CD", "LOA_HS_GDS_CD", "ORG_GRP_DFAS_CD", "LOA_UIC", "LOA_TRNSN_ID",
            "LOA_SUB_ACNT_ID", "LOA_BET_CD", "LOA_FND_TY_FG_CD", "LOA_BGT_LN_ITM_ID", "LOA_SCRTY_COOP_IMPL_AGNC_CD",
            "LOA_SCRTY_COOP_DSGNTR_CD", "LOA_SCRTY_COOP_LN_ITM_ID", "LOA_AGNC_DSBR_CD", "LOA_AGNC_ACNTNG_CD",
            "LOA_FND_CNTR_ID", "LOA_CST_CNTR_ID", "LOA_PRJ_ID", "LOA_ACTVTY_ID", "LOA_CST_CD", "LOA_WRK_ORD_ID",
            "LOA_FNCL_AR_ID", "LOA_SCRTY_COOP_CUST_CD", "LOA_END_FY_TX", "LOA_BG_FY_TX", "LOA_BGT_RSTR_CD",
            "LOA_BGT_SUB_ACT_CD", "ROW_STS_CD" };

    public List<LineOfAccounting> parse(byte[] fileContent, XMLGregorianCalendar trdmLastUpdate)
            throws RuntimeException {
        logger.info("beginning to parse LOA TGET data");
        List<LineOfAccounting> codes = new ArrayList<>();
        Scanner scanner = new Scanner(new ByteArrayInputStream(fileContent));
        logger.info("skipping the first line and then gathering headers");
        String[] columnHeaders = scanner.nextLine().split("\\|"); // Skip first line and gather headers immediately
        logger.info("parsed these column headers from LOA attachment {}", Arrays.toString(columnHeaders));
        // TODO: Possibly allow for unexpected column names and proceed with the columns
        // we are familiar with. This will be a must for LOA.
        if (!Arrays.equals(expectedColumnNames, columnHeaders)) {
            String message = String.format("Column headers do not match expected format. Received %s and expected %s",
                    Arrays.toString(columnHeaders), Arrays.toString(expectedColumnNames));
            throw new RuntimeException(message);
        }

        // Map their order for when processing the LOA values properly
        for (int i = 0; i < columnHeaders.length; i++) {
            columnNamesAndLocations.put(columnHeaders[i], i);
        }

        logger.info("headers received and mapped, beginning to process every other line");
        // Loop until the last line in the file is found
        int lineCount = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineCount++;
            // "Unclassified" will always be the last line in the file
            if (line.equals("Unclassified")) {
                logger.info("finished parsing TGET data file from TRDM");
                break;
            }
            String[] values = line.split("\\|");
            logger.info("Line {} | Values Length: {} | Values: {}", lineCount, values.length, Arrays.toString(values));
            LineOfAccounting code = processLineIntoLOA(values, columnNamesAndLocations, trdmLastUpdate);

            if (code != null) {
                codes.add(code);
            }
        }
        logger.info("finished parsing every single line");

        scanner.close();

        return codes;
    }

    private LineOfAccounting processLineIntoLOA(String[] values, Map<String, Integer> columnHeaders,
            XMLGregorianCalendar lastLoaUpdate)
            throws RuntimeException {
        // Check if LOA is empty or if ROW_STS_CD is "DLT"
        if (values[columnHeaders.get("LOA_SYS_ID")].isEmpty()
                || "DLT".equals(values[columnHeaders.get("ROW_STS_CD")])) {
            return null; // Skip this line
        }

        try {
            LineOfAccounting loa = new LineOfAccounting();
            loa.setLoaSysID(values[columnHeaders.get("LOA_SYS_ID")]);
            loa.setLoaDptID(values[columnHeaders.get("LOA_DPT_ID")]);
            loa.setLoaTnsfrDptNm(values[columnHeaders.get("LOA_TNSFR_DPT_NM")]);
            loa.setLoaBafID(values[columnHeaders.get("LOA_BAF_ID")]);
            loa.setLoaTrsySfxTx(values[columnHeaders.get("LOA_TRSY_SFX_TX")]);
            loa.setLoaMajClmNm(values[columnHeaders.get("LOA_MAJ_CLM_NM")]);
            loa.setLoaOpAgncyID(values[columnHeaders.get("LOA_OP_AGNCY_ID")]);
            loa.setLoaAlltSnID(values[columnHeaders.get("LOA_ALLT_SN_ID")]);
            loa.setLoaPgmElmntID(values[columnHeaders.get("LOA_PGM_ELMNT_ID")]);
            loa.setLoaTskBdgtSblnTx(values[columnHeaders.get("LOA_TSK_BDGT_SBLN_TX")]);
            loa.setLoaDfAgncyAlctnRcpntID(values[columnHeaders.get("LOA_DF_AGNCY_ALCTN_RCPNT_ID")]);
            loa.setLoaJbOrdNm(values[columnHeaders.get("LOA_JB_ORD_NM")]);
            loa.setLoaSbaltmtRcpntID(values[columnHeaders.get("LOA_SBALTMT_RCPNT_ID")]);
            loa.setLoaWkCntrRcpntNm(values[columnHeaders.get("LOA_WK_CNTR_RCPNT_NM")]);
            loa.setLoaMajRmbsmtSrcID(values[columnHeaders.get("LOA_MAJ_RMBSMT_SRC_ID")]);
            loa.setLoaDtlRmbsmtSrcID(values[columnHeaders.get("LOA_DTL_RMBSMT_SRC_ID")]);
            loa.setLoaCustNm(values[columnHeaders.get("LOA_CUST_NM")]);
            loa.setLoaObjClsID(values[columnHeaders.get("LOA_OBJ_CLS_ID")]);
            loa.setLoaSrvSrcID(values[columnHeaders.get("LOA_SRV_SRC_ID")]);
            loa.setLoaSpclIntrID(values[columnHeaders.get("LOA_SPCL_INTR_ID")]);
            loa.setLoaBdgtAcntClsNm(values[columnHeaders.get("LOA_BDGT_ACNT_CLS_NM")]);
            loa.setLoaDocID(values[columnHeaders.get("LOA_DOC_ID")]);
            loa.setLoaClsRefID(values[columnHeaders.get("LOA_CLS_REF_ID")]);
            loa.setLoaInstlAcntgActID(values[columnHeaders.get("LOA_INSTL_ACNTG_ACT_ID")]);
            loa.setLoaLclInstlID(values[columnHeaders.get("LOA_LCL_INSTL_ID")]);
            loa.setLoaFmsTrnsactnID(values[columnHeaders.get("LOA_FMS_TRNSACTN_ID")]);
            loa.setLoaDscTx(values[columnHeaders.get("LOA_DSC_TX")]);
            if (values[columnHeaders.get("LOA_BGN_DT")] != null) {
                LocalDateTime beginDate = LocalDateTime.parse(values[columnHeaders.get("LOA_BGN_DT")],
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                loa.setLoaBgnDt(beginDate);
            }
            if (values[columnHeaders.get("LOA_END_DT")] != null) {
                LocalDateTime endDate = LocalDateTime.parse(values[columnHeaders.get("LOA_END_DT")],
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                loa.setLoaEndDt(endDate);
            }
            loa.setLoaFnctPrsNm(values[columnHeaders.get("LOA_FNCT_PRS_NM")]);
            loa.setLoaStatCd(values[columnHeaders.get("LOA_STAT_CD")]);
            loa.setLoaHistStatCd(values[columnHeaders.get("LOA_HIST_STAT_CD")]);
            loa.setLoaHsGdsCd(values[columnHeaders.get("LOA_HS_GDS_CD")]);
            loa.setOrgGrpDfasCd(values[columnHeaders.get("ORG_GRP_DFAS_CD")]);
            loa.setLoaUic(values[columnHeaders.get("LOA_UIC")]);
            loa.setLoaTrnsnID(values[columnHeaders.get("LOA_TRNSN_ID")]);
            loa.setLoaSubAcntID(values[columnHeaders.get("LOA_SUB_ACNT_ID")]);
            loa.setLoaBetCd(values[columnHeaders.get("LOA_BET_CD")]);
            loa.setLoaFndTyFgCd(values[columnHeaders.get("LOA_FND_TY_FG_CD")]);
            loa.setLoaBgtLnItmID(values[columnHeaders.get("LOA_BGT_LN_ITM_ID")]);
            loa.setLoaScrtyCoopImplAgncCd(values[columnHeaders.get("LOA_SCRTY_COOP_IMPL_AGNC_CD")]);
            loa.setLoaScrtyCoopDsgntrCd(values[columnHeaders.get("LOA_SCRTY_COOP_DSGNTR_CD")]);
            loa.setLoaScrtyCoopLnItmID(values[columnHeaders.get("LOA_SCRTY_COOP_LN_ITM_ID")]);
            loa.setLoaAgncDsbrCd(values[columnHeaders.get("LOA_AGNC_DSBR_CD")]);
            loa.setLoaAgncAcntngCd(values[columnHeaders.get("LOA_AGNC_ACNTNG_CD")]);
            loa.setLoaFndCntrID(values[columnHeaders.get("LOA_FND_CNTR_ID")]);
            loa.setLoaCstCntrID(values[columnHeaders.get("LOA_CST_CNTR_ID")]);
            loa.setLoaPrjID(values[columnHeaders.get("LOA_PRJ_ID")]);
            loa.setLoaActvtyID(values[columnHeaders.get("LOA_ACTVTY_ID")]);
            loa.setLoaCstCd(values[columnHeaders.get("LOA_CST_CD")]);
            loa.setLoaWrkOrdID(values[columnHeaders.get("LOA_WRK_ORD_ID")]);
            loa.setLoaFnclArID(values[columnHeaders.get("LOA_FNCL_AR_ID")]);
            loa.setLoaScrtyCoopCustCd(values[columnHeaders.get("LOA_SCRTY_COOP_CUST_CD")]);
            String loaEndFyTxValue = values[columnHeaders.get("LOA_END_FY_TX")];
            if (!loaEndFyTxValue.equals("")) {
                loa.setLoaEndFyTx(Integer.parseInt(loaEndFyTxValue));
            } else {
                loa.setLoaEndFyTx(null);
            }
            String loaBgFyTxValue = values[columnHeaders.get("LOA_BG_FY_TX")];
            if (!loaBgFyTxValue.equals("")) {
                loa.setLoaBgFyTx(Integer.parseInt(loaBgFyTxValue));
            } else {
                loa.setLoaBgFyTx(null);
            }
            loa.setLoaBgtRstrCd(values[columnHeaders.get("LOA_BGT_RSTR_CD")]);
            loa.setLoaBgtSubActCd(values[columnHeaders.get("LOA_BGT_SUB_ACT_CD")]);
            loa.setUpdatedAt(convertXMLGregorianCalendarToLocalDateTime(lastLoaUpdate));
            return loa;
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

}
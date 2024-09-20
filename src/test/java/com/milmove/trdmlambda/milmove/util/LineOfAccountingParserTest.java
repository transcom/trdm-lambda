package com.milmove.trdmlambda.milmove.util;

import org.junit.jupiter.api.Test;

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

class LineOfAccountingParserTest {

    @Test
    void testLoaParser() throws IOException, DatatypeConfigurationException {
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Line_Of_Accounting.txt"));
        final LineOfAccountingParser lineOfAccountingParser = new LineOfAccountingParser();
            
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        List<LineOfAccounting> result = lineOfAccountingParser.parse(bytes, today);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        LineOfAccounting expectedCode = new LineOfAccounting();
        expectedCode.setLoaSysID("124641");
        expectedCode.setLoaDptID("97");
        expectedCode.setLoaTnsfrDptNm("");
        expectedCode.setLoaBafID("4930");
        expectedCode.setLoaTrsySfxTx("AA37");
        expectedCode.setLoaMajClmNm("");
        expectedCode.setLoaOpAgncyID("6D");
        expectedCode.setLoaAlltSnID("0000");
        expectedCode.setLoaPgmElmntID("MZZF0000");
        expectedCode.setLoaTskBdgtSblnTx("");
        expectedCode.setLoaDfAgncyAlctnRcpntID("");
        expectedCode.setLoaJbOrdNm("");
        expectedCode.setLoaSbaltmtRcpntID("");
        expectedCode.setLoaWkCntrRcpntNm("D0000");
        expectedCode.setLoaMajRmbsmtSrcID("");
        expectedCode.setLoaDtlRmbsmtSrcID("");
        expectedCode.setLoaCustNm("");
        expectedCode.setLoaObjClsID("22NL");
        expectedCode.setLoaSrvSrcID("");
        expectedCode.setLoaSpclIntrID("");
        expectedCode.setLoaBdgtAcntClsNm("MA1MN4");
        expectedCode.setLoaDocID("FRT1MNUSAF7790");
        expectedCode.setLoaClsRefID("");
        expectedCode.setLoaInstlAcntgActID("011000");
        expectedCode.setLoaLclInstlID("");
        expectedCode.setLoaFmsTrnsactnID("");
        expectedCode.setLoaDscTx("CONUS ENTRY");
        expectedCode.setLoaBgnDt(LocalDateTime.of(2006, 10, 1, 0, 0));
        expectedCode.setLoaEndDt(LocalDateTime.of(2007, 9, 30, 0, 0));
        expectedCode.setLoaFnctPrsNm("");
        expectedCode.setLoaStatCd("U");
        expectedCode.setLoaHistStatCd("");
        expectedCode.setLoaHsGdsCd("");
        expectedCode.setOrgGrpDfasCd("DZ");
        expectedCode.setLoaUic("");
        expectedCode.setLoaTrnsnID("");
        expectedCode.setLoaSubAcntID("");
        expectedCode.setLoaBetCd("");
        expectedCode.setLoaFndTyFgCd("");
        expectedCode.setLoaBgtLnItmID("");
        expectedCode.setLoaScrtyCoopImplAgncCd("");
        expectedCode.setLoaScrtyCoopDsgntrCd("");
        expectedCode.setLoaScrtyCoopLnItmID("");
        expectedCode.setLoaAgncDsbrCd("");
        expectedCode.setLoaAgncAcntngCd("");
        expectedCode.setLoaFndCntrID("");
        expectedCode.setLoaCstCntrID("");
        expectedCode.setLoaPrjID("");
        expectedCode.setLoaActvtyID("");
        expectedCode.setLoaCstCd("");
        expectedCode.setLoaWrkOrdID("");
        expectedCode.setLoaFnclArID("");
        expectedCode.setLoaScrtyCoopCustCd("");
        expectedCode.setLoaEndFyTx(null);
        expectedCode.setLoaBgFyTx(null);
        expectedCode.setLoaBgtRstrCd("");
        expectedCode.setLoaBgtSubActCd("");
        expectedCode.setUpdatedAt(convertXMLGregorianCalendarToLocalDateTime(today));
        
        assertEquals(expectedCode, result.get(0));
    }

    // Test to make sure the parser does not fail when recieinvg malformed pipe data and does not add the malformed data to the codes array that will be inserted into the DB
    @Test
    void testLoaParserWithMissingPipeData() throws IOException, DatatypeConfigurationException {
        // This test file has one row that is missing a column
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Line_Of_Accounting_Missing_Pipes.txt"));
        final LineOfAccountingParser lineOfAccountingParser = new LineOfAccountingParser();
            
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        
        List<LineOfAccounting> result = lineOfAccountingParser.parse(bytes, today);

        // The parser in the case of parsing an incomplete pipe row will return the array of codes without the incomplete row
        assertNotNull(result);

        // The Array that is returned is an array of codes. In this case it is empty because the one row in the LOA pipe file was malformed
        assertTrue(result.isEmpty());
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

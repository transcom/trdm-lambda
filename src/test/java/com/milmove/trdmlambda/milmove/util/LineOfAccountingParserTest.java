package com.milmove.trdmlambda.milmove.util;

import org.junit.jupiter.api.Test;

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
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
        expectedCode.setLoaTnsfrDptNm(null);
        expectedCode.setLoaBafID("4930");
        expectedCode.setLoaTrsySfxTx("AA37");
        expectedCode.setLoaMajClmNm(null);
        expectedCode.setLoaOpAgncyID("6D");
        expectedCode.setLoaAlltSnID("0000");
        expectedCode.setLoaPgmElmntID("MZZF0000");
        expectedCode.setLoaTskBdgtSblnTx(null);
        expectedCode.setLoaDfAgncyAlctnRcpntID(null);
        expectedCode.setLoaJbOrdNm(null);
        expectedCode.setLoaSbaltmtRcpntID(null);
        expectedCode.setLoaWkCntrRcpntNm("D0000");
        expectedCode.setLoaMajRmbsmtSrcID(null);
        expectedCode.setLoaDtlRmbsmtSrcID(null);
        expectedCode.setLoaCustNm(null);
        expectedCode.setLoaObjClsID("22NL");
        expectedCode.setLoaSrvSrcID(null);
        expectedCode.setLoaSpclIntrID(null);
        expectedCode.setLoaBdgtAcntClsNm("MA1MN4");
        expectedCode.setLoaDocID("FRT1MNUSAF7790");
        expectedCode.setLoaClsRefID(null);
        expectedCode.setLoaInstlAcntgActID("011000");
        expectedCode.setLoaLclInstlID(null);
        expectedCode.setLoaFmsTrnsactnID(null);
        expectedCode.setLoaDscTx("CONUS ENTRY");
        expectedCode.setLoaBgnDt(LocalDateTime.of(2006, 10, 1, 0, 0));
        expectedCode.setLoaEndDt(LocalDateTime.of(2007, 9, 30, 0, 0));
        expectedCode.setLoaFnctPrsNm(null);
        expectedCode.setLoaStatCd("U");
        expectedCode.setLoaHistStatCd(null);
        expectedCode.setLoaHsGdsCd(null);
        expectedCode.setOrgGrpDfasCd("DZ");
        expectedCode.setLoaUic(null);
        expectedCode.setLoaTrnsnID(null);
        expectedCode.setLoaSubAcntID(null);
        expectedCode.setLoaBetCd(null);
        expectedCode.setLoaFndTyFgCd(null);
        expectedCode.setLoaBgtLnItmID(null);
        expectedCode.setLoaScrtyCoopImplAgncCd(null);
        expectedCode.setLoaScrtyCoopDsgntrCd(null);
        expectedCode.setLoaScrtyCoopLnItmID(null);
        expectedCode.setLoaAgncDsbrCd(null);
        expectedCode.setLoaAgncAcntngCd(null);
        expectedCode.setLoaFndCntrID(null);
        expectedCode.setLoaCstCntrID(null);
        expectedCode.setLoaPrjID(null);
        expectedCode.setLoaActvtyID(null);
        expectedCode.setLoaCstCd(null);
        expectedCode.setLoaWrkOrdID(null);
        expectedCode.setLoaFnclArID(null);
        expectedCode.setLoaScrtyCoopCustCd(null);
        expectedCode.setLoaEndFyTx(null);
        expectedCode.setLoaBgFyTx(null);
        expectedCode.setLoaBgtRstrCd(null);
        expectedCode.setLoaBgtSubActCd(null);
        expectedCode.setUpdatedAt(convertXMLGregorianCalendarToLocalDateTime(today));

        assertEquals(expectedCode, result.get(0));
    }

    // Test to make sure the parser does not fail when receiving malformed pipe data
    // and does not add the malformed data to the codes array that will be inserted
    // into the DB
    @Test
    void testLoaParserWithMissingPipeData() throws IOException, DatatypeConfigurationException {
        // This test file has one row that is missing a column
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Line_Of_Accounting_Missing_Pipes.txt"));
        final LineOfAccountingParser lineOfAccountingParser = new LineOfAccountingParser();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        List<LineOfAccounting> result = lineOfAccountingParser.parse(bytes, today);

        // The parser in the case of parsing an incomplete pipe row will return the
        // array of codes without the incomplete row
        assertNotNull(result);

        // The Array that is returned is an array of codes. In this case it is empty
        // because the one row in the LOA pipe file was malformed
        assertTrue(result.isEmpty());
    }

    // Test that when the delimited value is empty spaces or if it has trailing
    // spaces that it will be omitted
    @Test
    void testLoaParserWithTrailingSpaces() throws IOException, DatatypeConfigurationException, IllegalArgumentException, IllegalAccessException {
        // This test file has one row that is missing a column
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Line_Of_Accounting_Trailing_Spaces.txt"));
        final LineOfAccountingParser lineOfAccountingParser = new LineOfAccountingParser();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        List<LineOfAccounting> result = lineOfAccountingParser.parse(bytes, today);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 3);

        // Check no field has empty strings or trailing spaces
        for (LineOfAccounting loa : result) {
            // Reflect
            for (Field field : loa.getClass().getDeclaredFields()) {
                if (field.getType().equals(String.class)) {
                    field.setAccessible(true); // Set field public for testing purposes
                    String value = (String) field.get(loa);
                    if (value != null) {
                        assertFalse(value.isEmpty(), "Field " + field.getName() + " should not be empty");
                        assertEquals(value.trim(), value,
                                "Field " + field.getName() + " should not have trailing spaces");
                    }
                }
            }
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

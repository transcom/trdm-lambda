package com.milmove.trdmlambda.milmove.util;


import org.junit.jupiter.api.Test;

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;

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

class TransportationAccountingCodeParserTest {

    @Test
    void testTacParser() throws IOException, DatatypeConfigurationException {
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Transportation_Account.txt"));
        TransportationAccountingCodeParser parser = new TransportationAccountingCodeParser();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        List<TransportationAccountingCode> result = parser.parse(bytes, today);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        TransportationAccountingCode expectedCode = new TransportationAccountingCode();
        expectedCode.setTacSysID("1234567884");
        expectedCode.setLoaSysID("12345678");
        expectedCode.setTac("0003");
        expectedCode.setTacFyTxt("2022");
        expectedCode.setTacFnBlModCd("3");
        expectedCode.setOrgGrpDfasCd("DF");
        expectedCode.setTacTyCd("O");
        expectedCode.setTacUseCd("O");
        expectedCode.setTacMajClmtID("USTC");
        expectedCode.setTacCostCtrNm("G31M32");
        expectedCode.setTacStatCd("I");
        expectedCode.setTrnsprtnAcntTx("FOR MOVEMENT TEST 1");
        expectedCode.setTrnsprtnAcntBgnDt(LocalDateTime.parse("2021-10-01T00:00:00"));
        expectedCode.setTrnsprtnAcntEndDt(LocalDateTime.parse("2022-09-30T00:00:00"));
        expectedCode.setDdActvtyAdrsID("F55555");
        expectedCode.setTacBlldAddFrstLnTx("FIRST LINE");
        expectedCode.setTacBlldAddScndLnTx("SECOND LINE");
        expectedCode.setTacBlldAddThrdLnTx("THIRD LINE");
        expectedCode.setTacBlldAddFrthLnTx("FOURTH LINE");
        expectedCode.setTacFnctPocNm("Contact Person Here");
        expectedCode.setTacMvtDsgID(null);
        expectedCode.setTacBillActTxt(null);
        expectedCode.setBuic(null);
        expectedCode.setTacHistCd(null);
        expectedCode.setUpdatedAt(convertXMLGregorianCalendarToLocalDateTime(today));

        assertEquals(expectedCode, result.get(0));
    }

     // Test to make sure the parser does not fail when receiving malformed pipe data and does not add the malformed data to the codes array that will be inserted into the DB
    @Test
    void testTacParserWithMissingPipeData() throws IOException, DatatypeConfigurationException {
        // This test file has one row that is missing a column
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Transportation_Account_Missing_Pipes.txt"));

        TransportationAccountingCodeParser parser = new TransportationAccountingCodeParser();
            
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        List<TransportationAccountingCode> result = parser.parse(bytes, today);

        // The parser in the case of parsing an incomplete pipe row will return the array of codes without the incomplete row
        assertNotNull(result);

        // The Array that is returned is an array of codes. In this case it is empty because the one row in the TAC pipe file was malformed
        assertTrue(result.isEmpty());
    }

    // Test that when the delimited value is empty spaces or if it has trailing
    // spaces that it will be omitted
    @Test
    void testTacParserWithTrailingSpaces() throws IOException, DatatypeConfigurationException, IllegalArgumentException, IllegalAccessException {
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Transportation_Account.txt"));
        TransportationAccountingCodeParser parser = new TransportationAccountingCodeParser();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        XMLGregorianCalendar today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        List<TransportationAccountingCode> result = parser.parse(bytes, today);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Check no field has empty strings or trailing spaces
        for (TransportationAccountingCode tac : result) {
            // Reflect
            for (Field field : tac.getClass().getDeclaredFields()) {
                if (field.getType().equals(String.class)) {
                    field.setAccessible(true); // Set field public for testing purposes
                    String value = (String) field.get(tac);
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

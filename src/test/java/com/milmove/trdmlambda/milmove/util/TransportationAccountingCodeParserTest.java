package com.milmove.trdmlambda.milmove.util;

import org.junit.jupiter.api.Test;

import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

class TransportationAccountingCodeParserTest {

    @Test
    void testTacParser() throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/Transportation_Account.txt"));
        TransportationAccountingCodeParser parser = new TransportationAccountingCodeParser();

        List<TransportationAccountingCode> result = parser.parse(bytes);

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
        expectedCode.setTacMvtDsgID("");
        expectedCode.setTacBillActTxt("");
        expectedCode.setBuic("");
        expectedCode.setTacHistCd("");

        assertEquals(expectedCode, result.get(0));
    }
}

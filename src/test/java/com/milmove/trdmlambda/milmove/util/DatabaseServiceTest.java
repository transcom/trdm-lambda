package com.milmove.trdmlambda.milmove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;
import com.milmove.trdmlambda.milmove.service.DatabaseService;

@ExtendWith(MockitoExtension.class)
public class DatabaseServiceTest {

    final String testDbUrl = System.getenv("TEST_DB_URL");

    public DatabaseService spyDatabaseService;
    public Connection testDbConn;

    public Connection createTestDbConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(testDbUrl);
        return conn;
    }

    void setUpTests() throws SQLException {
        SecretFetcher mockSeceretFetcher = mock(SecretFetcher.class);
        when(mockSeceretFetcher.getSecret("rds_hostname")).thenReturn(System.getenv("TEST_DB_HOST"));
        when(mockSeceretFetcher.getSecret("rds_port")).thenReturn(System.getenv("TEST_DB_PORT"));
        when(mockSeceretFetcher.getSecret("rds_db_name")).thenReturn(System.getenv("TEST_DB_NAME"));
        when(mockSeceretFetcher.getSecret("rds_username")).thenReturn(System.getenv("TEST_DB_USER"));
        DatabaseService databaseService = new DatabaseService(mockSeceretFetcher);
        spyDatabaseService = spy(databaseService);

        // Create test_db connection
        testDbConn = DriverManager.getConnection(testDbUrl);

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(testDbConn).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(testDbConn, spyDatabaseService.getConnection());
    }

    @Test // Test that we can retrieve all LOA codes from database table
          // line_of_accounting
    void getAllLoasTest() throws SQLException {
        setUpTests();
        ArrayList<LineOfAccounting> loas = spyDatabaseService.getCurrentLoaInformation();
        assertTrue(loas.size() > 0);
    }

    @Test // Test that we can retrieve all TAC codes from database table
          // transportation_accounting_codes
    void getAllTacsTest() throws Exception {
        setUpTests();
        ArrayList<TransportationAccountingCode> tacs = spyDatabaseService.getCurrentTacInformation();
        assertTrue(tacs.size() > 0);
    }

    // Test that we can insert LOA(s) using
    // DatabaseService.insertLinesOfAccounting()
    @Test
    void testInsertLinesOfAccounting() throws Exception {
        setUpTests();

        // Create mock loas for test
        ArrayList<LineOfAccounting> testLoas = createMockLoas(1);

        // Invoke insertLinesOfAccounting() with test TAC(s)
        spyDatabaseService.insertLinesOfAccounting(testLoas);

        // Verify test LOAs made it to the test_db
        try {
            // Select the LOA record with the UUID added through invoking
            // spyDatabaseService.insertLinesOfAccounting(testLoas)
            String sql = "select * from lines_of_accounting where id= ?";

            Connection conn = DriverManager.getConnection(testDbUrl);

            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setObject(1, testLoas.get(0).getId());

                ResultSet rs = pstmt.executeQuery();

                String uuid = "";
                if (rs.next()) {
                    uuid = rs.getString("id");
                }

                // Verify the UUIDs match to prove a successful insert using
                // insertLinesOfAccounting()
                assertEquals(UUID.fromString(uuid), testLoas.get(0).getId());
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("Database connection error to test_db");
            System.out.println(e);
        }
    }

    // Test that we can update existing LOA(s) using
    // DatabaseService.updateLinesOfAccountingCodes()
    @Test
    void testUpdateLinesOfAccountingCodes() throws Exception {

        setUpTests();

        // Create a list of TAC(s)
        List<LineOfAccounting> testLoas = createMockLoas(2);
        testLoas.get(0).setLoaSysID("PENDING_UPDATE");
        testLoas.get(0).setLoaDptID("01");
        testLoas.get(1).setLoaSysID("PENDING_UPDATE2");
        testLoas.get(1).setLoaDptID("02");

        // Invoke insertLinesOfAccounting() with test LOA(s)
        spyDatabaseService.insertLinesOfAccounting(testLoas);

        // Create a list of LOA(s)
        List<LineOfAccounting> testLoas2 = testLoas;
        testLoas2.get(0).setLoaSysID("PENDING_UPDATE");
        testLoas2.get(0).setLoaDptID("03");
        testLoas2.get(1).setLoaSysID("PENDING_UPDATE");
        testLoas2.get(1).setLoaDptID("04");

        Connection conn1 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn1).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn1, spyDatabaseService.getConnection());

        // Update TAC Codes
        spyDatabaseService.updateLinesOfAccountingCodes(testLoas2);

        Connection conn2 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn2).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn2, spyDatabaseService.getConnection());

        ArrayList<LineOfAccounting> dbTacs = spyDatabaseService.getCurrentLoaInformation();

        List<LineOfAccounting> codes = dbTacs.stream().filter(tac -> tac.getId().equals(testLoas.get(0).getId()))
                .collect(Collectors.toList());

        assertEquals("03", codes.get(0).getLoaDptID());

    }

    // Test that we can insert TAC codes using
    // DatabaseService.insertTransportationAccountingCodes()
    @Test
    void testInsertTransportationAccountingCodes() throws Exception {
        setUpTests();

        // Create a list of TAC(s)
        List<TransportationAccountingCode> testTacs = createMockTacs(1);

        // Invoke insertTransportationAccountingCodes() with test TAC(s)
        spyDatabaseService.insertTransportationAccountingCodes(testTacs);

        // Verify test TAC(s) made it to the test_db
        try {
            // Select the TAC record with the UUID added through invoking
            spyDatabaseService.insertTransportationAccountingCodes(testTacs);
            String sql = "select * from transportation_accounting_codes where id= ?";

            Connection conn = DriverManager.getConnection(testDbUrl);

            if (conn != null) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setObject(1, testTacs.get(0).getId());

                ResultSet rs = pstmt.executeQuery();

                String uuid = "";
                if (rs.next()) {
                    uuid = rs.getString("id");
                }

                // Verify the UUIDs match to prove a successful insert using
                assertEquals(UUID.fromString(uuid), testTacs.get(0).getId());
                conn.close();
            }

        } catch (Exception e) {
            System.out.println("Database connection error to test_db");
            System.out.println(e);
        }
    }

    // Test that we can update existing TAC(s) using
    // DatabaseService.updateTransportationAccountingCodes()
    @Test
    void testUpdateTransportationAccountingCodes() throws Exception {

        setUpTests();

        // Create a list of TAC(s)
        List<TransportationAccountingCode> testTacs = createMockTacs(2);
        testTacs.get(0).setTacSysID("PENDING_UPDATE");
        testTacs.get(0).setTac("2222");
        testTacs.get(1).setTacSysID("PENDING_UPDATE2");
        testTacs.get(1).setTac("3333");

        // Invoke insertTransportationAccountingCodes() with test TAC(s)
        spyDatabaseService.insertTransportationAccountingCodes(testTacs);

        // Create a list of TAC(s)
        List<TransportationAccountingCode> testTacs2 = testTacs;
        testTacs2.get(0).setTacSysID("PENDING_UPDATE");
        testTacs2.get(0).setTac("4445");
        testTacs2.get(1).setTacSysID("PENDING_UPDATE");
        testTacs2.get(1).setTac("6667");

        Connection conn1 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn1).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn1, spyDatabaseService.getConnection());

        // Update TAC Codes
        spyDatabaseService.updateTransportationAccountingCodes(testTacs2);

        Connection conn2 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn2).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn2, spyDatabaseService.getConnection());

        ArrayList<TransportationAccountingCode> dbTacs = spyDatabaseService.getCurrentTacInformation();

        List<TransportationAccountingCode> codes = dbTacs.stream()
                .filter(tac -> tac.getId().equals(testTacs.get(0).getId())).collect(Collectors.toList());

        assertEquals(testTacs.get(0).getTac(), codes.get(0).getTac());
    }

     // Test that we get a list of loa_sys_ids that occur more than once
    // DatabaseService.getLoaSysIdCountGreaterThan1()
    @Test
    void testGetLoaSysIdCountGreaterThan1() throws Exception {

        setUpTests();

        LocalDateTime time = LocalDateTime.now();
        int mo = time.getMonthValue();
        int day = time.getDayOfMonth();
        int year = time.getYear();
        int hr = time.getHour();
        int min = time.getMinute();
        int sec = time.getSecond();

        String dayTime = Integer.toString(mo) + Integer.toString(day) + Integer.toString(year) + Integer.toString(hr) + Integer.toString(hr) + Integer.toString(min) + Integer.toString(sec);

        String testLoaSysId = "dum" + dayTime;
        String nonDupLoaSysId = testLoaSysId + "a";

        ArrayList<LineOfAccounting> testLoas = createMockLoas(3);

        testLoas.get(0).setLoaSysID(testLoaSysId);
        testLoas.get(1).setLoaSysID(testLoaSysId);
        testLoas.get(2).setLoaSysID(nonDupLoaSysId);

        // Invoke insertLineOfAccountingCodes() with test LOA(s)
        spyDatabaseService.insertLinesOfAccounting(testLoas);

        Connection conn1 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn1).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn1, spyDatabaseService.getConnection());

        ArrayList<String> loaSysIds = spyDatabaseService.getLoaSysIdCountGreaterThan1();

        assertTrue(loaSysIds.contains(testLoaSysId));
        assertFalse(loaSysIds.contains(nonDupLoaSysId));
    }

    // Test that we can delete a list of loas
    // DatabaseService.deleteLoas()
    @Test
    void testDeleteLoas() throws Exception {
        
        setUpTests();

        // Create mock loas for test
        ArrayList<LineOfAccounting> testLoas = createMockLoas(3);
        testLoas.get(0).setLoaSysID("DELETE1");
        testLoas.get(1).setLoaSysID("DELETE2");
        testLoas.get(2).setLoaSysID("DELETE3");

        // Invoke insertLinesOfAccounting() with test TAC(s)
        spyDatabaseService.insertLinesOfAccounting(testLoas);

        Connection conn1 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn1).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn1, spyDatabaseService.getConnection());

        spyDatabaseService.deleteLoas(testLoas);

        Connection conn2 = createTestDbConnection();

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(conn2).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(conn2, spyDatabaseService.getConnection());

        ArrayList<LineOfAccounting> loasList = spyDatabaseService.getCurrentLoaInformation();
        List<UUID> loaIds = loasList.stream().map(loa -> loa.getId()).collect(Collectors.toList());

        assertFalse(loaIds.contains(testLoas.get(0).getId()));
        assertFalse(loaIds.contains(testLoas.get(1).getId()));
        assertFalse(loaIds.contains(testLoas.get(2).getId()));
    }

    // TEST HELPER FUNCTIONS

    // Create mock loas
    public ArrayList<LineOfAccounting> createMockLoas(int amount) {
        ArrayList<LineOfAccounting> mockLoas = new ArrayList<LineOfAccounting>();

        for (int index = 0; index != amount; index++) {
            LineOfAccounting mockLoa = createMockLoa();
            mockLoas.add(mockLoa);
        }

        return mockLoas;
    }

    // Create a mock loa
    public LineOfAccounting createMockLoa() {
        LineOfAccounting mockLoa = new LineOfAccounting();

        UUID testUUID = UUID.randomUUID();

        mockLoa.setId(testUUID);
        mockLoa.setLoaSysID("TESTFAKE");
        mockLoa.setLoaDptID("1");
        mockLoa.setLoaTnsfrDptNm("0000");
        mockLoa.setLoaBafID("0000");
        mockLoa.setLoaTrsySfxTx("0000");
        mockLoa.setLoaMajClmNm("0000");
        mockLoa.setLoaOpAgncyID("1A");
        mockLoa.setLoaAlltSnID("123A");
        mockLoa.setLoaPgmElmntID("00000000");
        mockLoa.setLoaTskBdgtSblnTx("00000000");
        mockLoa.setLoaDfAgncyAlctnRcpntID("0000");
        mockLoa.setLoaJbOrdNm("T");
        mockLoa.setLoaSbaltmtRcpntID("A");
        mockLoa.setLoaWkCntrRcpntNm("000000");
        mockLoa.setLoaMajRmbsmtSrcID("I");
        mockLoa.setLoaDtlRmbsmtSrcID("000");
        mockLoa.setLoaCustNm("000000");
        mockLoa.setLoaObjClsID("22NL");
        mockLoa.setLoaSrvSrcID("A");
        mockLoa.setLoaSpclIntrID("A");
        mockLoa.setLoaBdgtAcntClsNm("00000000");
        mockLoa.setLoaDocID("HHG12345678900");
        mockLoa.setLoaLclInstlID("000000000000000000");
        mockLoa.setLoaFmsTrnsactnID("000000000000");
        mockLoa.setLoaDscTx("PERSONAL PROPERTY - FAKE DATA DIVISION");
        mockLoa.setLoaEndDt(LocalDateTime.now());
        mockLoa.setLoaBgnDt(LocalDateTime.now());
        mockLoa.setLoaFnctPrsNm("0000");
        mockLoa.setLoaStatCd("U");
        mockLoa.setLoaHistStatCd("A");
        mockLoa.setLoaHsGdsCd("HC");
        mockLoa.setOrgGrpDfasCd("ZZ");
        mockLoa.setLoaUic("test");
        mockLoa.setLoaTrnsnID("C11");
        mockLoa.setLoaSubAcntID("A");
        mockLoa.setLoaBetCd("A");
        mockLoa.setLoaFndTyFgCd("A");
        mockLoa.setLoaBgtLnItmID("00000000");
        mockLoa.setLoaScrtyCoopImplAgncCd("A");
        mockLoa.setLoaScrtyCoopDsgntrCd("0000");
        mockLoa.setLoaScrtyCoopLnItmID("000");
        mockLoa.setLoaAgncDsbrCd("A");
        mockLoa.setLoaAgncAcntngCd("A");
        mockLoa.setLoaFndCntrID("000000000000");
        mockLoa.setLoaCstCntrID("0000000000000000");
        mockLoa.setLoaPrjID("000000000000");
        mockLoa.setLoaActvtyID("00000000000");
        mockLoa.setLoaCstCd("0000000000000000");
        mockLoa.setLoaWrkOrdID("0000000000000000");
        mockLoa.setLoaFnclArID("000000");
        mockLoa.setLoaScrtyCoopCustCd("A");
        mockLoa.setLoaEndFyTx(0);
        mockLoa.setLoaBgFyTx(0);
        mockLoa.setLoaBgtRstrCd("A");
        mockLoa.setLoaBgtSubActCd("A");
        mockLoa.setUpdatedAt(LocalDateTime.now());

        return mockLoa;
    }

    // Create mock tacs
    public ArrayList<TransportationAccountingCode> createMockTacs(int amount) {
        ArrayList<TransportationAccountingCode> mockTacs = new ArrayList<TransportationAccountingCode>();

        for (int index = 0; index != amount; index++) {
            TransportationAccountingCode mockTac = createMockTac();
            mockTacs.add(mockTac);
        }

        return mockTacs;
    }

    // Create a mock tac
    public TransportationAccountingCode createMockTac() {
        // Create a test TAC(s)
        TransportationAccountingCode mockTac = new TransportationAccountingCode();

        UUID testUUID = UUID.randomUUID();

        mockTac.setId(testUUID);
        mockTac.setTac("0000");
        mockTac.setTacSysID("20000");
        mockTac.setLoaSysID("10002");
        mockTac.setTacSysID("2017");
        mockTac.setTacFnBlModCd("W");
        mockTac.setOrgGrpDfasCd("HS");
        mockTac.setTacMvtDsgID("test");
        mockTac.setTacTyCd("O");
        mockTac.setTacUseCd("N");
        mockTac.setTacMajClmtID("12345");
        mockTac.setTacBillActTxt("123456");
        mockTac.setTacCostCtrNm("12345");
        mockTac.setBuic("A");
        mockTac.setTacHistCd("A");
        mockTac.setTacStatCd("I");
        mockTac.setTrnsprtnAcntTx("TEST HOUSING 1");
        mockTac.setDdActvtyAdrsID("test");
        mockTac.setTacBlldAddFrstLnTx("test");
        mockTac.setTacBlldAddScndLnTx("test");
        mockTac.setTacBlldAddThrdLnTx("test");
        mockTac.setTacBlldAddFrthLnTx("test");
        mockTac.setTacFnctPocNm("test");
        mockTac.setTrnsprtnAcntBgnDt(LocalDateTime.now());
        mockTac.setTrnsprtnAcntEndDt(LocalDateTime.now());
        mockTac.setUpdatedAt(LocalDateTime.now());

        return mockTac;
    }
}

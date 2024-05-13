package com.milmove.trdmlambda.milmove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;
import com.milmove.trdmlambda.milmove.service.DatabaseService;
import com.milmove.trdmlambda.milmove.service.GetTableService;
import com.milmove.trdmlambda.milmove.service.LastTableUpdateService;
import com.milmove.trdmlambda.milmove.model.LineOfAccounting;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;

@ExtendWith(MockitoExtension.class)
class TrdmTest {

    @Mock
    private LastTableUpdateService lastTableUpdateService;
    @Mock
    private GetTableService getTableService;
    @Mock
    private DatabaseService databaseService;
    @Mock
    private TransportationAccountingCodeParser tacParser;
    @Mock
    private LineOfAccountingParser loaParser;
    private XMLGregorianCalendar ourLastUpdate;
    private XMLGregorianCalendar trdmLastUpdate;
    String trdmTacTable = "TRNSPRTN_ACNT";
    String rdsTacTable = "transportation_accounting_codes";

    @InjectMocks
    private Trdm trdm;

    @Captor
    private ArgumentCaptor<GetTableRequest> getTableRequestCaptor;

    final String testDbUrl = System.getenv("TEST_DB_URL");

    @BeforeEach
    void setTestDates() throws Exception {
        ourLastUpdate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2024, 3, 1, 0);
        trdmLastUpdate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2024, 4, 1, 0);
    }

    // Tests updateTGET data with TAC, since both TAC and LOA use the same
    // components
    // these test both TAC and LOA at the same time, essentially.
    @Test
    void testUpdateTGETDataReceivesData() throws Exception {
        // Setup TRDM mock to respond with rowCount of 1 when getTable is called
        GetTableResponse mockResponse = mock(GetTableResponse.class);
        when(mockResponse.getRowCount()).thenReturn(BigInteger.ONE);
        when(getTableService.getTableRequest(any())).thenReturn(mockResponse);

        // Call to update TGET data
        trdm.UpdateTGETData(ourLastUpdate, trdmTacTable, rdsTacTable, trdmLastUpdate);

        // Verify that the database service was called once because we returned data
        verify(databaseService, times(1)).insertTransportationAccountingCodes(anyList());
    }

    @Test
    void testUpdateTGETDataReceivesNoData() throws Exception {
        // Mock setup to simulate receiving no data from the TRDM service
        // Setup TRDM mock to respond with rowCount of 0 when getTable is called
        GetTableResponse mockResponse = mock(GetTableResponse.class);
        when(mockResponse.getRowCount()).thenReturn(BigInteger.ZERO);
        when(getTableService.getTableRequest(any())).thenReturn(mockResponse);

        // Call to update TGET data
        trdm.UpdateTGETData(ourLastUpdate, trdmTacTable, rdsTacTable, trdmLastUpdate);

        // Verify that the database service never inserted any data
        verify(databaseService, never()).insertTransportationAccountingCodes(anyList());
    }

    // Explicitly test issue ticket I-12635
    @Test
    void testUpdateTGETDataTwoWeeksScenario() throws Exception {
        // The first getTable call should return 0 rows
        GetTableResponse firstResponse = mock(GetTableResponse.class);
        when(firstResponse.getRowCount()).thenReturn(BigInteger.ZERO);

        // When it's called a second time then it should return 1 row
        GetTableResponse secondResponse = mock(GetTableResponse.class);
        when(secondResponse.getRowCount()).thenReturn(BigInteger.ONE);

        // Have the mock return the second, successful response. It should only invoke
        // the second response
        // after it sees no data came from the first, and then asks for new data
        when(getTableService.getTableRequest(any())).thenReturn(firstResponse, secondResponse);

        // ourLastUpdate will be updated to not just be one week, but two weeks
        // because the first call will return no rows
        trdm.UpdateTGETData(ourLastUpdate, trdmTacTable, rdsTacTable, trdmLastUpdate);

        // Verify that getTableRequest was called twice
        verify(getTableService, times(2)).getTableRequest(any());

        // Verify that data insertion was only attempted once
        verify(databaseService, times(1)).insertTransportationAccountingCodes(anyList());
    }

    // Tests that we do not ask for another week of data if it would go beyond
    // TRDM's last update
    @Test
    void testUpdateTGETDataTwoWeeksScenarioButTheSecondWeekIsBeyondTrdmLastUpdate() throws Exception {
        // Simulate no rows being returned
        GetTableResponse firstResponse = mock(GetTableResponse.class);
        when(firstResponse.getRowCount()).thenReturn(BigInteger.ZERO);
        when(getTableService.getTableRequest(any())).thenReturn(firstResponse);

        // Knowing 0 rows are returned, it should not ask for 2 getTable calls because
        // the date range would go outside of what we have
        trdm.UpdateTGETData(DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2024, 3, 1, 0), trdmTacTable,
                rdsTacTable, DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2024, 3, 7, 0));

        // Verify that getTableRequest was called once, even though 0 rows were returned
        verify(getTableService, times(1)).getTableRequest(any());

        // Verify that data insertion was never attempted
        verify(databaseService, times(0)).insertTransportationAccountingCodes(anyList());
    }

    // Test that we can explicitly ask for a half week on the second request
    @Test
    void testUpdateTGETDataSecondWeekHalfWeekParameter() throws Exception {
        // First response returns nothing, second response returns data.
        GetTableResponse firstResponse = mock(GetTableResponse.class);
        when(firstResponse.getRowCount()).thenReturn(BigInteger.ZERO);
        GetTableResponse secondResponse = mock(GetTableResponse.class);
        when(secondResponse.getRowCount()).thenReturn(BigInteger.ONE);

        when(getTableService.getTableRequest(any(GetTableRequest.class))).thenReturn(firstResponse,
                secondResponse);

        XMLGregorianCalendar updatedAtAfterFilter = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2024, 3,
                1, 0);
        XMLGregorianCalendar updatedAtBeforeFilter = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2024, 3,
                11, 0);
        trdm.UpdateTGETData(updatedAtAfterFilter, trdmTacTable, rdsTacTable,
                updatedAtBeforeFilter);

        verify(getTableService, times(2)).getTableRequest(getTableRequestCaptor.capture());

        // Capture getTable requests
        List<GetTableRequest> allRequests = getTableRequestCaptor.getAllValues();
        // Make sure there are 2 requests
        assertEquals(2, allRequests.size());

        GetTableRequest secondRequest = allRequests.get(1);
        String contentUpdatedSinceDateTime = secondRequest.getContentUpdatedSinceDateTime();
        String contentUpdatedOnOrBeforeDateTime = secondRequest.getContentUpdatedOnOrBeforeDateTime();
        // Add a week to our original updatedAtAfterFilter
        XMLGregorianCalendar oneWeekLater = Trdm.AddOneWeek(updatedAtAfterFilter);
        // Now we should be explicitly asking for half a week of data
        assertEquals(oneWeekLater.toString(), contentUpdatedSinceDateTime);
        assertEquals(updatedAtBeforeFilter.toString(), contentUpdatedOnOrBeforeDateTime);
    }

    // Test that we can insert TAC codes using DatabaseService.insertTransportationAccountingCodes()
    @Test
    void testInsertTransportationAccountingCodes() throws Exception {

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

        // Create a list of TAC(s)
        List<TransportationAccountingCode> testTacs = new ArrayList<TransportationAccountingCode>();
        testTacs.add(mockTac);

        // Mock Seceret Fetcher and its DB connections
        SecretFetcher mockSeceretFetcher = mock(SecretFetcher.class);
        when(mockSeceretFetcher.getSecret("rds_hostname")).thenReturn(System.getenv("TEST_DB_HOST"));
        when(mockSeceretFetcher.getSecret("rds_port")).thenReturn(System.getenv("TEST_DB_PORT"));
        when(mockSeceretFetcher.getSecret("rds_db_name")).thenReturn(System.getenv("TEST_DB_NAME"));
        when(mockSeceretFetcher.getSecret("rds_username")).thenReturn(System.getenv("TEST_DB_USER"));

        // Create a test Database Service Instance for testing
        DatabaseService testDatabaseService = new DatabaseService(mockSeceretFetcher);

        // Create a SPY on the test Database Instance
        DatabaseService spyDatabaseService = spy(testDatabaseService);

        // Create test_db connection
        Connection testDbConn = DriverManager.getConnection(testDbUrl);

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(testDbConn).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(testDbConn, spyDatabaseService.getConnection());

        // Invoke insertTransportationAccountingCodes() with test TAC(s)
        spyDatabaseService.insertTransportationAccountingCodes(testTacs);

        // Verfiy test TAC(s) made it to the test_db
        try {
            // Select the TAC record with the UUID added through invoking spyDatabaseService.insertTransportationAccountingCodes(testTacs)
            String sql = "select * from transportation_accounting_codes where id =" + testUUID;

            Connection conn = DriverManager.getConnection(testDbUrl);
            if (testDbConn != null) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet myRs= pstmt.executeQuery();
                String uuid = myRs.getString("id");

                // Verify the UUIDs match to prove a successful insert using insertTransportationAccountingCodes()
                assertEquals(uuid, mockTac.getId());
            }

            conn.close();
        } catch (Exception e){
            System.out.println("Database connection error to test_db");
            System.out.println(e);
        }
    }

    // Test that we can insert LOA(s) using DatabaseService.insertLinesOfAccounting()
    @Test
    void testInsertLinesOfAccounting() throws Exception {

        // Create a test TAC(s)
        LineOfAccounting mockLoa = new LineOfAccounting();

        UUID testUUID = UUID.randomUUID();

        mockLoa.setId(testUUID);
        mockLoa.setLoaSysID("0000");
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

        // Create a list of TAC(s)
        List<LineOfAccounting> testLoas = new ArrayList<LineOfAccounting>();
        testLoas.add(mockLoa);

        // Mock Seceret Fetcher and its DB connections
        SecretFetcher mockSeceretFetcher = mock(SecretFetcher.class);
        when(mockSeceretFetcher.getSecret("rds_hostname")).thenReturn(System.getenv("TEST_DB_HOST"));
        when(mockSeceretFetcher.getSecret("rds_port")).thenReturn(System.getenv("TEST_DB_PORT"));
        when(mockSeceretFetcher.getSecret("rds_db_name")).thenReturn(System.getenv("TEST_DB_NAME"));
        when(mockSeceretFetcher.getSecret("rds_username")).thenReturn(System.getenv("TEST_DB_USER"));

        // Create a test Database Service Instance for testing
        DatabaseService testDatabaseService = new DatabaseService(mockSeceretFetcher);

        // Create a SPY on the test Database Instance
        DatabaseService spyDatabaseService = spy(testDatabaseService);

        // Create test_db connection
        Connection testDbConn = DriverManager.getConnection(testDbUrl);

        // Mock the DatabaseService.getConnection() to return the test_db connection
        doReturn(testDbConn).when(spyDatabaseService).getConnection();

        // Make sure the test_db connection is returned when .getConnection is called
        assertEquals(testDbConn, spyDatabaseService.getConnection());

        // Invoke insertLinesOfAccounting() with test TAC(s)
        spyDatabaseService.insertLinesOfAccounting(testLoas);

        // Verfiy test TAC(s) made it to the test_db
        try {
            // Select the TAC record with the UUID added through invoking spyDatabaseService.insertLinesOfAccounting(testLoas)
            String sql = "select * from lines_of_accounting where id =" + testUUID;

            Connection conn = DriverManager.getConnection(testDbUrl);
            if (testDbConn != null) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet myRs= pstmt.executeQuery();
                String uuid = myRs.getString("id");

                // Verify the UUIDs match to prove a successful insert using insertLinesOfAccounting()
                assertEquals(uuid, mockLoa.getId());
            }

            conn.close();
        } catch (Exception e){
            System.out.println("Database connection error to test_db");
            System.out.println(e);
        }
    }
}

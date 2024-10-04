package com.milmove.trdmlambda.milmove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.sql.SQLException;
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
    private SecretFetcher secretFetcher;
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

    @Test // Test that we can identify TACs needing to be updated
    void identifyTacsToUpdateTest() throws SQLException {
        ArrayList<TransportationAccountingCode> newTacs = createMockTacs(3);
        newTacs.get(0).setTacSysID("TAC1");
        newTacs.get(1).setTacSysID("TAC2");
        newTacs.get(2).setTacSysID("TAC4");

        ArrayList<TransportationAccountingCode> currentTacs = createMockTacs(3);
        currentTacs.get(0).setTacSysID("TAC1");
        currentTacs.get(1).setTacSysID("TAC3");
        currentTacs.get(2).setTacSysID("TAC5");

        List<TransportationAccountingCode> updateTacs = trdm.identifyTacsToUpdate(newTacs, currentTacs);
      
        assertEquals(updateTacs.size(), 1);
        assertEquals(updateTacs.get(0).getTacSysID(), newTacs.get(0).getTacSysID());
    }

    @Test // Test that we can identify TACs needing to be created
    void identifyTacsToCreateTest() throws SQLException {
        ArrayList<TransportationAccountingCode> newTacs = createMockTacs(3);
        newTacs.get(0).setTacSysID("TAC1");
        newTacs.get(1).setTacSysID("TAC2");
        newTacs.get(2).setTacSysID("TAC3");

        ArrayList<TransportationAccountingCode> updatedTacs = createMockTacs(3);
        updatedTacs.get(0).setTacSysID("TAC1");

        List<TransportationAccountingCode> createTacs = trdm.identifyTacsToCreate(newTacs, updatedTacs);
      
        assertEquals(createTacs.size(), 2);
        assertEquals(createTacs.get(0).getTacSysID(), newTacs.get(1).getTacSysID());
        assertEquals(createTacs.get(1).getTacSysID(), newTacs.get(2).getTacSysID());
    }

    @Test // Test that we can identify Loas needing to be updated
    void identifyLoasToUpdateTest() throws SQLException {
        ArrayList<LineOfAccounting> newLoas = createMockLoas(3);
        newLoas.get(0).setLoaSysID("LOA1");
        newLoas.get(1).setLoaSysID("LOA2");
        newLoas.get(2).setLoaSysID("LOA4");

        ArrayList<LineOfAccounting> currentLoas = createMockLoas(3);
        currentLoas.get(0).setLoaSysID("LOA1");
        currentLoas.get(1).setLoaSysID("LOA3");
        currentLoas.get(2).setLoaSysID("LOA5");

        List<LineOfAccounting> updateLoas = trdm.identifyLoasToUpdate(newLoas, currentLoas);
      
        assertEquals(updateLoas.size(), 1);
        assertEquals(updateLoas.get(0).getLoaSysID(), newLoas.get(0).getLoaSysID());
    }

    @Test // Test that we can identify Loas needing to be created
    void identifyLoasToCreateTest() throws SQLException {
        ArrayList<LineOfAccounting> newLoas = createMockLoas(3);
        newLoas.get(0).setLoaSysID("TAC1");
        newLoas.get(1).setLoaSysID("TAC2");
        newLoas.get(2).setLoaSysID("TAC3");

        ArrayList<LineOfAccounting> updateLoas = createMockLoas(3);
        updateLoas.get(0).setLoaSysID("TAC1");

        List<LineOfAccounting> createLoas = trdm.identifyLoasToCreate(newLoas, updateLoas);
      
        assertEquals(createLoas.size(), 2);
        assertEquals(createLoas.get(0).getLoaSysID(), newLoas.get(1).getLoaSysID());
        assertEquals(createLoas.get(1).getLoaSysID(), newLoas.get(2).getLoaSysID());
    }


    @Test
    void identifyDuplicateLoasToDeleteTest() throws SQLException {

        Random rand = new Random();

        ArrayList<TransportationAccountingCode> currentTacs = createMockTacs(3);
        ArrayList<LineOfAccounting> currentLoas = createMockLoas(7);

        // This loa is a duplicate and referenced by TACS. This should not be in the loasToDelete. No Delete
        currentLoas.get(0).setLoaSysID("DUPE1");
        currentTacs.get(0).setLoaID(currentLoas.get(0).getId());

        // This loa is a duplicate and not referenced in TACS but is not the oldest. Should not be in loas to delete. No Delete
        currentLoas.get(1).setLoaSysID("DUPE1");
        currentLoas.get(1).setUpdatedAt(currentLoas.get(0).getUpdatedAt().plusMinutes(5));

        // This loa is a duplicate but not referenced in TACS and is the oldest. This should be in loasToDelete. Yes Delete
        currentLoas.get(2).setLoaSysID("DUPE1");
        currentLoas.get(2).setUpdatedAt(currentLoas.get(0).getUpdatedAt().plusMinutes(3));

        // Dupe2 will have duplicates not referenced in TACs and oldest. This should be in loasToDelete. Yes Delete
        currentLoas.get(3).setLoaSysID("DUPE2");
        currentLoas.get(3).setUpdatedAt(currentLoas.get(2).getUpdatedAt().plusMinutes(1));

        // This loa is a duplicate, not referenced in TACs but is not the oldest. Should not be in loasToDelete. No Delete
        currentLoas.get(4).setLoaSysID("DUPE2");
        currentLoas.get(4).setUpdatedAt(currentLoas.get(2).getUpdatedAt().plusMinutes(2));


        // Not duplicates so these should not be in the loasToDeleteList
        currentLoas.get(5).setLoaSysID("NoDupe" + rand.nextInt(1000) + rand.nextInt(1000)); // No delete
        currentLoas.get(6).setLoaSysID("NoDupe" + rand.nextInt(1000) + rand.nextInt(1000)); // No Delete

        // List of duplicateLoas
        ArrayList<String> duplicateLoaSysIds = new ArrayList<String>();
        duplicateLoaSysIds.add(currentLoas.get(0).getLoaSysID());
        duplicateLoaSysIds.add(currentLoas.get(1).getLoaSysID());
        duplicateLoaSysIds.add(currentLoas.get(2).getLoaSysID());
        duplicateLoaSysIds.add(currentLoas.get(3).getLoaSysID());
        duplicateLoaSysIds.add(currentLoas.get(4).getLoaSysID());


        ArrayList<LineOfAccounting> loasToDelete = trdm.identifyDuplicateLoasToDelete(currentLoas, currentTacs, duplicateLoaSysIds);
        List<UUID> loaIds = loasToDelete.stream().map(loa -> loa.getId()).collect(Collectors.toList());

        // Loas that should be deleted should be returned for deletion
        assertTrue(loaIds.contains(currentLoas.get(2).getId()));
        assertTrue(loaIds.contains(currentLoas.get(3).getId()));

        // Loas that should not be deleted should not be returned for deletion
        assertFalse(loaIds.contains(currentLoas.get(0).getId()));
        assertFalse(loaIds.contains(currentLoas.get(1).getId()));
        assertFalse(loaIds.contains(currentLoas.get(4).getId()));
        assertFalse(loaIds.contains(currentLoas.get(5).getId()));
        assertFalse(loaIds.contains(currentLoas.get(6).getId()));
    }

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
        mockLoa.setCreatedAt(LocalDateTime.now());

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
        UUID testLoaUUID = UUID.randomUUID();

        mockTac.setId(testUUID);
        mockTac.setLoaID(testLoaUUID);
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

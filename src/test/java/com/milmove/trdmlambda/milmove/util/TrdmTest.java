package com.milmove.trdmlambda.milmove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;

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
}

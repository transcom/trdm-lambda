package com.milmove.trdmlambda.milmove.service;

import java.io.IOException;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.exceptions.TableRequestException;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;
import com.milmove.trdmlambda.milmove.util.ClientPasswordCallback;
import com.milmove.trdmlambda.milmove.util.SHA512PolicyLoader;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import jakarta.xml.ws.BindingProvider;
import jakarta.activation.DataHandler;
import lombok.Data;
import cxf.trdm.returntableservice.ColumnFilter;
import cxf.trdm.returntableservice.ColumnFilterTypeAndValues;
import cxf.trdm.returntableservice.ReturnTable;
import cxf.trdm.returntableservice.ReturnTableInput;
import cxf.trdm.returntableservice.ReturnTableInput.TRDM;
import cxf.trdm.returntableservice.ReturnTableInput.TRDM.ColumnFilters;
import cxf.trdm.returntableservice.ReturnTableRequestElement;
import cxf.trdm.returntableservice.ReturnTableResponseElement;
import cxf.trdm.returntableservice.ReturnTableWSSoapHttpPort;
import cxf.trdm.returntableservice.SingleValueDateFilterType;
import cxf.trdm.returntableservice.SingleValueDateTimeFilter;
import cxf.trdm.returntableservice.SingleValueDateTimeFilterType;
import cxf.trdm.returntableservice.TwoValueDateTimeFilter;
import cxf.trdm.returntableservice.TwoValueFilterType;

@Service
@Data
public class GetTableService {

    private static final String SUCCESS = "Successful";
    private static final String FAILURE = "Failure";

    private Logger logger = (Logger) LoggerFactory.getLogger(GetTableService.class);

    @Autowired
    private TrdmProps trdmProps;
    @Autowired
    private ClientPasswordCallback clientPasswordCallback;

    private ReturnTable returnTable = new ReturnTable();
    private ReturnTableWSSoapHttpPort returnTableWSSoapHttpPort = returnTable.getReturnTableWSSoapHttpPort();

    public GetTableService(TrdmProps trdmProps, ClientPasswordCallback clientPasswordCallback) {
        Client client = ClientProxy.getClient(returnTableWSSoapHttpPort);
        // Set HTTP policy (Timeout)
        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        // Doubling timeout from 30 -> 90
        httpClientPolicy.setConnectionTimeout(90000);
        httpClientPolicy.setReceiveTimeout(90000);
        httpConduit.setClient(httpClientPolicy);

        new SHA512PolicyLoader(client.getBus());
        Map<String, Object> ctx = ((BindingProvider) returnTableWSSoapHttpPort).getRequestContext();
        ctx.put("ws-security.callback-handler", clientPasswordCallback);
        ctx.put("ws-security.signature.properties", trdmProps.getPropsPath());
        ctx.put("ws-security.encryption.username", trdmProps.getEncryptionUsername());
    }

    /**
     * Processes REST request for getTable
     * 
     * @param request GetTableRequest
     * @return GetTableResponse
     * @throws IOException                    attachment processing failure
     * @throws DatatypeConfigurationException user provided string for
     *                                        contentUpdatedSinceDateTime not valid
     *                                        for XMLGregorianCalendar type
     * @throws TableRequestException
     */
    public GetTableResponse getTableRequest(GetTableRequest request)
            throws IOException, DatatypeConfigurationException, TableRequestException {
        return createSoapRequest(request);
    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     * @throws IOException
     * @throws DatatypeConfigurationException
     * @throws TableRequestException
     * @throws XMLStreamException
     */
    private GetTableResponse createSoapRequest(GetTableRequest request)
            throws IOException, DatatypeConfigurationException, TableRequestException {

        // To better understand this, please review sample payloads for TRDM
        // ReturnTableV7 WSDL endpoints
        // It makes more sense to imagine each class as a nested XML attribute, all
        // bundled together per WSDL guidelines
        // See README documentation on how these classes were generated.
        ReturnTableRequestElement requestElement = new ReturnTableRequestElement();
        ReturnTableInput input = new ReturnTableInput();
        TRDM trdm = new TRDM();
        trdm.setPhysicalName(request.getPhysicalName());
        trdm.setReturnContent(Boolean.valueOf(request.isReturnContent()));


        // Check if the optional fields of date time filters were provided
        // If so, then apply filters accordingly
        if (request.getContentUpdatedOnOrBeforeDateTime() != null) {
            // Convert String to XMLGregorianCalendar
            XMLGregorianCalendar contentUpdatedOnOrBeforeDateTime = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(request.getContentUpdatedOnOrBeforeDateTime());
            // IS_BETWEEN with TwoValueDateTimeFilter does not behave as expected. Add IS_ON_OR_BEFORE and IS_ON_OR_AFTER

            // Configure IS_ON_OR_AFTER column filter
            SingleValueDateTimeFilter isOnOrAfterSingleValueDateTimeFilter = new SingleValueDateTimeFilter();
            isOnOrAfterSingleValueDateTimeFilter.setFilterType(SingleValueDateTimeFilterType.IS_ON_OR_AFTER);
            // Set value to the contentUpdatedSinceDateTime (So we can reuse it, it means the same logically but is used
            // for different purposes)
            isOnOrAfterSingleValueDateTimeFilter.setFilterValue(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(request.getContentUpdatedSinceDateTime()));
            // Add SingleValueDateTimeFilter to ColumnFilterTypes
            ColumnFilterTypeAndValues isOnOrAfterColumnFilterTypeAndValues = new ColumnFilterTypeAndValues();
            isOnOrAfterColumnFilterTypeAndValues.getNoValueFilterOrSingleValueFilterOrSingleValueNumericalFilter()
                    .add(isOnOrAfterSingleValueDateTimeFilter);
            ColumnFilter isOnOrAfterColumnFilter = new ColumnFilter();
            isOnOrAfterColumnFilter.setColumn("LAST_UPD_DT"); // Last update date, this string value comes from TRDM
            // Add ColumnFilterTypes to ColumnFilter
            isOnOrAfterColumnFilter.setColumnFilterTypes(isOnOrAfterColumnFilterTypeAndValues);

            // Configure IS_ON_OR_BEFORE column filter
            SingleValueDateTimeFilter isOnOrBeforeSingleValueDateTimeFilter = new SingleValueDateTimeFilter();
            isOnOrBeforeSingleValueDateTimeFilter.setFilterType(SingleValueDateTimeFilterType.IS_ON_OR_BEFORE);
            isOnOrBeforeSingleValueDateTimeFilter.setFilterValue(contentUpdatedOnOrBeforeDateTime);
            // Add SingleValueDateTimeFilter to ColumnFilterTypes
            ColumnFilterTypeAndValues isOnOrBeforecolumnFilterTypeAndValues = new ColumnFilterTypeAndValues();
            isOnOrBeforecolumnFilterTypeAndValues.getNoValueFilterOrSingleValueFilterOrSingleValueNumericalFilter()
                    .add(isOnOrBeforeSingleValueDateTimeFilter);
            ColumnFilter isOnOrBeforeColumnFilter = new ColumnFilter();
            isOnOrBeforeColumnFilter.setColumn("LAST_UDP_DT"); // Last update date, this string value comes from TRDM
            // Add ColumnFilterTypes to ColumnFilter
            isOnOrBeforeColumnFilter.setColumnFilterTypes(isOnOrBeforecolumnFilterTypeAndValues);

            // Create the trdm columns filters class so we can add our column filter to it
            ReturnTableInput.TRDM.ColumnFilters columnFilters = new ReturnTableInput.TRDM.ColumnFilters();
            columnFilters.getColumnFilter().add(isOnOrAfterColumnFilter);
            columnFilters.getColumnFilter().add(isOnOrBeforeColumnFilter);

            // Set the columnFilters object to the trdm object
            trdm.setColumnFilters(columnFilters);
        } else {
        // Use this to set the data pull since time X so as to not always pull all data
        // every request
        trdm.setContentUpdatedSinceDateTime(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(request.getContentUpdatedSinceDateTime()));
        }

        // Check optional field
        if (request.isReturnContent()) {
            trdm.setReturnRowStatus(Boolean.valueOf(request.isReturnContent()));
        }
        // Check optional field
        if (request.isReturnLastUpdate()) {
            trdm.setReturnLastUpdate(Boolean.valueOf(request.isReturnLastUpdate()));
        }

        // Nest our classes for the XML SOAP body creation per WSDL specifications
        input.setTRDM(trdm);
        requestElement.setInput(input);

        return makeRequest(requestElement);
    }

    private GetTableResponse makeRequest(ReturnTableRequestElement requestElement)
            throws IOException, TableRequestException {
        ReturnTableResponseElement responseElement = returnTableWSSoapHttpPort.getTable(requestElement);
        GetTableResponse getTableResponse = new GetTableResponse();

        String statusCode = responseElement.getOutput().getTRDM().getStatus().getStatusCode();

        switch (statusCode) {
            case SUCCESS:
                logger.info("Request to TRDM succeeded");
                getTableResponse.setDateTime(responseElement.getOutput().getTRDM().getStatus().getDateTime());
                getTableResponse.setRowCount(responseElement.getOutput().getTRDM().getStatus().getRowCount());
                getTableResponse.setStatusCode(statusCode);
                // Convert attachment datahandler to bytes for the response:
                DataHandler dataHandler = responseElement.getAttachment();
                try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    dataHandler.writeTo(output);
                    byte[] bytes = output.toByteArray();
                    getTableResponse.setAttachment(bytes);
                } catch (IOException e) {
                    logger.error("Error while processing attachment", e);
                    throw e;
                }
                break;
            case FAILURE:
                logger.error("Request to TRDM failed: {}",
                        responseElement.getOutput().getTRDM().getStatus().getMessage());
                throw new TableRequestException(responseElement.getOutput().getTRDM().getStatus().getMessage());
            default:
                logger.error("Unknown status code: {} {}", statusCode,
                        responseElement.getOutput().getTRDM().getStatus().getMessage());
                throw new TableRequestException(responseElement.getOutput().getTRDM().getStatus().getMessage());
        }
        return getTableResponse;
    }
}
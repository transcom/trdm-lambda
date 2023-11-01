package com.milmove.trdmlambda.milmove.service;

import java.io.IOException;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
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
import cxf.trdm.returntableservice.ReturnTable;
import cxf.trdm.returntableservice.ReturnTableInput;
import cxf.trdm.returntableservice.ReturnTableInput.TRDM;
import cxf.trdm.returntableservice.ReturnTableRequestElement;
import cxf.trdm.returntableservice.ReturnTableResponseElement;
import cxf.trdm.returntableservice.ReturnTableWSSoapHttpPort;

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

        ReturnTableRequestElement requestElement = new ReturnTableRequestElement();
        ReturnTableInput input = new ReturnTableInput();
        TRDM trdm = new TRDM();
        trdm.setPhysicalName(request.getPhysicalName());
        trdm.setReturnContent(request.isReturnContent());
        trdm.setReturnContent(Boolean.valueOf(request.isReturnContent()));
        trdm.setContentUpdatedSinceDateTime(DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(request.getContentUpdatedSinceDateTime()));

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
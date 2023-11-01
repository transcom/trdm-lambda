package com.milmove.trdmlambda.milmove.service;

import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.exceptions.TableRequestException;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;
import com.milmove.trdmlambda.milmove.util.ClientPasswordCallback;
import com.milmove.trdmlambda.milmove.util.SHA512PolicyLoader;

import ch.qos.logback.classic.Logger;
import jakarta.xml.ws.BindingProvider;
import lombok.Data;
import cxf.trdm.returntableservice.ReturnTable;
import cxf.trdm.returntableservice.ReturnTableLastUpdateRequest;
import cxf.trdm.returntableservice.ReturnTableLastUpdateResponse;
import cxf.trdm.returntableservice.ReturnTableWSSoapHttpPort;

@Service
@Data
public class LastTableUpdateService {
    private static final String SUCCESS = "Successful";
    private static final String FAILURE = "Failure";

    private Logger logger = (Logger) LoggerFactory.getLogger(LastTableUpdateService.class);

    @Autowired
    private TrdmProps trdmProps;

    @Autowired
    private ClientPasswordCallback clientPasswordCallback;

    private ReturnTable returnTable = new ReturnTable();
    private ReturnTableWSSoapHttpPort returnTableWSSoapHttpPort = returnTable.getReturnTableWSSoapHttpPort();

    public LastTableUpdateService(TrdmProps trdmProps, ClientPasswordCallback clientPasswordCallback) {
        // This is not pretty, but it can absolutely save sanity when debugging. Leaving
        // until next version comes out
        if (trdmProps == null) {
            throw new IllegalArgumentException("trdmProps is null!");
        }
        if (trdmProps.getPropsPath() == null) {
            throw new IllegalArgumentException("trdmProps.getPropsPath() is null!");
        }

        Client client = ClientProxy.getClient(returnTableWSSoapHttpPort);
        new SHA512PolicyLoader(client.getBus());
        Map<String, Object> ctx = ((BindingProvider) returnTableWSSoapHttpPort).getRequestContext();
        ctx.put("ws-security.callback-handler", clientPasswordCallback);
        ctx.put("ws-security.signature.properties", trdmProps.getPropsPath());
        ctx.put("ws-security.encryption.username", trdmProps.getEncryptionUsername());

    }

    /**
     * Processes lastTableUpdate REST request
     * 
     * @param request LastTableUpdateRequest
     * @return LastTableUpdateResponse
     * @throws TableRequestException
     */
    public LastTableUpdateResponse lastTableUpdateRequest(LastTableUpdateRequest request) throws TableRequestException {
        return createSoapRequest(request);
    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     * @throws TableRequestException
     */
    private LastTableUpdateResponse createSoapRequest(LastTableUpdateRequest request) throws TableRequestException {
        ReturnTableLastUpdateRequest requestElement = new ReturnTableLastUpdateRequest();
        requestElement.setPhysicalName(request.getPhysicalName());
        return makeRequest(requestElement);

    }

    private LastTableUpdateResponse makeRequest(ReturnTableLastUpdateRequest requestElement)
            throws TableRequestException {
        ReturnTableLastUpdateResponse responseElement = returnTableWSSoapHttpPort.getLastTableUpdate(requestElement);
        LastTableUpdateResponse lastTableUpdateResponse = new LastTableUpdateResponse();

        String statusCode = responseElement.getStatus().getStatusCode();

        switch (statusCode) {
            case SUCCESS:
                logger.info("Request for last table update succeeded");
                lastTableUpdateResponse.setDateTime(responseElement.getStatus().getDateTime());
                lastTableUpdateResponse.setLastUpdate(responseElement.getLastUpdate());
                lastTableUpdateResponse.setStatusCode(statusCode);
                break;
            case FAILURE:
                logger.error("Request for last table update failed: {}",
                        responseElement.getStatus().getMessage());
                throw new TableRequestException(responseElement.getStatus().getMessage());
            default:
                logger.error("Unknown status code: {} {}", statusCode,
                        responseElement.getStatus().getMessage());
                throw new TableRequestException(responseElement.getStatus().getMessage());
        }
        return lastTableUpdateResponse;
    }
}

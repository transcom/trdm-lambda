package com.milmove.trdmlambda.milmove.service;

import java.util.Map;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;
import com.milmove.trdmlambda.milmove.util.ClientPasswordCallback;
import com.milmove.trdmlambda.milmove.util.SHA512PolicyLoader;

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

    @Autowired
    private TrdmProps trdmProps;

    @Autowired
    private ClientPasswordCallback clientPasswordCallback;

    private ReturnTable returnTable = new ReturnTable();
    private ReturnTableWSSoapHttpPort returnTableWSSoapHttpPort = returnTable.getReturnTableWSSoapHttpPort();

    public LastTableUpdateService(TrdmProps trdmProps, ClientPasswordCallback clientPasswordCallback) {
        // This is not pretty, but it can absolutely save sanity when debugging. Leaving until next version comes out
        if(trdmProps == null) {
            throw new IllegalArgumentException("trdmProps is null!");
        }
        if(trdmProps.getPropsPath() == null) {
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
     */
    public LastTableUpdateResponse lastTableUpdateRequest(LastTableUpdateRequest request) {
        return createSoapRequest(request);
    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     */
    private LastTableUpdateResponse createSoapRequest(LastTableUpdateRequest request) {
        ReturnTableLastUpdateRequest requestElement = new ReturnTableLastUpdateRequest();
        requestElement.setPhysicalName(request.getPhysicalName());
        return makeRequest(requestElement);

    }

    private LastTableUpdateResponse makeRequest(ReturnTableLastUpdateRequest requestElement) {
        ReturnTableLastUpdateResponse responseElement = returnTableWSSoapHttpPort.getLastTableUpdate(requestElement);
        LastTableUpdateResponse lastTableUpdateResponse = new LastTableUpdateResponse();

        if (responseElement.getStatus().getStatusCode().equals(SUCCESS)) {
            lastTableUpdateResponse.setDateTime(responseElement.getStatus().getDateTime());
            lastTableUpdateResponse.setLastUpdate(responseElement.getLastUpdate());
            lastTableUpdateResponse.setStatusCode(responseElement.getStatus().getStatusCode());
        }
        return lastTableUpdateResponse;
    }

}

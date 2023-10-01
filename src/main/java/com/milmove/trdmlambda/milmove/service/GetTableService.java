package com.milmove.trdmlambda.milmove.service;

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;
import com.milmove.trdmlambda.milmove.util.ClientPasswordCallback;
import com.milmove.trdmlambda.milmove.util.SHA512PolicyLoader;

import jakarta.xml.ws.BindingProvider;
import lombok.Data;
import trdm.returntableservice.ReturnTable;
import trdm.returntableservice.ReturnTableInput;
import trdm.returntableservice.ReturnTableInput.TRDM;
import trdm.returntableservice.ReturnTableRequestElement;
import trdm.returntableservice.ReturnTableResponseElement;
import trdm.returntableservice.ReturnTableWSSoapHttpPort;

@Service
@Data
public class GetTableService {

    private static final String SUCCESS = "Success";

    @Autowired
    private TrdmProps trdmProps;

    private ReturnTable returnTable = new ReturnTable();
    private ReturnTableWSSoapHttpPort returnTableWSSoapHttpPort = returnTable.getReturnTableWSSoapHttpPort();

    private GetTableService() {
        Client client = ClientProxy.getClient(returnTableWSSoapHttpPort);
        new SHA512PolicyLoader(client.getBus());
        Map<String, Object> ctx = ((BindingProvider) returnTableWSSoapHttpPort).getRequestContext();
        ctx.put("ws-security.callback-handler", ClientPasswordCallback.class.getName());
        ctx.put("ws-security.signature.properties", trdmProps.getClientPropertiesFilePath());
        ctx.put("ws-security.encryption.username", trdmProps.getEncryptionUsername());
    }

    /**
     * Processes REST request for getTable
     * 
     * @param request GetTableRequest
     * @return GetTableResponse
     */
    public GetTableResponse getTableRequest(GetTableRequest request) {
        return createSoapRequest(request);
    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     * @throws XMLStreamException
     */
    private GetTableResponse createSoapRequest(GetTableRequest request) {

        ReturnTableRequestElement requestElement = new ReturnTableRequestElement();
        ReturnTableInput input = new ReturnTableInput();
        TRDM trdm = new TRDM();
        trdm.setPhysicalName(request.getPhysicalName());
        trdm.setReturnContent(Boolean.valueOf(request.getPhysicalName()));
        trdm.setContentUpdatedSinceDateTime(null);

        input.setTRDM(trdm);
        requestElement.setInput(input);

        return makeRequest(requestElement);

    }

    private GetTableResponse makeRequest(ReturnTableRequestElement requestElement) {
        ReturnTableResponseElement responseElement = returnTableWSSoapHttpPort.getTable(requestElement);
        GetTableResponse getTableResponse = new GetTableResponse();

        if (responseElement.getOutput().getTRDM().getStatus().getMessage().equals(SUCCESS)) {
            getTableResponse.setDateTime(responseElement.getOutput().getTRDM().getStatus().getDateTime());
            getTableResponse.setRowCount(responseElement.getOutput().getTRDM().getStatus().getRowCount());
            getTableResponse.setStatusCode(responseElement.getOutput().getTRDM().getStatus().getStatusCode());
            getTableResponse.setAttachment(null);
        }
        return getTableResponse;

    }
}

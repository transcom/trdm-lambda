package com.milmove.trdmlambda.milmove.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderLoader;
import org.apache.cxf.ws.security.policy.custom.AlgorithmSuiteBuilder;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.apache.wss4j.stax.ext.WSSSecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Client;

import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import trdm.returntableservice.ReturnTable;
import trdm.returntableservice.ReturnTableInput;
import trdm.returntableservice.ReturnTableRequestElement;
import trdm.returntableservice.ReturnTableResponseElement;
import trdm.returntableservice.ReturnTableWSSoapHttpPort;
import trdm.returntableservice.ReturnTableInput.TRDM;

@Service
public class GetTableService {

    @Autowired
    private TrdmProps trdmProps;

    /**
     * Processes REST request for getTable
     * 
     * @param request GetTableRequest
     * @return GetTableResponse
     */
    public GetTableResponse getTableRequest(GetTableRequest request) {
        // return callSoapWebService(trdmProps.getServiceUrl(), "POST", request);
        buildSoapBody(request);
        return null;
    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     */
    private void buildSoapBody(GetTableRequest request) {

        ReturnTable returnTable = new ReturnTable();
        ReturnTableWSSoapHttpPort returnTableWSSoapHttpPort = returnTable.getReturnTableWSSoapHttpPort();

        ReturnTableRequestElement requestElement = new ReturnTableRequestElement();
        ReturnTableInput input = new ReturnTableInput();
        TRDM trdm = new TRDM();
        trdm.setPhysicalName(request.getPhysicalName());
        trdm.setReturnContent(Boolean.valueOf(request.getPhysicalName()));
        trdm.setContentUpdatedSinceDateTime(null);

        input.setTRDM(trdm);
        requestElement.setInput(input);

        Client client = ClientProxy.getClient(returnTableWSSoapHttpPort);
        Endpoint cxfEndpoint = client.getEndpoint();

        Map<String, Object> outProps = new HashMap<String, Object>();
        // how to configure the properties is outlined below;
        outProps.put(ConfigurationConstants.ACTION, "Signature");
        outProps.put(ConfigurationConstants.ACTOR, "myAlias");
        outProps.put(ConfigurationConstants.ACTION,
                ConfigurationConstants.TIMESTAMP + " " +
                        ConfigurationConstants.SIGNATURE + " " +
                        ConfigurationConstants.ENCRYPT);
        outProps.put(ConfigurationConstants.SIG_C14N_ALGO, "");
        outProps.put(ConfigurationConstants.SIG_DIGEST_ALGO, "");
        outProps.put(ConfigurationConstants.SIG_PROP_FILE, "client_sign.properties");

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        cxfEndpoint.getOutInterceptors().add(wssOut);

        returnTableWSSoapHttpPort.getTable(requestElement);

        // return null;
    }

    // private GetTableResponse callSoapWebService(String soapEndpointUrl, String
    // soapAction, GetTableRequest request) {

    // JAXBContext jaxbContext;
    // try (SOAPConnection soapConnection =
    // SOAPConnectionFactory.newInstance().createConnection()) {
    // // Send SOAP Message to SOAP Server
    // SOAPMessage soapResponse = soapConnection.call(buildSoapBody(request),
    // soapEndpointUrl);

    // SOAPBody body = soapResponse.getSOAPBody();
    // jaxbContext = JAXBContext.newInstance(GetTableResponse.class);
    // Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    // GetTableResponse getTable = (GetTableResponse)
    // jaxbUnmarshaller.unmarshal(body);

    // return getTable;

    // } catch (Exception e) {
    // System.err.println(
    // "\nError occurred while sending SOAP Request to Server!\nMake sure you have
    // the correct endpoint URL and SOAPAction!\n");
    // e.printStackTrace();
    // }
    // return null;

    // }

}

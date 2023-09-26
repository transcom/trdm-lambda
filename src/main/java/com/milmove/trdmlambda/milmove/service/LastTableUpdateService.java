package com.milmove.trdmlambda.milmove.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.config.TrdmProps;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;

import jakarta.xml.bind.JAXBContext;
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

@Service
public class LastTableUpdateService {

    @Autowired
    private TrdmProps trdmProps;

    /**
     * Processes lastTableUpdate REST request
     * 
     * @param request LastTableUpdateRequest
     * @return LastTableUpdateResponse
     */
    public LastTableUpdateResponse lastTableUpdateRequest(LastTableUpdateRequest request) {
        return callSoapWebService(trdmProps.getServiceUrl(), "POST", request);
    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     */
    private SOAPMessage buildSoapBody(LastTableUpdateRequest request) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage msg = factory.createMessage();
            SOAPPart part = msg.getSOAPPart();

            SOAPEnvelope envelope = part.getEnvelope();
            SOAPHeader header = envelope.getHeader();
            envelope.addNamespaceDeclaration("ret", "http://ReturnTablePackage/");
            SOAPBody body = envelope.getBody();

            SOAPElement lastTableUpdateRequestElement = body.addChildElement("lastTableUpdateRequestElement", "ret");

            SOAPElement physicalNameElement = lastTableUpdateRequestElement.addChildElement("physicalName", "ret");
            physicalNameElement.addTextNode(request.getPhysicalName());

            msg.saveChanges();

            msg.writeTo(System.out);
            return msg;

        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    private LastTableUpdateResponse callSoapWebService(String soapEndpointUrl, String soapAction,
            LastTableUpdateRequest request) {
        JAXBContext jaxbContext;
        try (SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection()) {
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(buildSoapBody(request), soapEndpointUrl);

            SOAPBody body = soapResponse.getSOAPBody();
            jaxbContext = JAXBContext.newInstance(LastTableUpdateResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (LastTableUpdateResponse) jaxbUnmarshaller
                    .unmarshal(body);

        } catch (Exception e) {
            System.err.println(
                    "\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
        return null;
    }

}

package com.milmove.trdmlambda.milmove.service;

import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;

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
    private String endpointURL = "";

    /**
     * Processes lastTableUpdate REST request
     * @param request LastTableUpdateRequest
     * @return LastTableUpdateResponse
     */
    public LastTableUpdateResponse lastTableUpdateRequest(LastTableUpdateRequest request) {
        callSoapWebService(endpointURL, "POST" ,request);
        var response = new LastTableUpdateResponse();

        return response;
    }

    /**
     * Builds SOAP body from REST request
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

    private void callSoapWebService(String soapEndpointUrl, String soapAction, LastTableUpdateRequest request) {
        try(SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection()) {
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(buildSoapBody(request), soapEndpointUrl);

            // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);

        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
    }

}

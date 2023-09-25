package com.milmove.trdmlambda.milmove.service;

import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;

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
public class GetTableService {
    private String endpointURL = "";

    /**
     * Processes REST request for getTable
     * 
     * @param request GetTableRequest
     * @return GetTableResponse
     */
    public GetTableResponse getTableRequest(GetTableRequest request) {
        callSoapWebService(endpointURL, "POST", request);
        var response = new GetTableResponse();
        return response;

    }

    /**
     * Builds SOAP body from REST request
     * 
     * @param request - GetTableRequest
     * @return built SOAP XML body with header.
     */
    private SOAPMessage buildSoapBody(GetTableRequest request) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage msg = factory.createMessage();
            SOAPPart part = msg.getSOAPPart();

            SOAPEnvelope envelope = part.getEnvelope();
            SOAPHeader header = envelope.getHeader();
            envelope.addNamespaceDeclaration("ret", "http://ReturnTablePackage/");
            SOAPBody body = envelope.getBody();

            SOAPElement getTableRequestElement = body.addChildElement("getTableRequestElement", "ret");
            SOAPElement inputElement = getTableRequestElement.addChildElement("input", "ret");
            SOAPElement trdmElement = inputElement.addChildElement("TRDM", "ret");

            SOAPElement physicalNameElement = trdmElement.addChildElement("physicalName", "ret");
            physicalNameElement.addTextNode(request.getPhysicalName());

            SOAPElement returnContentElement = trdmElement.addChildElement("returnContent", "ret");
            returnContentElement.addTextNode(String.valueOf(request.isReturnContent()));

            SOAPElement contentUpdatedSinceDateTimeElement = trdmElement.addChildElement("contentUpdatedSinceDateTime",
                    "ret");
            contentUpdatedSinceDateTimeElement.addTextNode(request.getContentUpdatedSinceDateTime());

            msg.saveChanges();
            return msg;

        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    private void callSoapWebService(String soapEndpointUrl, String soapAction, GetTableRequest request) {
        try (SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection()) {
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(buildSoapBody(request), soapEndpointUrl);

            // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();

        } catch (Exception e) {
            System.err.println(
                    "\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
    }

}

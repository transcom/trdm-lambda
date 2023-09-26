package com.milmove.trdmlambda.milmove.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;

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
public class GetTableService {

    @Value("{$trdm.service-url}")
    private String endpointURL;

    /**
     * Processes REST request for getTable
     * 
     * @param request GetTableRequest
     * @return GetTableResponse
     */
    public GetTableResponse getTableRequest(GetTableRequest request) {
        return callSoapWebService(endpointURL, "POST", request);
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

    private GetTableResponse callSoapWebService(String soapEndpointUrl, String soapAction, GetTableRequest request) {

        JAXBContext jaxbContext;
        try (SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection()) {
            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(buildSoapBody(request),
                    soapEndpointUrl);

            SOAPBody body = soapResponse.getSOAPBody();
            jaxbContext = JAXBContext.newInstance(GetTableResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            GetTableResponse getTable = (GetTableResponse) jaxbUnmarshaller.unmarshal(body);
            System.out.println(getTable.getStatusCode());
            System.out.println(getTable.getRowCount());
            System.out.println(getTable.getDateTime());

            return getTable;

        } catch (Exception e) {
            System.err.println(
                    "\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }
        return null;

    }

}

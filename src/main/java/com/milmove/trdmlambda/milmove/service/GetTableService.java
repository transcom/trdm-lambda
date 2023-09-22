package com.milmove.trdmlambda.milmove.service;

import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

/**
 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:ret="http://ReturnTablePackage/">
    <soapenv:Header />
    <soapenv:Body>
        <ret:getTableRequestElement>
            <ret:input>
                <ret:TRDM>
                    <ret:physicalName>ACFT</ret:physicalName>
                    <ret:returnContent>true</ret:returnContent>
                </ret:TRDM>
            </ret:input>
        </ret:getTableRequestElement>
    </soapenv:Body>
</soapenv:Envelope>
 */
@Service
public class GetTableService {

    public GetTableResponse getTableRequest(GetTableRequest request) {
        buildSoapBody(request);
        var response = new GetTableResponse();
        return response;

    }

    private void buildSoapBody(GetTableRequest request) {
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

            SOAPElement contentUpdatedSinceDateTimeElement = trdmElement.addChildElement("contentUpdatedSinceDateTime", "ret");
            contentUpdatedSinceDateTimeElement.addTextNode(request.getContentUpdatedSinceDateTime());
            
            msg.saveChanges();

            msg.writeTo(System.out);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}

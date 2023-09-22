package com.milmove.trdmlambda.milmove.service;

import org.springframework.stereotype.Service;

import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

@Service
public class LastTableUpdateService {

    public LastTableUpdateResponse lastTableUpdateRequest(LastTableUpdateRequest request) {
        buildSoapBody(request);
        var response = new LastTableUpdateResponse();

        return response;
    }

    private void buildSoapBody(LastTableUpdateRequest request) {
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

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}

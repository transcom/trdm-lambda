package com.milmove.trdmlambda.milmove.model.gettable;

import java.math.BigInteger;

import javax.xml.datatype.XMLGregorianCalendar;

import lombok.Data;

@Data
public class GetTableResponse {

    private BigInteger rowCount;
    private String statusCode;
    private XMLGregorianCalendar dateTime;
    private byte[] attachment;

    
}

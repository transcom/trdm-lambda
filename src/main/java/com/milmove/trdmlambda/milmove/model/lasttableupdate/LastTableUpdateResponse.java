package com.milmove.trdmlambda.milmove.model.lasttableupdate;

import javax.xml.datatype.XMLGregorianCalendar;

import lombok.Data;

@Data
public class LastTableUpdateResponse {

    private String statusCode;
    private XMLGregorianCalendar dateTime;
    private XMLGregorianCalendar lastUpdate;

}

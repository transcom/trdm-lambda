package com.milmove.trdmlambda.milmove.model.gettable;

import javax.xml.datatype.XMLGregorianCalendar;

import com.milmove.trdmlambda.milmove.contraints.ContentUpdatedSinceDateTimeConstraint;
import com.milmove.trdmlambda.milmove.contraints.PhysicalNameConstraint;

import lombok.Data;

@Data
public class GetTableRequest {
    @PhysicalNameConstraint
    private String physicalName;
    @ContentUpdatedSinceDateTimeConstraint
    private XMLGregorianCalendar contentUpdatedSinceDateTime;
    private boolean returnContent;
}

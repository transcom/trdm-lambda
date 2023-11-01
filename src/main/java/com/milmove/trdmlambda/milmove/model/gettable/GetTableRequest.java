package com.milmove.trdmlambda.milmove.model.gettable;

import com.milmove.trdmlambda.milmove.contraints.ContentUpdatedSinceDateTimeConstraint;
import com.milmove.trdmlambda.milmove.contraints.PhysicalNameConstraint;

import lombok.Data;

@Data
public class GetTableRequest {
    @PhysicalNameConstraint
    private String physicalName;
    @ContentUpdatedSinceDateTimeConstraint
    private String contentUpdatedSinceDateTime; // To be converted to javax.xml.datatype.XMLGregorianCalendar
    private boolean returnContent;
}

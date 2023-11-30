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
    private String contentUpdatedSinceDateTime; // To be converted to javax.xml.datatype.XMLGregorianCalendar
    private boolean returnContent;
    // Optional date time filters to be used in specific range requests
    private String contentUpdatedOnOrBeforeDateTime; // Optional field: XMLGregorianCalendar
    private boolean returnRowStatus; // Optional field: Returns individual row status with request, boolean
    private boolean returnLastUpdate; // Optional field: Return last update with request, boolean
}

package com.milmove.trdmlambda.milmove.model.gettable;

import lombok.Data;

@Data
public class GetTableResponse {

    private int rowCount;
    private String statusCode;
    private String dateTime;
    private byte[] attachment;

    
}

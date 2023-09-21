package com.milmove.trdmLambda.model.getTable;

import lombok.Data;

@Data
public class GetTableRequest {
    private String physicalName;
    private String contentUpdatedSinceDateTime;
    private boolean returnContent;
    
}

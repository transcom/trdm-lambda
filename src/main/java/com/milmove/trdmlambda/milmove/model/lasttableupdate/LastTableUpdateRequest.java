package com.milmove.trdmlambda.milmove.model.lasttableupdate;

import com.milmove.trdmlambda.milmove.contraints.PhysicalNameConstraint;

import lombok.Data;

@Data
public class LastTableUpdateRequest {
    @PhysicalNameConstraint
    private String physicalName;
    
}

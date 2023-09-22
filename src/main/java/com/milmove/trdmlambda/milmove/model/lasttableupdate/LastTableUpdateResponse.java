package com.milmove.trdmlambda.milmove.model.lasttableupdate;

import lombok.Data;

@Data
public class LastTableUpdateResponse {

    private String statusCode;
    private String dateTime;
    private String lastUpdate;

}

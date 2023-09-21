package com.milmove.trdmLambda;

import com.amazonaws.services.costexplorer.model.Context;

public class GetLastTableUpdateMethodHandler {
    public String handleRequest(String input, Context context) {
       // context.getLogger().log("Input: " + input);
        return "Hello World - " + input;
    }
    
}

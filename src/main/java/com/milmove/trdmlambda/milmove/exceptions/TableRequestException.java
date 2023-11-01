package com.milmove.trdmlambda.milmove.exceptions;

// Exception to be thrown in the case of TRDM table requests
public class TableRequestException extends Exception {
    public TableRequestException(String message) {
        super(message);
    }
}
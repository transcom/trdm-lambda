package com.milmove.trdmlambda.milmove.model;

import java.util.List;

import lombok.Data;

@Data
public class ErrorResponse {
    List<Errors> errors;
}

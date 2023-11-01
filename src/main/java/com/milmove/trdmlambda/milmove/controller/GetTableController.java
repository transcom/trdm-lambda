package com.milmove.trdmlambda.milmove.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.milmove.trdmlambda.milmove.model.gettable.GetTableRequest;
import com.milmove.trdmlambda.milmove.model.gettable.GetTableResponse;
import com.milmove.trdmlambda.milmove.service.GetTableService;

import java.io.IOException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1")
public class GetTableController {

    @Autowired
    private GetTableService getTableService;

    private Logger logger = (Logger) LoggerFactory.getLogger(GetTableController.class);

    @PostMapping(path = "/getTable", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GetTableResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = "application/json") })
    })
    public ResponseEntity<GetTableResponse> getTable(@Valid @RequestBody GetTableRequest requestBody) {
        logger.info("Received a request for GetTable with details: {}", requestBody);
        try {
            GetTableResponse response = getTableService.getTableRequest(requestBody);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error processing attachment for GetTable request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (DatatypeConfigurationException e) {
            logger.error("Error processing XMLGregorianCalendar type for provided contentUpdatedSinceDateTime value for GetTable request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

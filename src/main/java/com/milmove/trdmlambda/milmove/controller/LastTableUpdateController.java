package com.milmove.trdmlambda.milmove.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateRequest;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;
import com.milmove.trdmlambda.milmove.service.LastTableUpdateService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1")
public class LastTableUpdateController {

    @Autowired
    private LastTableUpdateService lastTableUpdateService;

    @PostMapping(path = "/lastTableUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LastTableUpdateResponse.class)) })
    })
    public LastTableUpdateResponse lastTableUpdate(@Valid @RequestBody LastTableUpdateRequest requestBody) {
        return lastTableUpdateService.lastTableUpdateRequest(requestBody);
    }

}

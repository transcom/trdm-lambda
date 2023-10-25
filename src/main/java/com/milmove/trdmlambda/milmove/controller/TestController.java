package com.milmove.trdmlambda.milmove.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @PostMapping("/test")
    public ResponseEntity<String> postTest() {
        return ResponseEntity.ok("Test Successful!");
    }

    @PostMapping("/api/v1/test")
    public ResponseEntity<String> postTestApiV1() {
        return ResponseEntity.ok("Test Successful!");
    }
}

package com.milmove.trdmlambda.milmove.util;

import com.milmove.trdmlambda.milmove.service.EmailService;

import jakarta.mail.MessagingException;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private SecretFetcher secretFetcher;

    @InjectMocks
    private EmailService emailService;

    @Test // Test sendMalformedTACDataEmailTest()
    void sendMalformedTACDataEmailTest() throws MessagingException, URISyntaxException {

        EmailService spyEmailService = spy(emailService);
        ArrayList<String> codes = new ArrayList<String>();

        codes.add("TEST");

        // Skip the actual email send in a test
        Mockito.doNothing().when(spyEmailService).send(any());
        spyEmailService.sendMalformedTACDataEmail(codes);

        verify(spyEmailService, times(1)).send(any());
    }

    @Test // Test sendMalformedLOADataEmailTest()
    void sendMalformedLOADataEmailTest() throws MessagingException, URISyntaxException {

        EmailService spyEmailService = spy(emailService);
        ArrayList<String> codes = new ArrayList<String>();
        codes.add("TEST");

        // Skip the actual email send in a test
        Mockito.doNothing().when(spyEmailService).send(any());
        spyEmailService.sendMalformedLOADataEmail(codes);

        verify(spyEmailService, times(1)).send(any());
    }
}

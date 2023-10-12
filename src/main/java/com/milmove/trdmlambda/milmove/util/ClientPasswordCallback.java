package com.milmove.trdmlambda.milmove.util;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;


@Component
public class ClientPasswordCallback implements CallbackHandler {

    @Autowired
    private SecretFetcher secretFetcher;
    
    private String keyStorePassword;

        @PostConstruct
        public void init() {
            this.keyStorePassword = secretFetcher.getSecret("trdm_lambda_milmove_keypair_key");

        }

    
    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        // set the password for our message.
        pc.setPassword(keyStorePassword);
    }

}
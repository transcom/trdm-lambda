package com.milmove.trdmlambda.milmove.util;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.stereotype.Component;

@Component
public class ClientPasswordCallback implements CallbackHandler {

    private String keyStorePassword;

    public ClientPasswordCallback(SecretFetcher secretFetcher) {
        this.keyStorePassword = secretFetcher.getSecret("trdm_lambda_milmove_keypair_key");
    }
    
    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        // set the password for our message.
        pc.setPassword(keyStorePassword);
    }

}
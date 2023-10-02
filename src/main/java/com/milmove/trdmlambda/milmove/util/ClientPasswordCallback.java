package com.milmove.trdmlambda.milmove.util;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.config.KeyStoreConfig;

@Component
public class ClientPasswordCallback implements CallbackHandler {
    @Autowired
    KeyStoreConfig keyStoreConfig;

    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        // set the password for our message.
        pc.setPassword(keyStoreConfig.getKeyStorePassword());
    }

}
package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.Saga;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Pipe;

public class ForwardingContextSetupInterceptor<SAGA extends Saga> {
    private final SAGA sagaUnderTest;
    private final Consumer2<SAGA, Message> contextSetter;

    ForwardingContextSetupInterceptor(final SAGA sagaUnderTest, final Consumer2<SAGA, Message> contextSetter) {
        this.sagaUnderTest = sagaUnderTest;
        this.contextSetter = contextSetter;
    }

    /**
     * Called by the dynamic code generation framework to intercept a method that takes a Message as its first argument.
     */
    public void handle(final @Argument(0) Message message, final @Pipe Forwarder<SAGA, Void> pipe) {
        // sets up the context for handling the given message
        contextSetter.accept(sagaUnderTest, message);

        // calls the handle method on the original saga under test instance
        // debug into this method to get directly to your saga code
        pipe.to(sagaUnderTest);
    }
}

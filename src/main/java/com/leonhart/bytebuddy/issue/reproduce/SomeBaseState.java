package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.AbstractSagaState;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import java.io.Serializable;

public class SomeBaseState extends AbstractSagaState<Serializable> {
    private Message initialMessage;

    public Message getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(final Message initialMessage) {
        this.initialMessage = initialMessage;
    }

    public void addMessage(final Message message) {
        if (initialMessage == null) {
            initialMessage = message;
        }
    }
}

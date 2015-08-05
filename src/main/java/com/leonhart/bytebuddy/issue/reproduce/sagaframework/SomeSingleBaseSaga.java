package com.leonhart.bytebuddy.issue.reproduce.sagaframework;

import com.codebullets.sagalib.AbstractSingleEventSaga;
import com.leonhart.bytebuddy.issue.reproduce.Messenger;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;

public class SomeSingleBaseSaga<MESSAGE extends Message> extends AbstractSingleEventSaga {
    private Messenger messenger;

    public void injectMessenger(final Messenger injectedMessenger) {
        this.messenger = injectedMessenger;
    }
}

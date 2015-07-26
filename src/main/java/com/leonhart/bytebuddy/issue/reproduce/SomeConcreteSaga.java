package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.StartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.AcknowledgeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeMessageThatContinuesSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeResponseForSaga;

public class SomeConcreteSaga extends SomeBaseSaga<SomeBaseState, SomeRequestThatStartsSaga> {

    @StartsSaga
    public void handle(final SomeRequestThatStartsSaga message) {
        acknowledge(message, new AcknowledgeRequestThatStartsSaga());
    }

    @EventHandler
    public void handle(final SomeMessageThatContinuesSaga message) {
        respondWith(new SomeResponseForSaga());
    }
}

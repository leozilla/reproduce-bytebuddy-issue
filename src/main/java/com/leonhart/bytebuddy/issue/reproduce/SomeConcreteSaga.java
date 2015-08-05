package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.StartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.AcknowledgeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeMessageThatContinuesSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeResponseForSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseState;
import javax.inject.Inject;

public class SomeConcreteSaga extends SomeBaseSaga<SomeBaseState, SomeRequestThatStartsSaga> {

    @Inject
    public SomeConcreteSaga(final Object dummy) {
    }

    @StartsSaga
    public void handle(final SomeRequestThatStartsSaga message) {
        acknowledge(message, new AcknowledgeRequestThatStartsSaga());
    }

    @EventHandler
    public void handle(final SomeMessageThatContinuesSaga message) {
        respondWith(new SomeResponseForSaga());
    }

    // no onTimeout override
}

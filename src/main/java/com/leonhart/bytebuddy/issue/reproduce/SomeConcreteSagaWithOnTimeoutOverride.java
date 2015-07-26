package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.StartsSaga;
import com.codebullets.sagalib.timeout.TimeoutId;
import com.leonhart.bytebuddy.issue.reproduce.messages.AcknowledgeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeMessageThatContinuesSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeResponseForSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseState;

public class SomeConcreteSagaWithOnTimeoutOverride extends SomeBaseSaga<SomeBaseState, SomeRequestThatStartsSaga> {

    @StartsSaga
    public void handle(final SomeRequestThatStartsSaga message) {
        acknowledge(message, new AcknowledgeRequestThatStartsSaga());
    }

    @EventHandler
    public void handle(final SomeMessageThatContinuesSaga message) {
        respondWith(new SomeResponseForSaga());
    }

    @Override
    public void onTimeout(final TimeoutId id, final String name, final Object data) {
    }
}

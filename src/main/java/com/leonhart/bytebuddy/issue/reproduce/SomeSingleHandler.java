package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.StartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeSingleBaseSaga;

public class SomeSingleHandler extends SomeSingleBaseSaga<SomeRequestThatStartsSaga> {

    @StartsSaga
    public void handle(final SomeRequestThatStartsSaga request) {
    }
}

package com.leonhart.bytebuddy.issue.reproduce;

import com.leonhart.bytebuddy.issue.reproduce.messages.AcknowledgeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

public class SomeSingleHandlerTest extends SingleSagaTest {

    private SomeSingleHandler sut;

    @Before
    public void before() {
        sut = intercept(injectDependencies(new SomeSingleHandler()));
    }

    @Test
    public void startSaga_shallInterceptHandleMethodToSetupContext_andThenForwardToRealInstance() {
        // GIVEN

        // WHEN
        SomeRequestThatStartsSaga startingMessage = new SomeRequestThatStartsSaga();
        sut.handle(startingMessage);

        // THEN
        verify(getMessenger()).respondToRequest(eq(startingMessage), isA(AcknowledgeRequestThatStartsSaga.class), any());
    }
}
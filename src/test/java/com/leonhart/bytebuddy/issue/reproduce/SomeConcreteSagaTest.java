package com.leonhart.bytebuddy.issue.reproduce;

import com.leonhart.bytebuddy.issue.reproduce.messages.AcknowledgeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

public class SomeConcreteSagaTest extends SagaTest {
    private static final String CORRELATION_ID = "AnyCorrelationIdForThisMessage";

    private SomeConcreteSaga sut;

    @Before
    public void before() {
        setUpCorrelationIdForContext(CORRELATION_ID);

        sut = intercept(injectDependencies(new SomeConcreteSaga()));
    }

    @Test
    public void startSaga_shallInterceptHandleMethodToSetupContext_andThenForwardToRealInstance() {
        // GIVEN

        // WHEN
        SomeRequestThatStartsSaga startingMessage = new SomeRequestThatStartsSaga();
        sut.handle(startingMessage);

        // THEN
        verify(getMessenger()).respondToRequest(eq(startingMessage), isA(AcknowledgeRequestThatStartsSaga.class), eq(CORRELATION_ID));
    }

    @Test
    public void onTimeout_shallInterceptHandleMethodToSetupContext_andThenForwardToRealInstance() {
        // GIVEN

        // WHEN
        sut.onTimeout(null, "", null);

        // THEN

    }
}
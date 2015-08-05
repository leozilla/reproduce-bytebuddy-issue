package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseState;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SagaTest extends BaseSagaTest {
    private Messenger messenger;
    private ExecutionContext executionContext;

    protected void setUpCorrelationIdForContext(final String anyCorrelationIdForThisMessage) {
        executionContext = mock(ExecutionContext.class);
        given(executionContext.getHeaderValue("correlationId")).willReturn(anyCorrelationIdForThisMessage);
    }

    protected <SAGA extends SomeBaseSaga> SAGA injectDependencies(final SAGA sagaUnderTest) {
        messenger = mock(Messenger.class);

        sagaUnderTest.injectMessenger(messenger);
        sagaUnderTest.createNewState();
        return sagaUnderTest;
    }

    protected <SAGA extends SomeBaseSaga> SAGA intercept(final SAGA sagaUnderTest) {
        Class<? extends Saga> dynamicSpaceSagaSubClass = generateProxyClassForSagaUnderTest(sagaUnderTest, this::setupContext);
        return newInstanceUnchecked(dynamicSpaceSagaSubClass);
    }

    protected <SAGA extends SomeBaseSaga> void setupContext(final SAGA saga, final Message msg) {
        ((SomeBaseState) saga.state()).addMessage(msg);
        saga.setExecutionContext(executionContext);
    }

    private static <T> T newInstanceUnchecked(final Class<?> clazz) {
        T instance;
        try {
            instance = (T) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        return instance;
    }

    public Messenger getMessenger() {
        return messenger;
    }
}

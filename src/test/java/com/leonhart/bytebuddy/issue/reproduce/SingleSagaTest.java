package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.Saga;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeSingleBaseSaga;

import static org.mockito.Mockito.mock;

public class SingleSagaTest extends BaseSagaTest {
    private Messenger messenger;

    protected void setUp(final Saga sagaUnderTest) {
        super.setup();
    }

    protected <SAGA extends SomeSingleBaseSaga> SAGA injectDependencies(final SAGA sagaUnderTest) {
        setUp(sagaUnderTest);
        messenger = mock(Messenger.class);

        sagaUnderTest.injectMessenger(messenger);
        return sagaUnderTest;
    }

    protected <SAGA extends SomeSingleBaseSaga> SAGA intercept(final SAGA sagaUnderTest) {
        Class<? extends Saga> dynamicSpaceSagaSubClass = generateProxyClassForSagaUnderTest(sagaUnderTest, this::setupContext);
        return newInstanceUnchecked(dynamicSpaceSagaSubClass);
    }

    protected <SAGA extends SomeSingleBaseSaga> void setupContext(final SAGA saga, final Message msg) {
        superCall(msg);
        saga.setExecutionContext(getExecutionContext());
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

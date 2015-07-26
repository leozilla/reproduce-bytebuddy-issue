package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.StartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.AcknowledgeRequestThatStartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import org.junit.Before;
import org.junit.Test;

import static com.leonhart.bytebuddy.issue.reproduce.DynamicSagaTypeBuilder.generateSubClassFor;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SomeConcreteSagaTest {
    private SomeConcreteSaga sut;
    private Messenger messenger;
    private ExecutionContext executionContext;

    @Before
    public void before() {
        executionContext = mock(ExecutionContext.class);
        given(executionContext.getHeaderValue("correlationId")).willReturn("AnyCorrelationIdForThisMessage");

        messenger = mock(Messenger.class);

        sut = intercept(injectDependencies(new SomeConcreteSaga()));
    }

    private <SAGA extends SomeBaseSaga> SAGA injectDependencies(final SAGA sagaUnderTest) {
        sagaUnderTest.injectMessenger(messenger);
        return sagaUnderTest;
    }

    private <SAGA extends SomeBaseSaga> SAGA intercept(final SAGA sagaUnderTest) {
        Class<? extends Saga> dynamicSpaceSagaSubClass = generateProxyClassForSagaUnderTest(sagaUnderTest, this::setupContext);
        return newInstanceUnchecked(dynamicSpaceSagaSubClass);
    }

    protected <SAGA extends Saga> Class<? extends Saga> generateProxyClassForSagaUnderTest(
            final SAGA sagaUnderTest,
            final Consumer2<SAGA, Message> contextSetter) {
        return generateSubClassFor(sagaUnderTest.getClass())
                .withDefaultNullPassingConstructor()
                .and(
                        builder -> builder.method(isAnnotatedWith(StartsSaga.class).or(isAnnotatedWith(EventHandler.class)))
                                          .intercept(
                                                  MethodDelegation.to(new ForwardingContextSetupInterceptor<>(sagaUnderTest, contextSetter))
                                                                  .appendParameterBinder(Pipe.Binder.install(Forwarder.class)))
                                          .method(
                                                  isPublic()
                                                          .and(isDeclaredBy(sagaUnderTest.getClass()).or(isDeclaredBy(AbstractSaga.class)))
                                                          .and(not(isAnnotatedWith(StartsSaga.class)))
                                                          .and(not(isAnnotatedWith(EventHandler.class))))
                                          .intercept(MethodDelegation.to(sagaUnderTest).filter(not(isDeclaredBy(Object.class)))))
                .buildAndLoad();
    }

    private <SAGA extends SomeBaseSaga> void setupContext(final SAGA saga, final Message msg) {
        ((SomeBaseState) saga.state()).addMessage(msg);
        saga.setExecutionContext(executionContext);
    }

    @Test
    public void startSaga_shallInterceptHandleMethodToSetupContext_andThenForwardToRealInstance() {
        // GIVEN

        // WHEN
        SomeRequestThatStartsSaga startingMessage = new SomeRequestThatStartsSaga();
        sut.handle(startingMessage);

        // THEN
        verify(messenger).respondToRequest(eq(startingMessage), isA(AcknowledgeRequestThatStartsSaga.class), eq("AnyCorrelationIdForThisMessage"));
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
}
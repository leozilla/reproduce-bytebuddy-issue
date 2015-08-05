package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.StartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Pipe;

import static com.leonhart.bytebuddy.issue.reproduce.DynamicSagaTypeBuilder.generateSubClassFor;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BaseSagaTest {

    private ExecutionContext executionContext;

    protected void setup() {
        executionContext = mock(ExecutionContext.class);
    }

    protected ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public Message superCall(final Message message) {
        given(executionContext.message()).willReturn(message);
        given(getExecutionContext().getHeaderValue("correlationId")).willReturn("Any");
        return message;
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
}

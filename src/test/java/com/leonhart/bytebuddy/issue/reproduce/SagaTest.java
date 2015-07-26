package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.StartsSaga;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseSaga;
import com.leonhart.bytebuddy.issue.reproduce.sagaframework.SomeBaseState;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Pipe;

import static com.leonhart.bytebuddy.issue.reproduce.DynamicSagaTypeBuilder.defineConstructorIfNecessary;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SagaTest {
    private Messenger messenger;
    private ExecutionContext executionContext;

    protected void setUpCorrelationIdForContext(final String anyCorrelationIdForThisMessage) {
        executionContext = mock(ExecutionContext.class);
        given(executionContext.getHeaderValue("correlationId")).willReturn(anyCorrelationIdForThisMessage);
    }

    protected <SAGA extends SomeBaseSaga> SAGA injectDependencies(final SAGA sagaUnderTest) {
        messenger = mock(Messenger.class);

        sagaUnderTest.injectMessenger(messenger);
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

    protected <SAGA extends Saga> Class<? extends Saga> generateProxyClassForSagaUnderTest(
            final SAGA sagaUnderTest,
            final Consumer2<SAGA, Message> contextSetter) {
        Class<? extends Saga> sagaUnderTestClass = sagaUnderTest.getClass();

        DynamicType.Builder<? extends Saga> dynamicBuilder = new ByteBuddy()
                .subclass(sagaUnderTestClass, ConstructorStrategy.Default.IMITATE_SUPER_TYPE_PUBLIC);

        dynamicBuilder = defineConstructorIfNecessary(sagaUnderTestClass, dynamicBuilder);

        return dynamicBuilder.method(isAnnotatedWith(StartsSaga.class).or(isAnnotatedWith(EventHandler.class)))
                             .intercept(MethodDelegation.to(new ForwardingContextSetupInterceptor<>(sagaUnderTest, contextSetter))
                                                        .appendParameterBinder(Pipe.Binder.install(Forwarder.class)))
                             .method(isPublic()
                                      .and(isDeclaredBy(sagaUnderTest.getClass()).or(isDeclaredBy(AbstractSaga.class)))
                                      .and(not(isAnnotatedWith(StartsSaga.class)))
                                      .and(not(isAnnotatedWith(EventHandler.class))))
                             .intercept(MethodDelegation.to(sagaUnderTest))
                             .make()
                             .load(sagaUnderTestClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                             .getLoaded();
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

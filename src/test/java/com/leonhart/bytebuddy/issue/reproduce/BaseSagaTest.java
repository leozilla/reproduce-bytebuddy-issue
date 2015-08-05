package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
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

public class BaseSagaTest {

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
                                                          .and(not(isAnnotatedWith(StartsSaga.class))).and(not(isAnnotatedWith(EventHandler.class))))
                                          .intercept(MethodDelegation.to(sagaUnderTest)))
                /*
                                          .method(
                                                  isPublic()
                                                          .and(isDeclaredBy(sagaUnderTest.getClass()))
                                                          .and(not(isAnnotatedWith(StartsSaga.class)))
                                                          .and(not(isAnnotatedWith(EventHandler.class))))
                                          .intercept(MethodDelegation.to(sagaUnderTest))
                                          .method(named("state").or(named("onTimeout").or(named("isFinished"))))
                                          .intercept(MethodDelegation.to(sagaUnderTest)))*/
                                .buildAndLoad();
    }
}

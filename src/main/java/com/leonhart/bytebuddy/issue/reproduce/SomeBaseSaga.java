package com.leonhart.bytebuddy.issue.reproduce;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.timeout.Timeout;
import com.codebullets.sagalib.timeout.TimeoutId;
import com.google.common.reflect.TypeToken;
import com.leonhart.bytebuddy.issue.reproduce.messages.Message;
import com.leonhart.bytebuddy.issue.reproduce.messages.SomeRequestThatStartsSaga;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;

public abstract class SomeBaseSaga<SAGA_STATE extends SomeBaseState, MESSAGE extends SomeRequestThatStartsSaga> extends AbstractSaga<SAGA_STATE> {
    private static final TypeVariable<Class<SomeBaseSaga>> STATE_TYPE_PARAMETER = SomeBaseSaga.class.getTypeParameters()[0];

    private String correlationId;
    private Messenger messenger;

    public void createNewState() {
        Class<?> rawType = TypeToken.of(getClass()).resolveType(STATE_TYPE_PARAMETER).getRawType();
        try {
            setState((SAGA_STATE) rawType.newInstance());
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(String.format("Cannot instantiate saga state class (%s)", rawType), ex);
        }
    }

    public Collection<KeyReader> keyReaders() {
        return Collections.EMPTY_LIST;
    }

    public void injectMessenger(final Messenger injectedMessenger) {
        this.messenger = injectedMessenger;
    }

    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {
        super.setExecutionContext(executionContext);
        correlationId = (String) executionContext.getHeaderValue("correlationId");
    }

    @EventHandler
    public final void handle(final Timeout timeout) {
        // if (timeout.notCanceled) {
        onTimeout(timeout.getId(), timeout.getName(), timeout.getData());
        // }
    }

    public void onTimeout(final TimeoutId id, final String name, final Object data) {
    }

    protected void acknowledge(final Message request, final Message ack) {
        messenger.respondToRequest(request, ack, correlationId);
    }

    protected void respondWith(final Message response) {
        messenger.respondToRequest(state().getInitialMessage(), response, correlationId);
    }
}

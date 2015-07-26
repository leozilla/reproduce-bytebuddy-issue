package com.leonhart.bytebuddy.issue.reproduce;

import com.leonhart.bytebuddy.issue.reproduce.messages.Message;

public interface Messenger {
    void respondToRequest(final Message request, final Message response, final String correlationId);
}

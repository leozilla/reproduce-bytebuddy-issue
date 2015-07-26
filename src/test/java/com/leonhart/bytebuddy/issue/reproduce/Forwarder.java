package com.leonhart.bytebuddy.issue.reproduce;

public interface Forwarder<TARGET, OUT> {

    OUT to(TARGET target);
}
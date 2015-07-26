package com.leonhart.bytebuddy.issue.reproduce;

@FunctionalInterface
public interface Consumer2<T1, T2> {

    void accept(T1 arg1, T2 arg2);
}

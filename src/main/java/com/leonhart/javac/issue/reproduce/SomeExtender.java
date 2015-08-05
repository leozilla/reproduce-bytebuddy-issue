package com.leonhart.javac.issue.reproduce;

import java.util.function.Consumer;

public class SomeExtender<T> {

    public SomeExtender<T> withExceptionHandler(final Consumer<T> action) {
        return this;
    }
}

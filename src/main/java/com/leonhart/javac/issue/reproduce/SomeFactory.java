package com.leonhart.javac.issue.reproduce;

import java.util.function.Function;

public class SomeFactory {

    public SomeFactoredObject<Exception> create(final Function<SomeExtender<Exception>, SomeExtender<Exception>> builder) {
        return new SomeFactoredObject<>();
    }
}


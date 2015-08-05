package com.leonhart.javac.issue.reproduce;

public class InnerBaseClass {
    private final SomeFactoredObject<Exception> someFactoredObject;

    InnerBaseClass(final SomeFactoredObject<Exception> someFactoredObject) {
        this.someFactoredObject = someFactoredObject;
    }
}

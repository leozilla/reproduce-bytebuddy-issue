package com.leonhart.javac.issue.reproduce;

public class JavacIssue {
    private final InnerClass strategy;

    public JavacIssue(final SomeFactory innerClass) {
        strategy = new InnerClass(innerClass);
    }

    class InnerClass extends InnerBaseClass {

        public InnerClass(final SomeFactory factory) {
            // VerifierError happens here because of ref to other scope i guess
            super(factory.create(o -> o.withExceptionHandler(JavacIssue.this::handleMyException)));
        }
    }

    private void handleMyException(final Exception ex) {
    }
}

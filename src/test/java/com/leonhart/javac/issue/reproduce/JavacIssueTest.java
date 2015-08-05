package com.leonhart.javac.issue.reproduce;

import org.junit.Test;

public class JavacIssueTest {

    @Test
    public void createJavaIssues_shallNotThrowVerifierError() {
        new JavacIssue(new SomeFactory());
    }
}
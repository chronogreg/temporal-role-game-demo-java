package com.temporal.roleGameDemo.testOfTest;

public class FormatImpl implements Format {

    @Override
    public String composeGreeting(String name) {
        return "Hello " + name + "!";
    }
}
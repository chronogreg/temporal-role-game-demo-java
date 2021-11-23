package com.temporal.roleGameDemo;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Assert;

public class FizzBuzzProcessorTest {
    @Test
    public void FizzBuzzNormalNumbers() {

        System.out.println("Executing FizzBuzzNormalNumbers()...");

        FizzBuzzProcessor fb = new FizzBuzzProcessor();
        Assert.assertEquals("1", fb.convert(1));
        Assert.assertEquals("2", fb.convert(2));
    }

    @Test
    public void FizzBuzzThreeNumbers() {

        System.out.println("Executing FizzBuzzThreeNumbers()...");

        FizzBuzzProcessor fb = new FizzBuzzProcessor();
        Assert.assertEquals("Fizz", fb.convert(3));
    }

    @Test
    public void FizzBuzzFiveNumbers() {

        System.out.println("Executing FizzBuzzFiveNumbers()...");

        FizzBuzzProcessor fb = new FizzBuzzProcessor();
        Assert.assertEquals("Buzz", fb.convert(5));
    }

    @Test
    public void FizzBuzzThreeAndFiveNumbers() {

        System.out.println("Executing FizzBuzzThreeAndFiveNumbers()...");

        FizzBuzzProcessor fb = new FizzBuzzProcessor();
        Assert.assertEquals("Buzz", fb.convert(5));
    }
}
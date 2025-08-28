package com.teamnorms;


import org.junit.Test;

import org.junit.Before;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;


public class simpleTest {



    private String testString;



    @Before

    public void setUp() {

        testString = "Hello World";

    }



    @Test

    public void test1() {

        System.out.println("Test 1 works!");

        assertThat(testString, is("Hello World"));

    }



    @Test

    public void test2() {

        assertNotNull(testString);

        assertTrue(testString.contains("World"));

    }

}





package com.opengamma.sesame.config;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.test.TestGroup;

@Test(groups= TestGroup.UNIT)
public class FunctionArgumentsTest {

  @Test
  public void merge4() {
    FunctionArguments args1 = new FunctionArguments(ImmutableMap.<String, Object>of("foo", "FOO", "bar", "BAR"));
    FunctionArguments args2 = new FunctionArguments(ImmutableMap.<String, Object>of("bar", "BAR2", "baz", "BAZ"));
    FunctionArguments args3 = new FunctionArguments(ImmutableMap.<String, Object>of("boz", "BOZ", "baz", "BAZ2"));
    FunctionArguments args4 = new FunctionArguments(ImmutableMap.<String, Object>of("aaa", "AAA", "baz", "BAZ3"));

    FunctionArguments expected =
        new FunctionArguments(ImmutableMap.<String, Object>of("foo", "FOO",
                                                              "bar", "BAR",
                                                              "baz", "BAZ",
                                                              "boz", "BOZ",
                                                              "aaa", "AAA"));
    assertEquals(expected, args1.mergeWith(args2, args3, args4));
  }
}

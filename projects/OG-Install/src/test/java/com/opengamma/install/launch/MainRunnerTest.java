/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.launch;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Tests the {@link MainRunner} class
 */
@Test
public class MainRunnerTest {

  public static class Main {

    private static List<String[]> s_args;

    public static void main(final String[] args) {
      s_args.add(args);
    }

  }

  public void testNoArg() {
    MainRunner.main(new String[] {});
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testBadArgNumber() {
    MainRunner.main(new String[] {"Foo", "Bar", "Cow" });
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testBadClassName() {
    MainRunner.main(new String[] {"NoSuchClass", "Bar" });
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testBadClassMethod() {
    MainRunner.main(new String[] {getClass().getName(), "Bar" });
  }

  public void testOne() {
    Main.s_args = new ArrayList<String[]>();
    MainRunner.main(new String[] {Main.class.getName(), "Foo" });
    assertEquals(Main.s_args.size(), 1);
    assertEquals(Main.s_args.get(0)[0], "Foo");
    Main.s_args = null;
  }

  public void testTwo() {
    Main.s_args = new ArrayList<String[]>();
    MainRunner.main(new String[] {Main.class.getName(), "Foo", Main.class.getName(), "Bar Cow" });
    assertEquals(Main.s_args.size(), 2);
    assertEquals(Main.s_args.get(0)[0], "Foo");
    assertEquals(Main.s_args.get(1)[0], "Bar");
    assertEquals(Main.s_args.get(1)[1], "Cow");
    Main.s_args = null;
  }

  public void testQuotedArgs() {
    Main.s_args = new ArrayList<String[]>();
    MainRunner.main(new String[] {Main.class.getName(), "A B 'C D' 'E'" });
    assertEquals(Main.s_args.size(), 1);
    assertEquals(Main.s_args.get(0)[0], "A");
    assertEquals(Main.s_args.get(0)[1], "B");
    assertEquals(Main.s_args.get(0)[2], "C D");
    assertEquals(Main.s_args.get(0)[3], "E");
    Main.s_args = null;
  }

}

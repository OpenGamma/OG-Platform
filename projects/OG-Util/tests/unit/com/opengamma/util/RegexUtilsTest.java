/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Test RegexUtils.
 */
public class RegexUtilsTest {

  @Test(expected=IllegalArgumentException.class)
  public void test_null() {
    assertEquals(null, RegexUtils.wildcardsToPattern(null));
  }

  @Test
  public void test_star() {
    assertEquals(Pattern.compile("^\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("Hello*").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("*Hello").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("*Hello*").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.*\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He*llo").toString());
  }

  @Test
  public void test_doubleStar() {
    assertEquals(Pattern.compile("^\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("Hello**").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("**Hello").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("**Hello*").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.*\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He**llo").toString());
  }

  @Test
  public void test_question() {
    assertEquals(Pattern.compile("^\\QHello\\E.$").toString(), RegexUtils.wildcardsToPattern("Hello?").toString());
    assertEquals(Pattern.compile("^.\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("?Hello").toString());
    assertEquals(Pattern.compile("^.\\QHello\\E.$").toString(), RegexUtils.wildcardsToPattern("?Hello?").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He?llo").toString());
  }

  @Test
  public void test_escape() {
    assertEquals(Pattern.compile("^\\QH\\E.*\\Qel[l\\E.\\Qo\\E$").toString(), RegexUtils.wildcardsToPattern("H*el[l?o").toString());
  }

}

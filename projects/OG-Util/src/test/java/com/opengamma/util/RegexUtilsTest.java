/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RegexUtilsTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_null() {
    assertEquals(null, RegexUtils.wildcardsToPattern(null));
  }

  public void test_star() {
    assertEquals(Pattern.compile("^\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("Hello*").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("*Hello").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("*Hello*").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.*\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He*llo").toString());
  }

  public void test_doubleStar() {
    assertEquals(Pattern.compile("^\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("Hello**").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("**Hello").toString());
    assertEquals(Pattern.compile("^.*\\QHello\\E.*$").toString(), RegexUtils.wildcardsToPattern("**Hello*").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.*\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He**llo").toString());
  }

  public void test_question() {
    assertEquals(Pattern.compile("^\\QHello\\E.$").toString(), RegexUtils.wildcardsToPattern("Hello?").toString());
    assertEquals(Pattern.compile("^.\\QHello\\E$").toString(), RegexUtils.wildcardsToPattern("?Hello").toString());
    assertEquals(Pattern.compile("^.\\QHello\\E.$").toString(), RegexUtils.wildcardsToPattern("?Hello?").toString());
    assertEquals(Pattern.compile("^\\QHe\\E.\\Qllo\\E$").toString(), RegexUtils.wildcardsToPattern("He?llo").toString());
  }

  public void test_escape() {
    assertEquals(Pattern.compile("^\\QH\\E.*\\Qel[l\\E.\\Qo\\E$").toString(), RegexUtils.wildcardsToPattern("H*el[l?o").toString());
  }

  public void test_matches_star() {
    assertEquals(true, RegexUtils.wildcardMatch("Hello*", "Hello"));
    assertEquals(true, RegexUtils.wildcardMatch("Hello*", "Hello world"));
    assertEquals(false, RegexUtils.wildcardMatch("Hello*", "Hell on earth"));
    assertEquals(false, RegexUtils.wildcardMatch(null, "Hell on earth"));
    assertEquals(false, RegexUtils.wildcardMatch("Hello*", null));
  }

}

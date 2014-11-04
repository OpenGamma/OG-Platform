/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SimulationUtilsTest {

  @Test
  public void patternForGlob() {
    assertEquals("\\Qfoo\\E.\\Qbar\\E", patternFor("foo?bar"));
    assertEquals(".\\Qfoobar\\E", patternFor("?foobar"));
    assertEquals("\\Qfoobar\\E.", patternFor("foobar?"));
    assertEquals("\\Qfoo\\E..\\Qbar\\E", patternFor("foo??bar"));
    assertEquals("..\\Qfoobar\\E", patternFor("??foobar"));
    assertEquals("\\Qfoobar\\E..", patternFor("foobar??"));
    assertEquals(".\\Qfoobar\\E.", patternFor("?foobar?"));

    assertEquals(".*?\\Qfoobar\\E.*?", patternFor("*foobar%"));
    assertEquals("\\Qfoo\\E.*?\\Qbar\\E", patternFor("foo*bar"));
    assertEquals("\\Qf\\E.\\Qoo\\E.*?\\Qbar\\E", patternFor("f?oo*bar"));
    assertEquals("\\Qf\\E.\\Qoo\\E.*?\\Qbar\\E", patternFor("f?oo%bar"));
  }

  @Test
  public void globMatches() {
    assertTrue(matches("foo?bar", "fooxbar"));
    assertFalse(matches("foo?bar", "foobar"));
    assertFalse(matches("foo?bar", "fooxxbar"));
    assertTrue(matches("foo*bar", "fooxxbar"));
    assertTrue(matches("foo*bar", "fooxbar"));
    assertTrue(matches("foo*bar", "foobar"));
    assertTrue(matches("foo%bar", "fooxxbar"));
    assertTrue(matches("foo%bar", "fooxbar"));
    assertTrue(matches("foo%bar", "foobar"));
    assertFalse(matches("?foo%bar", "foobar"));
    assertTrue(matches("?foo%bar", "xfoobar"));
    assertTrue(matches("?foo%bar", "xfooxbar"));
    assertTrue(matches("?foo%bar", "xfooxxbar"));
    assertTrue(matches("f??oo%bar", "fxxooxxbar"));
    assertFalse(matches("f??oo%bar", "fxooxxbar"));
    assertTrue(matches("$?^", "$A^"));
    assertTrue(matches("$*^", "$ABC^"));
    assertTrue(matches("$%^", "$ABC^"));
  }

  @SuppressWarnings("deprecation")
  private static boolean matches(String glob, String str) {
    return SimulationUtils.patternForGlob(glob).matcher(str).matches();
  }

  @SuppressWarnings("deprecation")
  private static String patternFor(String glob) {
    return SimulationUtils.patternForGlob(glob).pattern();
  }

}

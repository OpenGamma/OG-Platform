/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PathMatcherTest {

  public void testMatching() {
    assertTrue(PathMatcher.matches("/MarketData/Bloomberg/AAPL/View", "/MarketData/Bloomberg/*/View"));
    assertFalse(PathMatcher.matches("/MarketData2/Bloomberg/AAPL/View", "/MarketData/Bloomberg/*/View"));
    
    assertTrue(PathMatcher.matches("/Portfolio/2DFS/View", "/Portf*/**"));
    assertTrue(PathMatcher.matches("/Portfolio/3XYZ/Modify", "/Portf*/**"));
    assertFalse(PathMatcher.matches("/MarketData/Bloomberg/AAPL/View", "/Portf*/**"));
    
    assertTrue(PathMatcher.matches("/anything/anything", "**"));
    assertTrue(PathMatcher.matches("/anything", "**"));
    assertTrue(PathMatcher.matches("/", "**"));
    assertTrue(PathMatcher.matches("", "**"));
    
    assertTrue(PathMatcher.matches("/random", "/*"));
    assertFalse(PathMatcher.matches("random", "/*"));
    
    assertFalse(PathMatcher.matches("/random", "*"));
    assertFalse(PathMatcher.matches("random/", "*"));
    assertTrue(PathMatcher.matches("random", "*"));
    
    assertTrue(PathMatcher.matches("random/", "*/"));
    assertFalse(PathMatcher.matches("random", "*/"));
  }

}

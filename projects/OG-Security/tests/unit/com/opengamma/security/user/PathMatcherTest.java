/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import org.junit.Assert;
import org.junit.Test;

import com.opengamma.security.user.PathMatcher;

/**
 * 
 *
 * @author pietari
 */
public class PathMatcherTest {
  
  @Test
  public void testMatching() {
    
    Assert.assertTrue(PathMatcher.matches("/MarketData/Bloomberg/AAPL/View", "/MarketData/Bloomberg/*/View"));
    Assert.assertFalse(PathMatcher.matches("/MarketData2/Bloomberg/AAPL/View", "/MarketData/Bloomberg/*/View"));
    
    Assert.assertTrue(PathMatcher.matches("/Portfolio/2DFS/View", "/Portf*/**"));
    Assert.assertTrue(PathMatcher.matches("/Portfolio/3XYZ/Modify", "/Portf*/**"));
    Assert.assertFalse(PathMatcher.matches("/MarketData/Bloomberg/AAPL/View", "/Portf*/**"));
    
    Assert.assertTrue(PathMatcher.matches("/anything/anything", "**"));
    Assert.assertTrue(PathMatcher.matches("/anything", "**"));
    Assert.assertTrue(PathMatcher.matches("/", "**"));
    Assert.assertTrue(PathMatcher.matches("", "**"));
    
    Assert.assertTrue(PathMatcher.matches("/random", "/*"));
    Assert.assertFalse(PathMatcher.matches("random", "/*"));
    
    Assert.assertFalse(PathMatcher.matches("/random", "*"));
    Assert.assertFalse(PathMatcher.matches("random/", "*"));
    Assert.assertTrue(PathMatcher.matches("random", "*"));
    
    Assert.assertTrue(PathMatcher.matches("random/", "*/"));
    Assert.assertFalse(PathMatcher.matches("random", "*/"));
  }

}

/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the function statistics gatherer.
 */
public class FunctionCostTest {
  
  private FunctionCost _cost = new FunctionCost ();
  
  @Test
  public void testBasicBehaviour () {
    FunctionInvocationStatistics stats = _cost.getStatistics("Default", "Foo");
    assertNotNull (stats);
    // Initial values
    assertEquals (1.0, stats.getInvocationCost(), 1e-5);
    assertEquals (1.0, stats.getDataInputCost(), 1e-5);
    assertEquals (1.0, stats.getDataOutputCost (), 1e-5);
    _cost.functionInvoked("Default", "Foo", 1, 2.0, 3.0, 4.0);
    // First sample
    assertEquals (2.0, stats.getInvocationCost(), 1e-5);
    assertEquals (3.0, stats.getDataInputCost(), 1e-5);
    assertEquals (4.0, stats.getDataOutputCost (), 1e-5);
    _cost.functionInvoked("Default", "Foo", 99, 3.0, 4.0, 5.0);
    assertEquals (2.0, stats.getInvocationCost(), 1e-5);
    assertEquals (3.0, stats.getDataInputCost(), 1e-5);
    assertEquals (4.0, stats.getDataOutputCost (), 1e-5);
    _cost.functionInvoked("Default", "Foo", 1, 3.0, 4.0, 5.0);
    // Updated sample
    assertEquals (2.991, stats.getInvocationCost (), 0.0005);
    assertEquals (3.991, stats.getDataInputCost(), 0.0005);
    assertEquals (4.991, stats.getDataOutputCost (), 0.0005);
    _cost.functionInvoked("Default", "Foo", 100, 3.0, 4.0, 5.0);
    // Older data less relevant
    assertEquals (2.996, stats.getInvocationCost(), 0.0005);
    assertEquals (3.996, stats.getDataInputCost(), 0.0005);
    assertEquals (4.996, stats.getDataOutputCost (), 0.0005);
  }
  
  @Test
  public void testMaps () {
    assertSame (_cost.getStatistics ("A", "1"), _cost.getStatistics ("A", "1"));
    assertNotSame (_cost.getStatistics ("A", "2"), _cost.getStatistics ("B", "2"));
    assertNotSame (_cost.getStatistics ("B", "1"), _cost.getStatistics ("A", "1"));
  }
  
}
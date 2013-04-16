/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatistics;
import com.opengamma.engine.calcnode.stats.InMemoryFunctionCostsMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the function statistics gatherer.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionCostsTest {

  private InMemoryFunctionCostsMaster _master;
  private FunctionCosts _costs;

  @BeforeMethod
  public void setUp() {
    _master = new InMemoryFunctionCostsMaster();
    _costs = new FunctionCosts(_master);
  }

  public void testBasicBehaviour() {
    FunctionInvocationStatistics stats = _costs.getStatistics("Default", "Foo");
    assertNotNull(stats);
    // Initial values
    assertEquals(1.0, stats.getInvocationCost(), 1e-5);
    assertEquals(1.0, stats.getDataInputCost(), 1e-5);
    assertEquals(1.0, stats.getDataOutputCost(), 1e-5);
    _costs.functionInvoked("Default", "Foo", 1, 2.0, 3.0, 4.0);
    // First sample
    assertEquals(2.0, stats.getInvocationCost(), 1e-5);
    assertEquals(3.0, stats.getDataInputCost(), 1e-5);
    assertEquals(4.0, stats.getDataOutputCost(), 1e-5);
    _costs.functionInvoked("Default", "Foo", 99, 99.0 * 3.0, 99.0 * 4.0, 99.0 * 5.0);
    assertEquals(2.0, stats.getInvocationCost(), 1e-5);
    assertEquals(3.0, stats.getDataInputCost(), 1e-5);
    assertEquals(4.0, stats.getDataOutputCost(), 1e-5);
    _costs.functionInvoked("Default", "Foo", 1, 3.0, 4.0, 5.0);
    // Updated sample
    assertEquals(2.991, stats.getInvocationCost(), 0.0005);
    assertEquals(3.991, stats.getDataInputCost(), 0.0005);
    assertEquals(4.991, stats.getDataOutputCost(), 0.0005);
    _costs.functionInvoked("Default", "Foo", 100, 100.0 * 3.0, 100.0 * 4.0, 100.0 * 5.0);
    // Older data less relevant
    assertEquals(2.996, stats.getInvocationCost(), 0.0005);
    assertEquals(3.996, stats.getDataInputCost(), 0.0005);
    assertEquals(4.996, stats.getDataOutputCost(), 0.0005);
  }

  public void testMaps() {
    assertSame(_costs.getStatistics("A", "1"), _costs.getStatistics("A", "1"));
    assertNotSame(_costs.getStatistics("A", "2"), _costs.getStatistics("B", "2"));
    assertNotSame(_costs.getStatistics("B", "1"), _costs.getStatistics("A", "1"));
  }

  public void testPersistence() {
    FunctionInvocationStatistics stats = _costs.getStatistics("Default", "Foo");
    assertNotNull(stats);
    stats.recordInvocation(1, 1.0, 2.0, 3.0);
    final Runnable writer = _costs.createPersistenceWriter();
    assertNotNull(writer);
    // First run of the writer will write the new function to store (+ the mean document)
    writer.run();
    assertEquals(2, _master.size());
    // Second run will do nothing as stats and averages haven't changed
    writer.run();
    assertEquals(2, _master.size());
    // Update stats and check the document updates (and the average)
    stats.recordInvocation(100, 500.0, 600.0, 700.0);
    writer.run();
    assertEquals(2, _master.size());
    // Create a new repository and check the values were preserved
    FunctionCosts costs = new FunctionCosts(_master);
    stats = costs.getStatistics("Default", "Foo");
    assertEquals(5.0, stats.getInvocationCost(), 0.05);
    assertEquals(6.0, stats.getDataInputCost(), 0.05);
    assertEquals(7.0, stats.getDataOutputCost(), 0.05);
  }

  public void testInitialMean() {
    FunctionInvocationStatistics stats = _costs.getStatistics("Default", "Foo");
    assertEquals(1.0, stats.getInvocationCost(), 1e-5);
    assertEquals(1.0, stats.getDataInputCost(), 1e-5);
    assertEquals(1.0, stats.getDataOutputCost(), 1e-5);
    stats.recordInvocation(1, 2.0, 3.0, 4.0);
    // Nothing will have updated the average
    stats = _costs.getStatistics("Default", "Bar");
    assertEquals(1.0, stats.getInvocationCost(), 1e-5);
    assertEquals(1.0, stats.getDataInputCost(), 1e-5);
    assertEquals(1.0, stats.getDataOutputCost(), 1e-5);
    final Runnable writer = _costs.createPersistenceWriter();
    writer.run();
    // Averages will have been set now
    stats = _costs.getStatistics("Default", "Cow");
    assertEquals(1.3, stats.getInvocationCost(), 0.05);
    assertEquals(1.7, stats.getDataInputCost(), 0.05);
    assertEquals(2.0, stats.getDataOutputCost(), 0.05);
    // Create a new repository and check the average was preserved
    FunctionCosts costs = new FunctionCosts(_master);
    stats = costs.getStatistics("Default", "Man");
    assertEquals(1.3, stats.getInvocationCost(), 0.05);
    assertEquals(1.7, stats.getDataInputCost(), 0.05);
    assertEquals(2.0, stats.getDataOutputCost(), 0.05);
  }

}

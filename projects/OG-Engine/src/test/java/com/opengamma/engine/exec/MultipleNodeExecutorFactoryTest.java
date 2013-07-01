/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.exec.plan.CachingExecutionPlanner;
import com.opengamma.engine.exec.plan.MultipleNodeExecutionPlanner;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MultipleNodeExecutorFactory} class.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleNodeExecutorFactoryTest {

  private MultipleNodeExecutionPlanner planner(final MultipleNodeExecutorFactory factory) {
    assertEquals(factory.getPlanner().getClass(), MultipleNodeExecutionPlanner.class);
    return (MultipleNodeExecutionPlanner) factory.getPlanner();
  }

  private CachingExecutionPlanner cachingPlanner(final MultipleNodeExecutorFactory factory) {
    assertEquals(factory.getPlanner().getClass(), CachingExecutionPlanner.class);
    return (CachingExecutionPlanner) factory.getPlanner();
  }

  public void testNoCaching() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    factory.setCacheManager(null);
    factory.afterPropertiesSet();
    assertNotNull(planner(factory));
    factory.invalidateCache();
  }

  public void testWithCaching() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    factory.setCacheManager(Mockito.mock(CacheManager.class));
    factory.afterPropertiesSet();
    assertNotNull(cachingPlanner(factory));
    factory.invalidateCache();
  }

  public void testMinimumJobItems() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    assertEquals(factory.getMinimumJobItems(), 1);
    assertEquals(planner(factory).getMinimumJobItems(), 1);
    factory.setMinimumJobItems(50);
    assertEquals(factory.getMinimumJobItems(), 50);
    assertEquals(planner(factory).getMinimumJobItems(), 50);
  }

  public void testMaximumJobItems() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    assertEquals(factory.getMaximumJobItems(), Integer.MAX_VALUE);
    assertEquals(planner(factory).getMaximumJobItems(), Integer.MAX_VALUE);
    factory.setMaximumJobItems(50);
    assertEquals(factory.getMaximumJobItems(), 50);
    assertEquals(planner(factory).getMaximumJobItems(), 50);
  }

  public void testMinimumJobCost() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    assertEquals(factory.getMinimumJobCost(), 0);
    assertEquals(planner(factory).getMinimumJobCost(), 0);
    factory.setMinimumJobCost(50);
    assertEquals(factory.getMinimumJobCost(), 50);
    assertEquals(planner(factory).getMinimumJobCost(), 50);
  }

  public void testMaximumJobCost() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    assertEquals(factory.getMaximumJobCost(), Long.MAX_VALUE);
    assertEquals(planner(factory).getMaximumJobCost(), Long.MAX_VALUE);
    factory.setMaximumJobCost(50);
    assertEquals(factory.getMaximumJobCost(), 50);
    assertEquals(planner(factory).getMaximumJobCost(), 50);
  }

  public void testMaximumConcurrency() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    assertEquals(factory.getMaximumConcurrency(), Integer.MAX_VALUE);
    assertEquals(planner(factory).getMaximumConcurrency(), Integer.MAX_VALUE);
    factory.setMaximumConcurrency(50);
    assertEquals(factory.getMaximumConcurrency(), 50);
    assertEquals(planner(factory).getMaximumConcurrency(), 50);
  }

  public void testFunctionCosts() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    FunctionCosts costs = factory.getFunctionCosts();
    assertSame(costs, planner(factory).getFunctionCosts());
    costs = new FunctionCosts();
    factory.setFunctionCosts(costs);
    assertSame(factory.getFunctionCosts(), costs);
    assertSame(planner(factory).getFunctionCosts(), costs);
  }

}

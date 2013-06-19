/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link BatchExecutorFactory} class
 */
@Test(groups = TestGroup.UNIT)
public class BatchExecutorFactoryTest {

  public void testCreateExecutor() {
    final BatchExecutorFactory factory = new BatchExecutorFactory(new SingleNodeExecutorFactory());
    final SingleComputationCycle cycle = Mockito.mock(SingleComputationCycle.class);
    final DependencyGraphExecutor executor = factory.createExecutor(cycle);
    assertEquals(executor.getClass(), BatchExecutor.class);
  }

  public void testToString() {
    final BatchExecutorFactory factory = new BatchExecutorFactory(new SingleNodeExecutorFactory());
    assertEquals(factory.toString(), "BatchExecutorFactory delegating to SingleNodeExecutorFactory");
  }

}

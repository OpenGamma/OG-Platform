/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DependencyNodeJobExecutionResultCache} class
 */
@Test(groups = TestGroup.UNIT)
public class DependencyNodeJobExecutionResultCacheTest {

  private ValueSpecification createValueSpec(final int id) {
    return new ValueSpecification("V", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").with("Id", Integer.toString(id)).get());
  }

  private DependencyNodeJobExecutionResult createExecutionResult() {
    return new DependencyNodeJobExecutionResult("Node", new CalculationJobResultItem(Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(),
        ExecutionLog.EMPTY), AggregatedExecutionLog.EMPTY);
  }

  public void testPutAndGet() {
    final DependencyNodeJobExecutionResultCache cache = new DependencyNodeJobExecutionResultCache();
    assertNull(cache.get(createValueSpec(1)));
    final DependencyNodeJobExecutionResult result = createExecutionResult();
    cache.put(createValueSpec(1), result);
    assertSame(cache.get(createValueSpec(1)), result);
  }

  public void testFind() {
    final DependencyNodeJobExecutionResultCache cache = new DependencyNodeJobExecutionResultCache();
    assertNull(cache.find(Collections.<ValueSpecification>emptySet()));
    final DependencyNodeJobExecutionResult result = createExecutionResult();
    cache.put(createValueSpec(1), result);
    assertNull(cache.find(Collections.singleton(createValueSpec(2))));
    assertSame(cache.find(ImmutableSet.of(createValueSpec(1), createValueSpec(2))), result);
  }

}

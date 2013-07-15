/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.EnumSet;

import org.testng.annotations.Test;

import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.calcnode.MutableExecutionLog;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DependencyNodeJobExecutionResult} class
 */
@Test(groups = TestGroup.UNIT)
public class DependencyNodeJobExecutionResultTest {

  public void testGetters() {
    final String nodeId = "Node";
    final CalculationJobResultItem resultItem = new CalculationJobResultItem(Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLog.EMPTY);
    final AggregatedExecutionLog logs = AggregatedExecutionLog.EMPTY;
    final DependencyNodeJobExecutionResult result = new DependencyNodeJobExecutionResult(nodeId, resultItem, logs);
    assertSame(result.getComputeNodeId(), nodeId);
    assertSame(result.getJobResultItem(), resultItem);
    assertSame(result.getAggregatedExecutionLog(), logs);
  }

  private DependencyNodeJobExecutionResult[] createResults() {
    final String nodeId1 = "Node1";
    final String nodeId2 = "Node2";
    final CalculationJobResultItem resultItem1 = new CalculationJobResultItem(Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLog.EMPTY);
    final CalculationJobResultItem resultItem2 = new CalculationJobResultItem(Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), MutableExecutionLog.single(
        SimpleLogEvent.of(LogLevel.WARN, "Foo"), ExecutionLogMode.FULL));
    final AggregatedExecutionLog logs1 = AggregatedExecutionLog.EMPTY;
    final AggregatedExecutionLog logs2 = new DefaultAggregatedExecutionLog(EnumSet.of(LogLevel.WARN), null, true);
    return new DependencyNodeJobExecutionResult[] {
        new DependencyNodeJobExecutionResult(nodeId1, resultItem1, logs1),
        new DependencyNodeJobExecutionResult(nodeId1, resultItem1, logs2),
        new DependencyNodeJobExecutionResult(nodeId2, resultItem1, logs1),
        new DependencyNodeJobExecutionResult(nodeId2, resultItem1, logs2),
        new DependencyNodeJobExecutionResult(nodeId1, resultItem2, logs1),
        new DependencyNodeJobExecutionResult(nodeId1, resultItem2, logs2),
        new DependencyNodeJobExecutionResult(nodeId2, resultItem2, logs1),
        new DependencyNodeJobExecutionResult(nodeId2, resultItem2, logs2) };
  }

  public void testHashCode() {
    final DependencyNodeJobExecutionResult[] resultsA = createResults();
    final DependencyNodeJobExecutionResult[] resultsB = createResults();
    for (int i = 0; i < resultsA.length; i++) {
      assertEquals(resultsA[i].hashCode(), resultsB[i].hashCode());
    }
  }

  public void testEquals() {
    final DependencyNodeJobExecutionResult[] resultsA = createResults();
    final DependencyNodeJobExecutionResult[] resultsB = createResults();
    for (int i = 0; i < resultsA.length; i++) {
      assertFalse(resultsA[i].equals(null));
      assertTrue(resultsA[i].equals(resultsA[i]));
      for (int j = 0; j < resultsB.length; j++) {
        if (i == j) {
          assertTrue(resultsA[i].equals(resultsB[j]));
        } else {
          assertFalse(resultsA[i].equals(resultsB[j]));
        }
      }
    }
  }

}

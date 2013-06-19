/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ExecutingGraph} class.
 */
@Test(groups = TestGroup.UNIT)
public class ExecutingGraphTest {

  private CalculationJobItem createItem(final int i) {
    return new CalculationJobItem(Integer.toString(i), new EmptyFunctionParameters(), ComputationTargetSpecification.NULL, Collections.<ValueSpecification>emptySet(),
        Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS);
  }

  private List<CalculationJobItem> createJobItems(final int i) {
    return Arrays.asList(createItem(i));
  }

  /**
   * <pre>
   *              J9
   *             /  \
   *           J8   J7
   *          /    / |
   *        J5    /  |
   *       /  \  /   |
   *     J3    J4    |  J3, J4, J5 and J8 are tails of J2
   *       \  /      |
   *        J2      /
   *        | \    /
   *       J1  \  /
   *            J6
   * </pre>
   */
  private GraphExecutionPlan create9JobPlan() {
    final PlannedJob job9 = new PlannedJob(2, createJobItems(9), CacheSelectHint.allShared(), null, null);
    final PlannedJob job8 = new PlannedJob(1, createJobItems(8), CacheSelectHint.allShared(), null, new PlannedJob[] {job9 });
    final PlannedJob job7 = new PlannedJob(2, createJobItems(7), CacheSelectHint.allShared(), null, new PlannedJob[] {job9 });
    final PlannedJob job5 = new PlannedJob(2, createJobItems(5), CacheSelectHint.allShared(), new PlannedJob[] {job8 }, null);
    final PlannedJob job3 = new PlannedJob(1, createJobItems(3), CacheSelectHint.allShared(), new PlannedJob[] {job5 }, null);
    final PlannedJob job4 = new PlannedJob(1, createJobItems(4), CacheSelectHint.allShared(), new PlannedJob[] {job5 }, new PlannedJob[] {job7 });
    final PlannedJob job2 = new PlannedJob(2, createJobItems(2), CacheSelectHint.allShared(), new PlannedJob[] {job3, job4 }, null);
    final PlannedJob job1 = new PlannedJob(0, createJobItems(1), CacheSelectHint.allShared(), null, new PlannedJob[] {job2 });
    final PlannedJob job6 = new PlannedJob(0, createJobItems(6), CacheSelectHint.allShared(), null, new PlannedJob[] {job2, job7 });
    return new GraphExecutionPlan("Default", 0, Arrays.asList(job1, job6), 0, 10d, 10d, 10d);
  }

  private void assertJob(final CalculationJob job, final int i) {
    assertEquals(job.getJobItems().size(), 1);
    assertEquals(job.getJobItems().get(0).getFunctionUniqueIdentifier(), Integer.toString(i));
  }

  public void basicTest() {
    final GraphExecutionPlan plan = create9JobPlan();
    final ExecutingGraph executing = new ExecutingGraph(plan, UniqueId.of("Cycle", "Test"), Instant.now(), VersionCorrection.LATEST);
    final CalculationJob job6 = executing.nextExecutableJob();
    assertJob(job6, 6);
    final CalculationJob job1 = executing.nextExecutableJob();
    assertJob(job1, 1);
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job1.getSpecification());
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job6.getSpecification());
    final CalculationJob job2 = executing.nextExecutableJob();
    assertJob(job2, 2);
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job6.getSpecification()); // Duplicate notification
    assertNull(executing.nextExecutableJob());
    assertNotNull(job2.getTail());
    assertEquals(job2.getTail().size(), 2);
    final Iterator<CalculationJob> itr = job2.getTail().iterator();
    final CalculationJob job3 = itr.next();
    assertJob(job3, 3);
    assertNull(job3.getTail());
    final CalculationJob job4 = itr.next();
    assertFalse(itr.hasNext());
    assertJob(job4, 4);
    assertNotNull(job4.getTail());
    assertEquals(job4.getTail().size(), 1);
    final CalculationJob job5 = job4.getTail().iterator().next();
    assertJob(job5, 5);
    assertNotNull(job5.getRequiredJobIds());
    assertEquals(job5.getRequiredJobIds().length, 2);
    assertEquals(job5.getRequiredJobIds()[0], job3.getSpecification().getJobId());
    assertEquals(job5.getRequiredJobIds()[1], job4.getSpecification().getJobId());
    assertNotNull(job5.getTail());
    assertEquals(job5.getTail().size(), 1);
    final CalculationJob job8 = job5.getTail().iterator().next();
    assertJob(job8, 8);
    executing.jobCompleted(job2.getSpecification());
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job3.getSpecification());
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job4.getSpecification());
    final CalculationJob job7 = executing.nextExecutableJob();
    assertJob(job7, 7);
    executing.jobCompleted(job5.getSpecification());
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job8.getSpecification());
    assertNull(executing.nextExecutableJob());
    executing.jobCompleted(job7.getSpecification());
    final CalculationJob job9 = executing.nextExecutableJob();
    assertJob(job9, 9);
    assertNull(executing.nextExecutableJob());
    assertFalse(executing.isFinished());
    executing.jobCompleted(job9.getSpecification());
    assertNull(executing.nextExecutableJob());
    assertTrue(executing.isFinished());
  }
}

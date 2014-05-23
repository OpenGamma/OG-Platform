/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the graph partitioning logic in {@link MultipleNodeExecutionPlanner}.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleNodeExecutionPlannerTest {

  private static final boolean PRINT_GRAPHS = false;

  /**
   * Test graph:
   * 
   * <pre>
   *      N0  N1  N4
   *        \ | /  |
   *          N2  N3
   *          |    |
   *           MDS
   * </pre>
   * 
   * If not partitioned:
   * <ul>
   * <li>v20, v21, v24, v34 to go into private cache
   * <li>v0x, v1x, v4x to go into shared cache (terminal outputs)
   * <li>vx2, vx3 to come from the shared cache (market data)
   * </ul>
   */
  private final ValueProperties _properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
  private final ValueSpecification _testValue20 = ValueSpecification.of("20", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "2"), _properties);
  private final ValueSpecification _testValue21 = ValueSpecification.of("21", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "2"), _properties);
  private final ValueSpecification _testValue24 = ValueSpecification.of("24", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "2"), _properties);
  private final ValueSpecification _testValue34 = ValueSpecification.of("34", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "3"), _properties);
  private final ValueRequirement _testRequirement0x = new ValueRequirement("0x", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "0"), ValueProperties.none());
  private final ValueSpecification _testValue0x = ValueSpecification.of("0x", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "0"), _properties);
  private final ValueRequirement _testRequirement1x = new ValueRequirement("1x", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "1"), ValueProperties.none());
  private final ValueSpecification _testValue1x = ValueSpecification.of("1x", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "1"), _properties);
  private final ValueRequirement _testRequirement4x = new ValueRequirement("4x", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "4"), ValueProperties.none());
  private final ValueSpecification _testValue4x = ValueSpecification.of("4x", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "4"), _properties);
  private final ValueSpecification _testValuex2 = ValueSpecification.of("x2", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "X"), _properties);
  private final ValueSpecification _testValuex3 = ValueSpecification.of("x3", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "X"), _properties);

  private TestDependencyGraphBuilder graphBuilder() {
    final TestDependencyGraphBuilder graph = new TestDependencyGraphBuilder("Default");
    NodeBuilder node = graph.addNode("Mock", _testValue0x.getTargetSpecification());
    node.addInput(_testValue20);
    node.addTerminalOutput(_testValue0x, _testRequirement0x);
    node = graph.addNode("Mock", _testValue1x.getTargetSpecification());
    node.addInput(_testValue21);
    node.addTerminalOutput(_testValue1x, _testRequirement1x);
    node = graph.addNode("Mock", _testValue20.getTargetSpecification());
    node.addInput(_testValuex2);
    node.addOutput(_testValue20);
    node.addOutput(_testValue21);
    node.addOutput(_testValue24);
    node = graph.addNode("Mock", _testValue34.getTargetSpecification());
    node.addInput(_testValuex3);
    node.addOutput(_testValue34);
    node = graph.addNode("Mock", _testValue4x.getTargetSpecification());
    node.addInput(_testValue24);
    node.addInput(_testValue34);
    node.addTerminalOutput(_testValue4x, _testRequirement4x);
    node = graph.addNode("MDS", _testValuex2.getTargetSpecification());
    node.addOutput(_testValuex2);
    node.addOutput(_testValuex3);
    return graph;
  }

  private MultipleNodeExecutionPlanner createPlanner(final int minimum, final int maximum, final int concurrency) {
    final MultipleNodeExecutionPlanner planner = new MultipleNodeExecutionPlanner();
    planner.setMininumJobItems(minimum);
    planner.setMaximimJobItems(maximum);
    planner.setMaximumConcurrency(concurrency);
    return planner;
  }

  private GraphExecutionPlan plan(final GraphExecutionPlanner planner, final DependencyGraph graph, final Set<ValueSpecification> sharedData) {
    return planner.createPlan(graph, new ExecutionLogModeSource(), 0, sharedData, Collections.<ValueSpecification, FunctionParameters>emptyMap());
  }

  /**
   * Graph untouched - single job.
   */
  public void testMin5() {
    final MultipleNodeExecutionPlanner planner = createPlanner(5, Integer.MAX_VALUE, Integer.MAX_VALUE);
    final GraphExecutionPlan plan = plan(planner, graphBuilder().buildGraph(), ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testMin5");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertEquals(job.getItems().size(), 5);
    final CacheSelectHint hint = job.getCacheSelectHint();
    if (PRINT_GRAPHS) {
      System.out.println(hint);
    }
    assertTrue(hint.isPrivateValue(_testValue20));
    assertTrue(hint.isPrivateValue(_testValue21));
    assertTrue(hint.isPrivateValue(_testValue24));
    assertTrue(hint.isPrivateValue(_testValue34));
    assertFalse(hint.isPrivateValue(_testValue0x));
    assertFalse(hint.isPrivateValue(_testValue1x));
    assertFalse(hint.isPrivateValue(_testValue4x));
    assertFalse(hint.isPrivateValue(_testValuex2));
    assertFalse(hint.isPrivateValue(_testValuex3));
    assertNull(job.getDependents());
    assertNull(job.getTails());
    assertEquals(job.getInputJobCount(), 0);
  }

  private boolean matchJob(final PlannedJob job, final ValueSpecification... outputs) {
    if (job.getItems().size() != outputs.length) {
      return false;
    }
    for (CalculationJobItem item : job.getItems()) {
      boolean match = false;
      for (ValueSpecification output : outputs) {
        for (ValueSpecification jobOutput : item.getOutputs()) {
          if (output.equals(jobOutput)) {
            match = true;
            break;
          }
        }
      }
      if (!match) {
        return false;
      }
    }
    return true;
  }

  /**
   * No changes to graph
   */
  public void testMax1() {
    final MultipleNodeExecutionPlanner planner = createPlanner(1, 1, 0);
    final DependencyGraph graph = graphBuilder().buildGraph();
    final GraphExecutionPlan plan = plan(planner, graph, ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testMax1");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 2);
    int mask = 0;
    for (PlannedJob job : plan.getLeafJobs()) {
      assertEquals(job.getInputJobCount(), 0);
      CacheSelectHint hint = job.getCacheSelectHint();
      if (matchJob(job, _testValue20)) {
        mask |= 1;
        assertEquals(job.getDependents().length, 3);
        assertNull(job.getTails());
        assertFalse(hint.isPrivateValue(_testValue20));
        assertFalse(hint.isPrivateValue(_testValue21));
        assertFalse(hint.isPrivateValue(_testValue24));
        assertFalse(hint.isPrivateValue(_testValuex2));
        for (PlannedJob job2 : job.getDependents()) {
          hint = job2.getCacheSelectHint();
          assertNull(job2.getDependents());
          assertNull(job2.getTails());
          if (matchJob(job2, _testValue0x)) {
            mask |= 2;
            assertEquals(job2.getInputJobCount(), 1);
            assertFalse(hint.isPrivateValue(_testValue0x));
            assertFalse(hint.isPrivateValue(_testValue20));
          } else if (matchJob(job2, _testValue1x)) {
            mask |= 4;
            assertEquals(job2.getInputJobCount(), 1);
            assertFalse(hint.isPrivateValue(_testValue1x));
            assertFalse(hint.isPrivateValue(_testValue21));
          } else if (matchJob(job2, _testValue4x)) {
            mask |= 8;
            assertEquals(job2.getInputJobCount(), 2);
            assertFalse(hint.isPrivateValue(_testValue4x));
            assertFalse(hint.isPrivateValue(_testValue24));
            assertFalse(hint.isPrivateValue(_testValue34));
          } else {
            fail();
          }
        }
      } else if (matchJob(job, _testValue34)) {
        mask |= 16;
        assertEquals(job.getDependents().length, 1);
        assertNull(job.getTails());
        assertFalse(hint.isPrivateValue(_testValue34));
        assertFalse(hint.isPrivateValue(_testValuex3));
        PlannedJob job2 = job.getDependents()[0];
        hint = job2.getCacheSelectHint();
        assertEquals(job2.getInputJobCount(), 2);
        assertNull(job2.getDependents());
        assertNull(job2.getTails());
        assertTrue(matchJob(job2, _testValue4x));
        assertFalse(hint.isPrivateValue(_testValue4x));
        assertFalse(hint.isPrivateValue(_testValue24));
        assertFalse(hint.isPrivateValue(_testValue34));
      } else {
        fail();
      }
    }
    assertEquals(mask, 31);
  }

  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3
   */
  public void testMinMax2() {
    final MultipleNodeExecutionPlanner planner = createPlanner(2, 2, 0);
    final GraphExecutionPlan plan = plan(planner, graphBuilder().buildGraph(), ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testMinMax2");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertEquals(job.getInputJobCount(), 0);
    CacheSelectHint hint = job.getCacheSelectHint();
    assertTrue(matchJob(job, _testValue20));
    assertFalse(hint.isPrivateValue(_testValue20));
    assertFalse(hint.isPrivateValue(_testValue21));
    assertEquals(job.getDependents().length, 2);
    assertNull(job.getTails());
    int mask = 0;
    for (PlannedJob job2 : job.getDependents()) {
      assertEquals(job2.getInputJobCount(), 1);
      assertNull(job2.getDependents());
      assertNull(job2.getTails());
      hint = job2.getCacheSelectHint();
      if (matchJob(job2, _testValue0x, _testValue1x)) {
        mask |= 1;
        assertFalse(hint.isPrivateValue(_testValue20));
        assertFalse(hint.isPrivateValue(_testValue21));
        assertFalse(hint.isPrivateValue(_testValue0x));
        assertFalse(hint.isPrivateValue(_testValue1x));
      } else if (matchJob(job2, _testValue34, _testValue4x)) {
        mask |= 2;
        assertFalse(hint.isPrivateValue(_testValue24));
        assertFalse(hint.isPrivateValue(_testValue4x));
        assertFalse(hint.isPrivateValue(_testValuex3));
        assertTrue(hint.isPrivateValue(_testValue34));
      } else {
        Assert.fail();
      }
    }
    assertEquals(mask, 3);
  }

  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3, input-merge on N(0+1)+N(4+3).
   */
  public void testMinMax4() {
    final MultipleNodeExecutionPlanner planner = createPlanner(3, 4, 0);
    final GraphExecutionPlan plan = plan(planner, graphBuilder().buildGraph(), ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testMinMax4");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    PlannedJob job = plan.getLeafJobs().iterator().next();
    assertEquals(job.getInputJobCount(), 0);
    assertTrue(matchJob(job, _testValue20));
    assertEquals(job.getDependents().length, 1);
    assertNull(job.getTails());
    CacheSelectHint hint = job.getCacheSelectHint();
    assertFalse(hint.isPrivateValue(_testValuex2));
    assertFalse(hint.isPrivateValue(_testValue20));
    assertFalse(hint.isPrivateValue(_testValue21));
    assertFalse(hint.isPrivateValue(_testValue24));
    PlannedJob job2 = job.getDependents()[0];
    assertNull(job2.getTails());
    assertEquals(job2.getInputJobCount(), 1);
    assertTrue(matchJob(job2, _testValue0x, _testValue1x, _testValue34, _testValue4x));
    assertNull(job2.getDependents());
    assertNull(job2.getTails());
    hint = job2.getCacheSelectHint();
    assertFalse(hint.isPrivateValue(_testValue0x));
    assertFalse(hint.isPrivateValue(_testValue1x));
    assertFalse(hint.isPrivateValue(_testValue4x));
    assertFalse(hint.isPrivateValue(_testValue20));
    assertFalse(hint.isPrivateValue(_testValue21));
    assertFalse(hint.isPrivateValue(_testValue24));
    assertFalse(hint.isPrivateValue(_testValuex3));
    assertTrue(hint.isPrivateValue(_testValue34));
  }

  /**
   * Single-dep merge N4+N3, single tail on N2 (one of N0, N1 or N(4+3)).
   */
  public void testThread1() {
    final MultipleNodeExecutionPlanner planner = createPlanner(1, 4, 1);
    final GraphExecutionPlan plan = plan(planner, graphBuilder().buildGraph(), ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testThread1");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertTrue(matchJob(job, _testValue20));
    assertEquals(job.getInputJobCount(), 0);
    assertEquals(job.getTails().length, 1);
    final PlannedJob tailJob = job.getTails()[0];
    final CacheSelectHint jobHint = job.getCacheSelectHint();
    final CacheSelectHint tailHint = tailJob.getCacheSelectHint();
    if (matchJob(tailJob, _testValue0x)) {
      assertTrue(jobHint.isPrivateValue(_testValue20));
      assertFalse(jobHint.isPrivateValue(_testValue21));
      assertFalse(jobHint.isPrivateValue(_testValue24));
      assertFalse(jobHint.isPrivateValue(_testValuex2));
      assertFalse(tailHint.isPrivateValue(_testValue0x));
      assertTrue(tailHint.isPrivateValue(_testValue20));
    } else if (matchJob(tailJob, _testValue1x)) {
      assertFalse(jobHint.isPrivateValue(_testValue20));
      assertTrue(jobHint.isPrivateValue(_testValue21));
      assertFalse(jobHint.isPrivateValue(_testValue24));
      assertFalse(jobHint.isPrivateValue(_testValuex2));
      assertFalse(tailHint.isPrivateValue(_testValue1x));
      assertTrue(tailHint.isPrivateValue(_testValue21));
    } else if (matchJob(tailJob, _testValue34, _testValue4x)) {
      assertFalse(jobHint.isPrivateValue(_testValue20));
      assertFalse(jobHint.isPrivateValue(_testValue21));
      assertTrue(jobHint.isPrivateValue(_testValue24));
      assertFalse(jobHint.isPrivateValue(_testValuex2));
      assertFalse(tailHint.isPrivateValue(_testValuex3));
      assertTrue(tailHint.isPrivateValue(_testValue34));
      assertFalse(tailHint.isPrivateValue(_testValue4x));
      assertTrue(tailHint.isPrivateValue(_testValue24));
    } else {
      fail();
    }
  }

  /**
   * Single-dep merge N4+N3, two tails on N2.
   */
  public void testThread2() {
    final MultipleNodeExecutionPlanner planner = createPlanner(1, Integer.MAX_VALUE, 2);
    final GraphExecutionPlan plan = plan(planner, graphBuilder().buildGraph(), ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testThread2");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertTrue(matchJob(job, _testValue20));
    assertEquals(job.getInputJobCount(), 0);
    assertEquals(job.getTails().length, 2);
    int mask = 0;
    final CacheSelectHint jobHint = job.getCacheSelectHint();
    for (PlannedJob tail : job.getTails()) {
      assertEquals(tail.getInputJobCount(), 1);
      assertNull(tail.getDependents());
      assertNull(tail.getTails());
      final CacheSelectHint tailHint = tail.getCacheSelectHint();
      if (matchJob(tail, _testValue0x)) {
        mask |= 1;
        assertTrue(jobHint.isPrivateValue(_testValue20));
        assertTrue(tailHint.isPrivateValue(_testValue20));
        assertFalse(tailHint.isPrivateValue(_testValue0x));
      } else if (matchJob(tail, _testValue1x)) {
        mask |= 2;
        assertTrue(jobHint.isPrivateValue(_testValue21));
        assertTrue(tailHint.isPrivateValue(_testValue21));
        assertFalse(tailHint.isPrivateValue(_testValue1x));
      } else if (matchJob(tail, _testValue34, _testValue4x)) {
        mask |= 4;
        assertTrue(jobHint.isPrivateValue(_testValue24));
        assertTrue(tailHint.isPrivateValue(_testValue24));
        assertTrue(tailHint.isPrivateValue(_testValue34));
        assertFalse(tailHint.isPrivateValue(_testValuex3));
        assertFalse(tailHint.isPrivateValue(_testValue4x));
      } else {
        fail();
      }
    }
    assertTrue((mask == 3) || (mask == 5) || (mask == 6));
  }

  /**
   * Single-dep merge N4+N3, three tails on N2.
   */
  public void testThread3() {
    final MultipleNodeExecutionPlanner planner = createPlanner(1, Integer.MAX_VALUE, 3);
    final GraphExecutionPlan plan = plan(planner, graphBuilder().buildGraph(), ImmutableSet.of(_testValuex2, _testValuex3));
    if (PRINT_GRAPHS) {
      System.out.println("testThread3");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertTrue(matchJob(job, _testValue20));
    assertEquals(job.getInputJobCount(), 0);
    assertEquals(job.getTails().length, 3);
    int mask = 0;
    final CacheSelectHint jobHint = job.getCacheSelectHint();
    for (PlannedJob tail : job.getTails()) {
      assertEquals(tail.getInputJobCount(), 1);
      assertNull(tail.getDependents());
      assertNull(tail.getTails());
      final CacheSelectHint tailHint = tail.getCacheSelectHint();
      if (matchJob(tail, _testValue0x)) {
        mask |= 1;
        assertTrue(jobHint.isPrivateValue(_testValue20));
        assertTrue(tailHint.isPrivateValue(_testValue20));
        assertFalse(tailHint.isPrivateValue(_testValue0x));
      } else if (matchJob(tail, _testValue1x)) {
        mask |= 2;
        assertTrue(jobHint.isPrivateValue(_testValue21));
        assertTrue(tailHint.isPrivateValue(_testValue21));
        assertFalse(tailHint.isPrivateValue(_testValue1x));
      } else if (matchJob(tail, _testValue34, _testValue4x)) {
        mask |= 4;
        assertTrue(jobHint.isPrivateValue(_testValue24));
        assertTrue(tailHint.isPrivateValue(_testValue24));
        assertTrue(tailHint.isPrivateValue(_testValue34));
        assertFalse(tailHint.isPrivateValue(_testValuex3));
        assertFalse(tailHint.isPrivateValue(_testValue4x));
      } else {
        fail();
      }
    }
    assertEquals(mask, 7);
  }

  private void gatherTailColours(final PlannedJob job, final Set<PlannedJob> colouredJobs) {
    if (job.getTails() != null) {
      for (PlannedJob tail : job.getTails()) {
        gatherTailColours(tail, colouredJobs);
      }
    }
    if (job.getDependents() != null) {
      for (PlannedJob dependent : job.getDependents()) {
        gatherColours(dependent, colouredJobs);
      }
    }
  }

  private void gatherColours(final PlannedJob job, final Set<PlannedJob> colouredJobs) {
    if (colouredJobs.add(job)) {
      gatherTailColours(job, colouredJobs);
    }
  }

  private int gatherColours(final GraphExecutionPlan plan) {
    final Set<PlannedJob> jobs = new HashSet<PlannedJob>();
    for (PlannedJob job : plan.getLeafJobs()) {
      gatherColours(job, jobs);
    }
    return jobs.size();
  }

  /**
   * <pre>
   *  N0    N1  N7  N4
   *   |\   ||  ||  ||
   *   | \  ++==++==++
   *   |  \ |       |
   *   |   N2      N3
   *   |     \    /
   *  N6       N5
   * </pre>
   * <ul>
   * <li>Should be broken into N{5, 2, 3, 1, 4, 7}, N6 and N0 at 3x concurrency
   * <li>Should be broken into N{5, 2, 3, [two of {1, 4, 7}]}, N6, N0 and other of N{1, 4, 7} at 2x concurrency
   * <li>Should be broken into N{5, 2} or N{5, 3} and others at 1x concurrency
   * </ul>
   */
  public void testTailGraphColouring() {
    final TestDependencyGraphBuilder graph = graphBuilder();
    final ValueSpecification testValue60 = ValueSpecification.of("60", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "6"), _properties);
    NodeBuilder node = graph.addNode("MDS", testValue60.getTargetSpecification());
    node.addOutput(testValue60);
    graph.getNode(0).addInput(testValue60);
    final ValueSpecification testValue7x = ValueSpecification.of("7x", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "7"), _properties);
    node = graph.addNode("Mock", testValue7x.getTargetSpecification());
    node.addInput(_testValue21);
    node.addInput(_testValue34);
    node.addTerminalOutput(testValue7x, testValue7x.toRequirementSpecification());
    graph.getNode(1).addInput(_testValue34);
    MultipleNodeExecutionPlanner planner = createPlanner(1, 1, 1);
    GraphExecutionPlan plan = plan(planner, graph.buildGraph(), Collections.<ValueSpecification>emptySet());
    if (PRINT_GRAPHS) {
      System.out.println("Concurrency 1");
      plan.print();
    }
    assertEquals(gatherColours(plan), 7);
    planner = createPlanner(1, 1, 2);
    plan = plan(planner, graph.buildGraph(), Collections.<ValueSpecification>emptySet());
    if (PRINT_GRAPHS) {
      System.out.println("Concurrency 2");
      plan.print();
    }
    assertEquals(gatherColours(plan), 4);
    planner = createPlanner(1, 1, 3);
    plan = plan(planner, graph.buildGraph(), Collections.<ValueSpecification>emptySet());
    if (PRINT_GRAPHS) {
      System.out.println("Concurrency 3");
      plan.print();
    }
    assertEquals(gatherColours(plan), 3);
  }

}

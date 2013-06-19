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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
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
   * </pre>
   * 
   * If not partitioned:
   * <ul>
   * <li>v20, v21, v24, v34 to go into private cache
   * <li>v0x, v1x, v4x to go into shared cache
   * <li>vx2, vx3 to go into shared cache
   * </ul>
   */
  private DependencyNode[] _testNode;
  private DependencyGraph _testGraph;
  private final ValueProperties _properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
  private final ValueSpecification _testValue20 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "20"), _properties);
  private final ValueSpecification _testValue21 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "21"), _properties);
  private final ValueSpecification _testValue24 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "24"), _properties);
  private final ValueSpecification _testValue34 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "34"), _properties);
  private final ValueRequirement _testRequirement0x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "0x"), ValueProperties.none());
  private final ValueSpecification _testValue0x = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "0x"), _properties);
  private final ValueRequirement _testRequirement1x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "1x"), ValueProperties.none());
  private final ValueSpecification _testValue1x = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "1x"), _properties);
  private final ValueRequirement _testRequirement4x = new ValueRequirement("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "4x"), ValueProperties.none());
  private final ValueSpecification _testValue4x = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "4x"), _properties);
  private final ValueSpecification _testValuex2 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "x2"), _properties);
  private final ValueSpecification _testValuex3 = ValueSpecification.of("Test", ComputationTargetType.PRIMITIVE, UniqueId.of("test", "x3"), _properties);

  @BeforeMethod
  public void createGraph() {
    _testGraph = new DependencyGraph("Default");
    _testNode = new DependencyNode[5];
    for (int i = 0; i < _testNode.length; i++) {
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", Integer.toString(i)));
      _testNode[i] = new DependencyNode(target);
      _testNode[i].setFunction(MockFunction.getMockFunction(target, "foo"));
    }
    _testNode[0].addOutputValue(_testValue0x);
    _testNode[1].addOutputValue(_testValue1x);
    _testNode[2].addOutputValue(_testValue20);
    _testNode[2].addOutputValue(_testValue21);
    _testNode[2].addOutputValue(_testValue24);
    _testNode[3].addOutputValue(_testValue34);
    _testNode[4].addOutputValue(_testValue4x);
    _testNode[0].addInputNode(_testNode[2]);
    _testNode[0].addInputValue(_testValue20);
    _testNode[1].addInputNode(_testNode[2]);
    _testNode[1].addInputValue(_testValue21);
    _testNode[2].addInputValue(_testValuex2);
    _testNode[3].addInputValue(_testValuex3);
    _testNode[4].addInputNode(_testNode[2]);
    _testNode[4].addInputValue(_testValue24);
    _testNode[4].addInputNode(_testNode[3]);
    _testNode[4].addInputValue(_testValue34);
    for (final DependencyNode a_testNode : _testNode) {
      _testGraph.addDependencyNode(a_testNode);
    }
    _testGraph.addTerminalOutput(_testRequirement0x, _testValue0x);
    _testGraph.addTerminalOutput(_testRequirement1x, _testValue1x);
    _testGraph.addTerminalOutput(_testRequirement4x, _testValue4x);
  }

  private MultipleNodeExecutionPlanner createPlanner(final int minimum, final int maximum, final int concurrency) {
    final MultipleNodeExecutionPlanner planner = new MultipleNodeExecutionPlanner();
    planner.setMininumJobItems(minimum);
    planner.setMaximimJobItems(maximum);
    planner.setMaximumConcurrency(concurrency);
    return planner;
  }

  private GraphExecutionPlan plan(final GraphExecutionPlanner planner, final DependencyGraph graph) {
    return planner.createPlan(graph, new ExecutionLogModeSource(), 0);
  }

  /**
   * Graph untouched - single job.
   */
  public void testMin5() {
    final MultipleNodeExecutionPlanner planner = createPlanner(5, Integer.MAX_VALUE, Integer.MAX_VALUE);
    final GraphExecutionPlan plan = plan(planner, _testGraph);
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

  private boolean matchJob(final PlannedJob job, final DependencyNode... nodes) {
    if (job.getItems().size() != nodes.length) {
      return false;
    }
    for (CalculationJobItem item : job.getItems()) {
      final ComputationTargetSpecification target = item.getComputationTargetSpecification();
      boolean match = false;
      for (DependencyNode node : nodes) {
        if (target.equals(node.getComputationTarget())) {
          match = true;
          break;
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
    final GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMax1");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 2);
    int mask = 0;
    for (PlannedJob job : plan.getLeafJobs()) {
      assertEquals(job.getInputJobCount(), 0);
      CacheSelectHint hint = job.getCacheSelectHint();
      if (matchJob(job, _testNode[2])) {
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
          if (matchJob(job2, _testNode[0])) {
            mask |= 2;
            assertEquals(job2.getInputJobCount(), 1);
            assertFalse(hint.isPrivateValue(_testValue0x));
            assertFalse(hint.isPrivateValue(_testValue20));
          } else if (matchJob(job2, _testNode[1])) {
            mask |= 4;
            assertEquals(job2.getInputJobCount(), 1);
            assertFalse(hint.isPrivateValue(_testValue1x));
            assertFalse(hint.isPrivateValue(_testValue21));
          } else if (matchJob(job2, _testNode[4])) {
            mask |= 8;
            assertEquals(job2.getInputJobCount(), 2);
            assertFalse(hint.isPrivateValue(_testValue4x));
            assertFalse(hint.isPrivateValue(_testValue24));
            assertFalse(hint.isPrivateValue(_testValue34));
          } else {
            fail();
          }
        }
      } else if (matchJob(job, _testNode[3])) {
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
        assertTrue(matchJob(job2, _testNode[4]));
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
    final GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMinMax2");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertEquals(job.getInputJobCount(), 0);
    CacheSelectHint hint = job.getCacheSelectHint();
    assertTrue(matchJob(job, _testNode[2]));
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
      if (matchJob(job2, _testNode[0], _testNode[1])) {
        mask |= 1;
        assertFalse(hint.isPrivateValue(_testValue20));
        assertFalse(hint.isPrivateValue(_testValue21));
        assertFalse(hint.isPrivateValue(_testValue0x));
        assertFalse(hint.isPrivateValue(_testValue1x));
      } else if (matchJob(job2, _testNode[3], _testNode[4])) {
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
    final GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMinMax4");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    PlannedJob job = plan.getLeafJobs().iterator().next();
    assertEquals(job.getInputJobCount(), 0);
    assertTrue(matchJob(job, _testNode[2]));
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
    assertTrue(matchJob(job2, _testNode[0], _testNode[1], _testNode[3], _testNode[4]));
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
    final GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testThread1");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertTrue(matchJob(job, _testNode[2]));
    assertEquals(job.getInputJobCount(), 0);
    assertEquals(job.getTails().length, 1);
    final PlannedJob tailJob = job.getTails()[0];
    final CacheSelectHint jobHint = job.getCacheSelectHint();
    final CacheSelectHint tailHint = tailJob.getCacheSelectHint();
    if (matchJob(tailJob, _testNode[0])) {
      assertTrue(jobHint.isPrivateValue(_testValue20));
      assertFalse(jobHint.isPrivateValue(_testValue21));
      assertFalse(jobHint.isPrivateValue(_testValue24));
      assertFalse(jobHint.isPrivateValue(_testValuex2));
      assertFalse(tailHint.isPrivateValue(_testValue0x));
      assertTrue(tailHint.isPrivateValue(_testValue20));
    } else if (matchJob(tailJob, _testNode[1])) {
      assertFalse(jobHint.isPrivateValue(_testValue20));
      assertTrue(jobHint.isPrivateValue(_testValue21));
      assertFalse(jobHint.isPrivateValue(_testValue24));
      assertFalse(jobHint.isPrivateValue(_testValuex2));
      assertFalse(tailHint.isPrivateValue(_testValue1x));
      assertTrue(tailHint.isPrivateValue(_testValue21));
    } else if (matchJob(tailJob, _testNode[3], _testNode[4])) {
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
    final GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testThread2");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertTrue(matchJob(job, _testNode[2]));
    assertEquals(job.getInputJobCount(), 0);
    assertEquals(job.getTails().length, 2);
    int mask = 0;
    final CacheSelectHint jobHint = job.getCacheSelectHint();
    for (PlannedJob tail : job.getTails()) {
      assertEquals(tail.getInputJobCount(), 1);
      assertNull(tail.getDependents());
      assertNull(tail.getTails());
      final CacheSelectHint tailHint = tail.getCacheSelectHint();
      if (matchJob(tail, _testNode[0])) {
        mask |= 1;
        assertTrue(jobHint.isPrivateValue(_testValue20));
        assertTrue(tailHint.isPrivateValue(_testValue20));
        assertFalse(tailHint.isPrivateValue(_testValue0x));
      } else if (matchJob(tail, _testNode[1])) {
        mask |= 2;
        assertTrue(jobHint.isPrivateValue(_testValue21));
        assertTrue(tailHint.isPrivateValue(_testValue21));
        assertFalse(tailHint.isPrivateValue(_testValue1x));
      } else if (matchJob(tail, _testNode[3], _testNode[4])) {
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
    final GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testThread3");
      plan.print();
    }
    assertEquals(plan.getLeafJobs().size(), 1);
    final PlannedJob job = plan.getLeafJobs().iterator().next();
    assertTrue(matchJob(job, _testNode[2]));
    assertEquals(job.getInputJobCount(), 0);
    assertEquals(job.getTails().length, 3);
    int mask = 0;
    final CacheSelectHint jobHint = job.getCacheSelectHint();
    for (PlannedJob tail : job.getTails()) {
      assertEquals(tail.getInputJobCount(), 1);
      assertNull(tail.getDependents());
      assertNull(tail.getTails());
      final CacheSelectHint tailHint = tail.getCacheSelectHint();
      if (matchJob(tail, _testNode[0])) {
        mask |= 1;
        assertTrue(jobHint.isPrivateValue(_testValue20));
        assertTrue(tailHint.isPrivateValue(_testValue20));
        assertFalse(tailHint.isPrivateValue(_testValue0x));
      } else if (matchJob(tail, _testNode[1])) {
        mask |= 2;
        assertTrue(jobHint.isPrivateValue(_testValue21));
        assertTrue(tailHint.isPrivateValue(_testValue21));
        assertFalse(tailHint.isPrivateValue(_testValue1x));
      } else if (matchJob(tail, _testNode[3], _testNode[4])) {
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
   * N0 N1N7N4
   * | \ |X|
   * |  N2 N3
   * |    \/
   * N6   N5
   * </pre>
   * <ul>
   * <li>Should be broken into N{5, 2, 3, 1, 4, 7}, N6 and N0 at 3x concurrency
   * <li>Should be broken into N{5, 2, 3, [two of {1, 4, 7}]}, N6, N0 and other of N{1, 4, 7} at 2x concurrency
   * <li>Should be broken into N{5, 2} or N{5, 3} and others at 1x concurrency
   * </ul>
   */
  public void testTailGraphColouring() {
    final ComputationTarget t5 = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "5"));
    final DependencyNode n5 = new DependencyNode(t5);
    n5.setFunction(MockFunction.getMockFunction(t5, "foo"));
    final ComputationTarget t6 = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "6"));
    final DependencyNode n6 = new DependencyNode(t6);
    n6.setFunction(MockFunction.getMockFunction(t6, "foo"));
    final ComputationTarget t7 = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "7"));
    final DependencyNode n7 = new DependencyNode(t7);
    n7.setFunction(MockFunction.getMockFunction(t7, "foo"));
    _testNode[0].addInputNode(n6);
    _testNode[1].addInputNode(_testNode[3]);
    _testNode[2].addInputNode(n5);
    _testNode[3].addInputNode(n5);
    n7.addInputNode(_testNode[2]);
    n7.addInputNode(_testNode[3]);
    _testGraph.addDependencyNode(n5);
    _testGraph.addDependencyNode(n6);
    _testGraph.addDependencyNode(n7);
    MultipleNodeExecutionPlanner planner = createPlanner(1, 1, 1);
    GraphExecutionPlan plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("Concurrency 1");
      plan.print();
    }
    assertEquals(gatherColours(plan), 7);
    planner = createPlanner(1, 1, 2);
    plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("Concurrency 2");
      plan.print();
    }
    assertEquals(gatherColours(plan), 4);
    planner = createPlanner(1, 1, 3);
    plan = plan(planner, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("Concurrency 3");
      plan.print();
    }
    assertEquals(gatherColours(plan), 3);
  }

}

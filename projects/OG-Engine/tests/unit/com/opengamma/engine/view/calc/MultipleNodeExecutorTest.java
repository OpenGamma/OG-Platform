/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.util.Cancellable;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Tests the graph partitioning logic in MultipleNodeExecutor.
 */
public class MultipleNodeExecutorTest {

  /**
   * Test graph:
   * 
   *      N0  N1  N4
   *        \ | /  |
   *          N2  N3
   * 
   */
  private DependencyNode[] _testNode;
  private DependencyGraph _testGraph;

  @Before
  public void createGraph() {
    _testGraph = new DependencyGraph("Default");
    _testNode = new DependencyNode[5];
    for (int i = 0; i < _testNode.length; i++) {
      _testNode[i] = new DependencyNode(new ComputationTarget(Integer.toString(i)));
      _testNode[i].setFunction(MockFunction.getMockFunction(_testNode[i].getComputationTarget(), "foo"));
    }
    _testNode[0].addInputNode(_testNode[2]);
    _testNode[1].addInputNode(_testNode[2]);
    _testNode[4].addInputNode(_testNode[2]);
    _testNode[4].addInputNode(_testNode[3]);
    for (int i = 0; i < _testNode.length; i++) {
      _testGraph.addDependencyNode(_testNode[i]);
    }
  }

  private MultipleNodeExecutor createExecutor(final int minimum, final int maximum, final int concurrency) {
    return new MultipleNodeExecutor(null, minimum, maximum, minimum, maximum, concurrency, new FunctionCosts(), new ExecutionPlanCache (EHCacheUtils.createCacheManager(), 0)) {
      
      @Override
      protected long getFunctionInitId () {
        return 0;
      }

      @Override
      protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
        return new CalculationJobSpecification("Test View", graph.getCalcConfName(), 1L, JobIdSource.getId());
      }

      @Override
      protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
        // Nothing
      }

      @Override
      protected Cancellable dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
        // No action - we're just testing graph construction
        return new Cancellable () {
          @Override
          public boolean cancel (final boolean mayInterrupt) {
            return false;
          }
        };
      }

    };
  }

  /**
   * Graph untouched - single job.
   */
  @Test
  public void testMin5() {
    final MultipleNodeExecutor executor = createExecutor(5, Integer.MAX_VALUE, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testMin5");
    executor.printFragment(root);
    assertEquals(5, root.getNodes().size());
  }

  private boolean singletonFragment(final GraphFragment fragment, final DependencyNode node) {
    assertEquals(1, fragment.getNodes().size());
    return fragment.getNodes().iterator().next() == node;
  }

  /**
   * No changes to graph
   */
  @Test
  public void testMax1() {
    final MultipleNodeExecutor executor = createExecutor(1, 1, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testMax1");
    executor.printFragment(root);
    assertEquals(3, root.getInputFragments().size());
    int mask = 0;
    for (GraphFragment fragment : root.getInputFragments()) {
      if (singletonFragment(fragment, _testNode[0])) {
        mask |= 1;
        assertEquals(1, fragment.getInputFragments().size());
        assertTrue(singletonFragment(fragment.getInputFragments().iterator().next(), _testNode[2]));
      } else if (singletonFragment(fragment, _testNode[1])) {
        mask |= 2;
        assertEquals(1, fragment.getInputFragments().size());
        assertTrue(singletonFragment(fragment.getInputFragments().iterator().next(), _testNode[2]));
      } else if (singletonFragment(fragment, _testNode[4])) {
        mask |= 4;
        assertEquals(2, fragment.getInputFragments().size());
        for (GraphFragment fragment2 : fragment.getInputFragments()) {
          if (singletonFragment(fragment2, _testNode[2])) {
            mask |= 8;
          } else if (singletonFragment(fragment2, _testNode[3])) {
            mask |= 16;
          } else {
            fail();
          }
        }
      } else {
        fail();
      }
    }
    assertEquals(31, mask);
  }

  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3
   */
  @Test
  public void testMinMax2() {
    final MultipleNodeExecutor executor = createExecutor(2, 2, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testMinMax2");
    executor.printFragment(root);
    assertEquals(2, root.getInputFragments().size());
    int mask = 0;
    for (GraphFragment fragment : root.getInputFragments()) {
      assertEquals(2, fragment.getNodes().size());
      if (fragment.getNodes().contains(_testNode[0]) && fragment.getNodes().contains(_testNode[1])) {
        mask |= 1;
        assertEquals(1, fragment.getInputFragments().size());
        assertTrue(singletonFragment(fragment.getInputFragments().iterator().next(), _testNode[2]));
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 2;
        assertEquals(1, fragment.getInputFragments().size());
        assertTrue(singletonFragment(fragment.getInputFragments().iterator().next(), _testNode[2]));
      } else {
        fail();
      }
    }
    assertEquals(3, mask);
  }

  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3, input-merge on N(0+1)+N(4+3).
   */
  @Test
  public void testMinMax4() {
    final MultipleNodeExecutor executor = createExecutor(3, 4, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testMinMax4");
    executor.printFragment(root);
    assertEquals(1, root.getInputFragments().size());
    GraphFragment node = root.getInputFragments().iterator().next();
    assertEquals(4, node.getNodes().size());
    assertTrue(node.getNodes().contains(_testNode[0]));
    assertTrue(node.getNodes().contains(_testNode[1]));
    assertTrue(node.getNodes().contains(_testNode[4]));
    assertTrue(node.getNodes().contains(_testNode[3]));
    assertEquals(1, node.getInputFragments().size());
    assertTrue(singletonFragment(node.getInputFragments().iterator().next(), _testNode[2]));
  }

  /**
   * Single-dep merge N4+N3, single tail on N2 (one of N0, N1 or N(4+3)).
   */
  @Test
  public void testThread1() {
    final MultipleNodeExecutor executor = createExecutor(1, 4, 1);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testThread1");
    executor.printFragment(root);
    assertEquals(3, root.getInputFragments().size());
    int mask = 0;
    for (GraphFragment fragment : root.getInputFragments()) {
      assertEquals(1, fragment.getInputFragments().size());
      GraphFragment input = fragment.getInputFragments().iterator().next();
      assertTrue(singletonFragment(input, _testNode[2]));
      assertEquals(1, input.getTail().size());
      if (fragment.getNodes().contains(_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes().contains(_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 4;
      } else {
        fail();
      }
    }
    assertEquals(7, mask);
  }

  /**
   * Single-dep merge N4+N3, two tails on N2.
   */
  @Test
  public void testThread2() {
    final MultipleNodeExecutor executor = createExecutor(1, Integer.MAX_VALUE, 2);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testThread2");
    executor.printFragment(root);
    assertEquals(3, root.getInputFragments().size());
    int mask = 0;
    for (GraphFragment fragment : root.getInputFragments()) {
      assertEquals(1, fragment.getInputFragments().size());
      GraphFragment input = fragment.getInputFragments().iterator().next();
      assertTrue(singletonFragment(input, _testNode[2]));
      assertEquals(2, input.getTail().size());
      if (fragment.getNodes().contains(_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes().contains(_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 4;
      } else {
        fail();
      }
    }
    assertEquals(7, mask);
  }

  /**
   * Single-dep merge N4+N3, three tails on N2.
   */
  @Test
  public void testThread3() {
    final MultipleNodeExecutor executor = createExecutor(1, Integer.MAX_VALUE, 3);
    final RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println("testThread3");
    executor.printFragment(root);
    assertEquals(3, root.getInputFragments().size());
    int mask = 0;
    for (GraphFragment fragment : root.getInputFragments()) {
      assertEquals(1, fragment.getInputFragments().size());
      GraphFragment input = fragment.getInputFragments().iterator().next();
      assertTrue(singletonFragment(input, _testNode[2]));
      assertEquals(3, input.getTail().size());
      if (fragment.getNodes().contains(_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes().contains(_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 4;
      } else {
        fail();
      }
    }
    assertEquals(7, mask);
  }
  
  private void extractColours (final GraphFragment fragment, final Map<Integer, Collection<DependencyNode>> colours) {
    if (fragment.getExecutionId () != 0) {
      Collection<DependencyNode> nodes = colours.get(fragment.getExecutionId ());
      if (nodes == null) {
        nodes = new HashSet<DependencyNode> ();
        colours.put (fragment.getExecutionId(), nodes);
      }
      nodes.addAll(fragment.getNodes ());
    }
    for (GraphFragment input : fragment.getInputFragments ()) {
      extractColours (input, colours);
    }
  }

  /**
   * N0 N1N7N4
   * | \ |X|
   * |  N2 N3
   * |    \/
   * N6   N5
   * 
   * Should be broken into N{5, 2, 3, 1, 4, 7}, N6 and N0 at 3x concurrency
   * Should be broken into N{5, 2, 3, [two of {1, 4, 7}]}, N6, N0 and other of N{1, 4, 7} at 2x concurrency
   * Should be broken into N{5, 2} or N{5, 3} and others at 1x concurrency
   */
  @Test
  public void testTailGraphColouring() {
    final DependencyNode n5 = new DependencyNode(new ComputationTarget("5"));
    n5.setFunction(MockFunction.getMockFunction(n5.getComputationTarget(), "foo"));
    final DependencyNode n6 = new DependencyNode(new ComputationTarget("6"));
    n6.setFunction(MockFunction.getMockFunction(n6.getComputationTarget(), "foo"));
    final DependencyNode n7 = new DependencyNode(new ComputationTarget("7"));
    n7.setFunction(MockFunction.getMockFunction(n6.getComputationTarget(), "foo"));
    _testNode[0].addInputNode(n6);
    _testNode[1].addInputNode(_testNode[3]);
    _testNode[2].addInputNode(n5);
    _testNode[3].addInputNode(n5);
    n7.addInputNode (_testNode[2]);
    n7.addInputNode(_testNode[3]);
    _testGraph.addDependencyNode(n5);
    _testGraph.addDependencyNode(n6);
    _testGraph.addDependencyNode(n7);
    System.out.println("testTailGraphColouring");
    System.out.println ("concurrency 1");
    MultipleNodeExecutor executor = createExecutor(1, 1, 1);
    RootGraphFragment root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    Map<Integer, Collection<DependencyNode>> colours = new HashMap<Integer, Collection<DependencyNode>>();
    extractColours(root, colours);
    System.out.println (colours);
    assertEquals (7, colours.size ());
    System.out.println ("concurrency 2");
    executor = createExecutor(1,1,2);
    root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    colours.clear ();
    extractColours(root, colours);
    System.out.println (colours);
    assertEquals (4, colours.size ());
    System.out.println ("concurrency 3");
    executor = createExecutor(1,1,3);
    root = executor.createExecutionPlan(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    colours.clear ();
    extractColours(root, colours);
    System.out.println (colours);
    assertEquals (3, colours.size ());
    int mask = 0;
    for (Map.Entry<Integer, Collection<DependencyNode>> colour : colours.entrySet ()) {
      if (colour.getValue ().contains (n6)) {
        assertEquals (1, colour.getValue ().size ());
        mask |= 1;
      } else if (colour.getValue ().contains (_testNode[0])) {
        assertEquals (1, colour.getValue ().size ());
        mask |= 2;
      } else if (colour.getValue ().contains (n5)) {
        assertTrue (colour.getValue ().containsAll(Arrays.asList (_testNode[1], _testNode[2], _testNode[3], _testNode[4])));
        mask |= 4;
      } else {
        fail ();
      }
    }
    assertEquals(7, mask);
  }

}

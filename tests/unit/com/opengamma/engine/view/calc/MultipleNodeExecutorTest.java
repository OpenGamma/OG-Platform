/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.opengamma.engine.view.calcnode.stats.FunctionCost;

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
  public void createGraph () {
    _testGraph = new DependencyGraph("Default");
    _testNode = new DependencyNode[5];
    for (int i = 0; i < _testNode.length; i++) {
      _testNode[i] = new DependencyNode(new ComputationTarget (Integer.toString(i)));
      _testNode[i].setFunction(MockFunction.getMockFunction (_testNode[i].getComputationTarget (), "foo"));
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
    return new MultipleNodeExecutor(null, minimum, maximum, minimum, maximum, concurrency, new FunctionCost ()) {

      @Override
      protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
        return new CalculationJobSpecification("Test View", graph.getCalcConfName(), 1L, JobIdSource.getId());
      }

      @Override
      protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
        // Nothing
      }

      @Override
      protected void dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
        // No action - we're just testing graph construction
      }

    };
  }

  /**
   * Graph untouched - single job.
   */
  @Test
  public void testMin5 () {
    final MultipleNodeExecutor executor = createExecutor(5, Integer.MAX_VALUE, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testMin5");
    executor.printFragment(root);
    assertEquals (5, root.getNodes ().size ());
  }
  
  private boolean singletonFragment (final GraphFragment fragment, final DependencyNode node) {
    assertEquals (1, fragment.getNodes ().size ());
    return fragment.getNodes ().iterator().next () == node;
  }
  
  /**
   * No changes to graph
   */
  @Test
  public void testMax1 () {
    final MultipleNodeExecutor executor = createExecutor(1, 1, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testMax1");
    executor.printFragment(root);
    assertEquals (3, root.getInputs ().size ());
    int mask = 0;
    for (GraphFragment fragment : root.getInputs ()) {
      if (singletonFragment (fragment, _testNode[0])) {
        mask |= 1;
        assertEquals (1, fragment.getInputs ().size ());
        assertTrue (singletonFragment (fragment.getInputs ().iterator ().next (), _testNode[2]));
      } else if (singletonFragment (fragment, _testNode[1])) {
        mask |= 2;
        assertEquals (1, fragment.getInputs ().size ());
        assertTrue (singletonFragment (fragment.getInputs ().iterator ().next (), _testNode[2]));
      } else if (singletonFragment (fragment, _testNode[4])) {
        mask |= 4;
        assertEquals (2, fragment.getInputs ().size ());
        for (GraphFragment fragment2 : fragment.getInputs ()) {
          if (singletonFragment (fragment2, _testNode[2])) {
            mask |= 8;
          } else if (singletonFragment (fragment2, _testNode[3])) {
            mask |= 16;
          } else {
            fail ();
          }
        }
      } else {
        fail ();
      }
    }
    assertEquals (31, mask);
  }
  
  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3
   */
  @Test
  public void testMinMax2 () {
    final MultipleNodeExecutor executor = createExecutor(2, 2, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testMinMax2");
    executor.printFragment(root);
    assertEquals (2, root.getInputs ().size ());
    int mask = 0;
    for (GraphFragment fragment : root.getInputs ()) {
      assertEquals (2, fragment.getNodes ().size ());
      if (fragment.getNodes ().contains (_testNode[0]) && fragment.getNodes ().contains (_testNode[1])) {
        mask |= 1;
        assertEquals (1, fragment.getInputs ().size ());
        assertTrue (singletonFragment (fragment.getInputs ().iterator ().next (), _testNode[2]));
      } else if (fragment.getNodes ().contains (_testNode[3]) && fragment.getNodes ().contains (_testNode[4])) {
        mask |= 2;
        assertEquals (1, fragment.getInputs ().size ());
        assertTrue (singletonFragment (fragment.getInputs ().iterator ().next (), _testNode[2]));
      } else {
        fail ();
      }
    }
    assertEquals (3, mask);
  }
  
  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3, input-merge on N(0+1)+N(4+3).
   */
  @Test
  public void testMinMax4 () {
    final MultipleNodeExecutor executor = createExecutor(3, 4, Integer.MAX_VALUE);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testMinMax4");
    executor.printFragment(root);
    assertEquals (1, root.getInputs ().size ());
    GraphFragment node = root.getInputs ().iterator ().next ();
    assertEquals (4, node.getNodes ().size ());
    assertTrue (node.getNodes ().contains (_testNode[0]));
    assertTrue (node.getNodes ().contains (_testNode[1]));
    assertTrue (node.getNodes ().contains (_testNode[4]));
    assertTrue (node.getNodes ().contains (_testNode[3]));
    assertEquals (1, node.getInputs ().size ());
    assertTrue (singletonFragment (node.getInputs ().iterator ().next (), _testNode[2]));
  }
  
  /**
   * Single-dep merge N4+N3, single tail on N2 (one of N0, N1 or N(4+3)).
   */
  @Test
  public void testThread1 () {
    final MultipleNodeExecutor executor = createExecutor(1, 4, 1);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testThread1");
    executor.printFragment(root);
    assertEquals (3, root.getInputs ().size ());
    int mask = 0;
    for (GraphFragment fragment : root.getInputs ()) {
      assertEquals (1, fragment.getInputs ().size ());
      GraphFragment input = fragment.getInputs ().iterator ().next ();
      assertTrue (singletonFragment (input, _testNode[2]));
      assertEquals (1, input.getTail ().size ());
      if (fragment.getNodes ().contains (_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes ().contains (_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes ().contains (_testNode[3]) && fragment.getNodes ().contains (_testNode[4])) {
        mask |= 4;
      } else {
        fail ();
      }
    }
    assertEquals (7, mask);
  }
  
  /**
   * Single-dep merge N4+N3, two tails on N2.
   */
  @Test
  public void testThread2 () {
    final MultipleNodeExecutor executor = createExecutor(1, Integer.MAX_VALUE, 2);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testThread2");
    executor.printFragment(root);
    assertEquals (3, root.getInputs ().size ());
    int mask = 0;
    for (GraphFragment fragment : root.getInputs ()) {
      assertEquals (1, fragment.getInputs ().size ());
      GraphFragment input = fragment.getInputs ().iterator ().next ();
      assertTrue (singletonFragment (input, _testNode[2]));
      assertEquals (2, input.getTail ().size ());
      if (fragment.getNodes ().contains (_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes ().contains (_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes ().contains (_testNode[3]) && fragment.getNodes ().contains (_testNode[4])) {
        mask |= 4;
      } else {
        fail ();
      }
    }
    assertEquals (7, mask);
  }
  
  /**
   * Single-dep merge N4+N3, three tails on N2.
   */
  @Test
  public void testThread3 () {
    final MultipleNodeExecutor executor = createExecutor(1, Integer.MAX_VALUE, 3);
    final RootGraphFragment root = executor.executeImpl(_testGraph, DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE);
    System.out.println ("testThread3");
    executor.printFragment(root);
    assertEquals (3, root.getInputs ().size ());
    int mask = 0;
    for (GraphFragment fragment : root.getInputs ()) {
      assertEquals (1, fragment.getInputs ().size ());
      GraphFragment input = fragment.getInputs ().iterator ().next ();
      assertTrue (singletonFragment (input, _testNode[2]));
      assertEquals (3, input.getTail ().size ());
      if (fragment.getNodes ().contains (_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes ().contains (_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes ().contains (_testNode[3]) && fragment.getNodes ().contains (_testNode[4])) {
        mask |= 4;
      } else {
        fail ();
      }
    }
    assertEquals (7, mask);
  }
  
}
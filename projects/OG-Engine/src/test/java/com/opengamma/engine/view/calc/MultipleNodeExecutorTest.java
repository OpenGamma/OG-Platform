/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.time.Instant;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ExecutionLogModeSource;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.async.Cancelable;

/**
 * Tests the graph partitioning logic in MultipleNodeExecutor.
 */
@Test
public class MultipleNodeExecutorTest {

  private static final boolean PRINT_GRAPHS = false;

  /**
   * Test graph:
   * 
   *      N0  N1  N4
   *        \ | /  |
   *          N2  N3
   *
   * If not partitioned:
   *    v20, v21, v24, v34 to go into private cache
   *    v0x, v1x, v4x to go into shared cache
   *    vx2, vx3 to go into shared cache
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

  private MultipleNodeExecutor createExecutor(final int minimum, final int maximum, final int concurrency) {
    return new MultipleNodeExecutor(null, minimum, maximum, 0, Integer.MAX_VALUE, concurrency, new FunctionCosts(), new ExecutionPlanCache(null)) {

      @Override
      protected long getFunctionInitId() {
        return 0;
      }

      @Override
      protected VersionCorrection getResolverVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
        return new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), graph.getCalculationConfigurationName(), Instant.now(), JobIdSource.getId());
      }

      @Override
      protected Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
        // No action - we're just testing graph construction
        return new Cancelable() {
          @Override
          public boolean cancel(final boolean mayInterrupt) {
            return false;
          }
        };
      }

    };
  }

  @SuppressWarnings("unchecked")
  private RootGraphFragmentFuture execute(final MultipleNodeExecutor executor, final DependencyGraph graph) {
    final ExecutionLogModeSource logModeSource = mock(ExecutionLogModeSource.class);
    when(logModeSource.getLogMode(any(Set.class))).thenReturn(ExecutionLogMode.INDICATORS);
    final Future<DependencyGraph> future = executor.executeImpl(graph, new LinkedBlockingQueue<ExecutionResult>(), DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE, logModeSource);
    return (RootGraphFragmentFuture) future;
  }

  /**
   * Graph untouched - single job.
   */
  public void testMin5() {
    final MultipleNodeExecutor executor = createExecutor(5, Integer.MAX_VALUE, Integer.MAX_VALUE);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMin5");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(5, root.getFragment().getNodes().size());
    final CacheSelectHint hint = root.getFragment().getCacheSelectHint();
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
  }

  private boolean singletonFragment(final GraphFragment<?> fragment, final DependencyNode node) {
    assertEquals(1, fragment.getNodes().size());
    return fragment.getNodes().iterator().next() == node;
  }

  /**
   * No changes to graph
   */
  public void testMax1() {
    final MultipleNodeExecutor executor = createExecutor(1, 1, 0);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMax1");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(3, root.getFragment().getInputFragments().size());
    int mask = 0;
    for (final GraphFragment<?> fragment : root.getFragment().getInputFragments()) {
      if (singletonFragment(fragment, _testNode[0])) {
        mask |= 1;
        assertEquals(1, fragment.getInputFragments().size());
        final GraphFragment<?> node2 = fragment.getInputFragments().iterator().next();
        assertTrue(singletonFragment(node2, _testNode[2]));
        final CacheSelectHint hint = fragment.createCalculationJob(root.getContext()).getCacheSelectHint();
        if (PRINT_GRAPHS) {
          System.out.println(hint);
        }
        assertFalse(hint.isPrivateValue(_testValue20));
        assertFalse(hint.isPrivateValue(_testValue0x));
      } else if (singletonFragment(fragment, _testNode[1])) {
        mask |= 2;
        assertEquals(1, fragment.getInputFragments().size());
        final GraphFragment<?> node2 = fragment.getInputFragments().iterator().next();
        assertTrue(singletonFragment(node2, _testNode[2]));
        final CacheSelectHint hint = fragment.createCalculationJob(root.getContext()).getCacheSelectHint();
        if (PRINT_GRAPHS) {
          System.out.println(hint);
        }
        assertFalse(hint.isPrivateValue(_testValue21));
        assertFalse(hint.isPrivateValue(_testValue1x));
      } else if (singletonFragment(fragment, _testNode[4])) {
        mask |= 4;
        assertEquals(2, fragment.getInputFragments().size());
        for (final GraphFragment<?> fragment2 : fragment.getInputFragments()) {
          if (singletonFragment(fragment2, _testNode[2])) {
            mask |= 8;
          } else if (singletonFragment(fragment2, _testNode[3])) {
            mask |= 16;
          } else {
            Assert.fail();
          }
        }
        final CacheSelectHint hint = fragment.createCalculationJob(root.getContext()).getCacheSelectHint();
        if (PRINT_GRAPHS) {
          System.out.println(hint);
        }
        assertFalse(hint.isPrivateValue(_testValue4x));
        assertFalse(hint.isPrivateValue(_testValue24));
        assertFalse(hint.isPrivateValue(_testValue34));
      } else {
        Assert.fail();
      }
    }
    assertEquals(31, mask);
  }

  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3
   */
  public void testMinMax2() {
    final MultipleNodeExecutor executor = createExecutor(2, 2, 0);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMinMax2");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(2, root.getFragment().getInputFragments().size());
    int mask = 0;
    for (final GraphFragment<?> fragment : root.getFragment().getInputFragments()) {
      assertEquals(2, fragment.getNodes().size());
      if (fragment.getNodes().contains(_testNode[0]) && fragment.getNodes().contains(_testNode[1])) {
        mask |= 1;
        assertEquals(1, fragment.getInputFragments().size());
        final GraphFragment<?> node2 = fragment.getInputFragments().iterator().next();
        assertTrue(singletonFragment(node2, _testNode[2]));
        final CacheSelectHint hint = fragment.createCalculationJob(root.getContext()).getCacheSelectHint();
        if (PRINT_GRAPHS) {
          System.out.println(hint);
        }
        assertFalse(hint.isPrivateValue(_testValue20));
        assertFalse(hint.isPrivateValue(_testValue21));
        assertFalse(hint.isPrivateValue(_testValue0x));
        assertFalse(hint.isPrivateValue(_testValue1x));
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 2;
        assertEquals(1, fragment.getInputFragments().size());
        final GraphFragment<?> node2 = fragment.getInputFragments().iterator().next();
        assertTrue(singletonFragment(node2, _testNode[2]));
        final CacheSelectHint hint = fragment.createCalculationJob(root.getContext()).getCacheSelectHint();
        if (PRINT_GRAPHS) {
          System.out.println(hint);
        }
        assertFalse(hint.isPrivateValue(_testValue24));
        assertFalse(hint.isPrivateValue(_testValue4x));
        assertFalse(hint.isPrivateValue(_testValuex3));
        assertTrue(hint.isPrivateValue(_testValue34));
      } else {
        Assert.fail();
      }
    }
    assertEquals(3, mask);
  }

  /**
   * Input-merge on N0+N1, single-dep merge on N4+N3, input-merge on N(0+1)+N(4+3).
   */
  public void testMinMax4() {
    final MultipleNodeExecutor executor = createExecutor(3, 4, 0);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testMinMax4");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(1, root.getFragment().getInputFragments().size());
    final GraphFragment<?> node = root.getFragment().getInputFragments().iterator().next();
    assertEquals(4, node.getNodes().size());
    assertTrue(node.getNodes().contains(_testNode[0]));
    assertTrue(node.getNodes().contains(_testNode[1]));
    assertTrue(node.getNodes().contains(_testNode[4]));
    assertTrue(node.getNodes().contains(_testNode[3]));
    assertEquals(1, node.getInputFragments().size());
    final GraphFragment<?> node2 = node.getInputFragments().iterator().next();
    assertTrue(singletonFragment(node2, _testNode[2]));
    CacheSelectHint hint = node2.getCacheSelectHint();
    if (PRINT_GRAPHS) {
      System.out.println(hint);
    }
    assertFalse(hint.isPrivateValue(_testValuex2));
    assertFalse(hint.isPrivateValue(_testValue20));
    assertFalse(hint.isPrivateValue(_testValue21));
    assertFalse(hint.isPrivateValue(_testValue24));
    hint = node.createCalculationJob(root.getContext()).getCacheSelectHint();
    if (PRINT_GRAPHS) {
      System.out.println(hint);
    }
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
    final MultipleNodeExecutor executor = createExecutor(1, 4, 1);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testThread1");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(3, root.getFragment().getInputFragments().size());
    int mask = 0;
    for (final GraphFragment<?> fragment : root.getFragment().getInputFragments()) {
      assertEquals(1, fragment.getInputFragments().size());
      final GraphFragment<?> input = fragment.getInputFragments().iterator().next();
      assertTrue(singletonFragment(input, _testNode[2]));
      assertEquals(1, input.getTail().size());
      if (fragment.getNodes().contains(_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes().contains(_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 4;
      } else {
        Assert.fail();
      }
    }
    assertEquals(7, mask);
  }

  /**
   * Single-dep merge N4+N3, two tails on N2.
   */
  public void testThread2() {
    final MultipleNodeExecutor executor = createExecutor(1, Integer.MAX_VALUE, 2);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testThread2");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(3, root.getFragment().getInputFragments().size());
    int mask = 0;
    for (final GraphFragment<?> fragment : root.getFragment().getInputFragments()) {
      assertEquals(1, fragment.getInputFragments().size());
      final GraphFragment<?> input = fragment.getInputFragments().iterator().next();
      assertTrue(singletonFragment(input, _testNode[2]));
      assertEquals(2, input.getTail().size());
      if (fragment.getNodes().contains(_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes().contains(_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 4;
      } else {
        Assert.fail();
      }
    }
    assertEquals(7, mask);
  }

  /**
   * Single-dep merge N4+N3, three tails on N2.
   */
  public void testThread3() {
    final MultipleNodeExecutor executor = createExecutor(1, Integer.MAX_VALUE, 3);
    final RootGraphFragmentFuture root = execute(executor, _testGraph);
    if (PRINT_GRAPHS) {
      System.out.println("testThread3");
      MultipleNodeExecutor.printFragment(root.getFragment());
    }
    assertEquals(3, root.getFragment().getInputFragments().size());
    int mask = 0;
    for (final GraphFragment<?> fragment : root.getFragment().getInputFragments()) {
      assertEquals(1, fragment.getInputFragments().size());
      final GraphFragment<?> input = fragment.getInputFragments().iterator().next();
      assertTrue(singletonFragment(input, _testNode[2]));
      assertEquals(3, input.getTail().size());
      if (fragment.getNodes().contains(_testNode[0])) {
        mask |= 1;
      } else if (fragment.getNodes().contains(_testNode[1])) {
        mask |= 2;
      } else if (fragment.getNodes().contains(_testNode[3]) && fragment.getNodes().contains(_testNode[4])) {
        mask |= 4;
      } else {
        Assert.fail();
      }
    }
    assertEquals(7, mask);
  }

  private void extractColours(final GraphFragment<?> fragment, final Map<Integer, Collection<DependencyNode>> colours) {
    assertTrue(fragment instanceof MutableGraphFragment);
    final int colour = ((MutableGraphFragment) fragment).getExecutionId();
    if (colour != 0) {
      Collection<DependencyNode> nodes = colours.get(colour);
      if (nodes == null) {
        nodes = new HashSet<DependencyNode>();
        colours.put(colour, nodes);
      }
      nodes.addAll(fragment.getNodes());
    }
    for (final GraphFragment<?> input : fragment.getInputFragments()) {
      extractColours(input, colours);
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
    if (PRINT_GRAPHS) {
      System.out.println("testTailGraphColouring");
      System.out.println("concurrency 1");
    }
    MultipleNodeExecutor executor = createExecutor(1, 1, 1);
    RootGraphFragmentFuture root = execute(executor, _testGraph);
    final Map<Integer, Collection<DependencyNode>> colours = new HashMap<Integer, Collection<DependencyNode>>();
    extractColours(root.getFragment(), colours);
    if (PRINT_GRAPHS) {
      System.out.println(colours);
    }
    assertEquals(7, colours.size());
    if (PRINT_GRAPHS) {
      System.out.println("concurrency 2");
    }
    executor = createExecutor(1, 1, 2);
    root = execute(executor, _testGraph);
    colours.clear();
    extractColours(root.getFragment(), colours);
    if (PRINT_GRAPHS) {
      System.out.println(colours);
    }
    assertEquals(4, colours.size());
    if (PRINT_GRAPHS) {
      System.out.println("concurrency 3");
    }
    executor = createExecutor(1, 1, 3);
    root = execute(executor, _testGraph);
    colours.clear();
    extractColours(root.getFragment(), colours);
    if (PRINT_GRAPHS) {
      System.out.println(colours);
    }
    assertEquals(3, colours.size());
    int mask = 0;
    for (final Map.Entry<Integer, Collection<DependencyNode>> colour : colours.entrySet()) {
      if (colour.getValue().contains(n6)) {
        assertEquals(1, colour.getValue().size());
        mask |= 1;
      } else if (colour.getValue().contains(_testNode[0])) {
        assertEquals(1, colour.getValue().size());
        mask |= 2;
      } else if (colour.getValue().contains(n5)) {
        assertTrue(colour.getValue().containsAll(Arrays.asList(_testNode[1], _testNode[2], _testNode[3], _testNode[4])));
        mask |= 4;
      } else {
        Assert.fail();
      }
    }
    assertEquals(7, mask);
  }

}

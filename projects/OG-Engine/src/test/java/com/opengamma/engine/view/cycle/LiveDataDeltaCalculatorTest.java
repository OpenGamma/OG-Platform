/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.depgraph.impl.DependencyGraphExplorerImpl;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataDeltaCalculatorTest {

  FunctionCompilationContext _context = new FunctionCompilationContext();

  DependencyGraph _graph;
  DependencyNode[] _node = new DependencyNode[5];
  ValueSpecification[] _value = new ValueSpecification[_node.length];

  ViewComputationCache _cache;
  ViewComputationCache _previousCache;

  @BeforeMethod
  public void setUp() {
    createTestGraph();
    final InMemoryViewComputationCacheSource source = new InMemoryViewComputationCacheSource(FudgeContext.GLOBAL_DEFAULT);
    _cache = source.getCache(UniqueId.of("Test", "ViewCycle", "1"), "Default");
    _previousCache = source.getCache(UniqueId.of("Test", "ViewCycle", "0"), "Default");
  }

  private LiveDataDeltaCalculator deltaCalculator() {
    return deltaCalculator(Collections.<ValueSpecification>emptySet());
  }

  private LiveDataDeltaCalculator deltaCalculator(final Set<ValueSpecification> dirtySpecifications) {
    return new LiveDataDeltaCalculator(_graph, _cache, _previousCache, dirtySpecifications);
  }

  private ComputationTargetSpecification getTarget(final String name) {
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("testdomain", name));
  }

  private void put(final ViewComputationCache cache, final int id, final Object value) {
    cache.putSharedValue(new ComputedValue(_value[id], value));
  }

  /**
   * Creates the test graph (data flows downwards - 0 & 1 are market data nodes)
   * 
   * <pre>
   *         0   1
   *          \ / \
   *           2   3
   *            \ /
   *             4
   * </pre>
   */
  private void createTestGraph() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("test");
    NodeBuilder n0 = gb.addNode(MarketDataSourcingFunction.INSTANCE, getTarget("Node0"));
    NodeBuilder n1 = gb.addNode(MarketDataSourcingFunction.INSTANCE, getTarget("Node1"));
    NodeBuilder n2 = gb.addNode(DependencyNodeFunctionImpl.of("Mock", EmptyFunctionParameters.INSTANCE), getTarget("Node2"));
    NodeBuilder n3 = gb.addNode(DependencyNodeFunctionImpl.of("Mock", EmptyFunctionParameters.INSTANCE), getTarget("Node3"));
    NodeBuilder n4 = gb.addNode(DependencyNodeFunctionImpl.of("Mock", EmptyFunctionParameters.INSTANCE), getTarget("Node4"));
    _value[0] = n0.addOutput("MarketValue");
    n2.addInput(_value[0]);
    _value[1] = n1.addOutput("MarketValue");
    n2.addInput(_value[1]);
    n3.addInput(_value[1]);
    _value[2] = n2.addOutput("IntermediateValue");
    n4.addInput(_value[2]);
    _value[3] = n3.addOutput("IntermediateValue");
    n4.addInput(_value[3]);
    _value[4] = n4.addTerminalOutput("TerminalValue");
    _graph = gb.buildGraph();
    final DependencyGraphExplorer dge = new DependencyGraphExplorerImpl(_graph);
    for (int i = 0; i < _value.length; i++) {
      _node[i] = dge.getNodeProducing(_value[i]);
    }
  }

  public void noChangeA() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator();
    put(_cache, 0, 6.0);
    put(_previousCache, 0, 6.0);
    deltaCalculator.computeDelta();
    assertEquals(ImmutableSet.copyOf(DependencyGraphImpl.getDependencyNodes(_graph)), deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), deltaCalculator.getChangedNodes());
  }

  public void noChangeB() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator();
    put(_cache, 1, 6.0);
    put(_previousCache, 1, 6.0);
    deltaCalculator.computeDelta();
    assertEquals(ImmutableSet.copyOf(DependencyGraphImpl.getDependencyNodes(_graph)), deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), deltaCalculator.getChangedNodes());
  }

  public void noChangeC() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator();
    put(_cache, 2, 6.0);
    put(_previousCache, 2, 6.0);
    deltaCalculator.computeDelta();
    assertEquals(ImmutableSet.copyOf(DependencyGraphImpl.getDependencyNodes(_graph)), deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), deltaCalculator.getChangedNodes());
  }

  public void changeA() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator();
    put(_cache, 0, 6.0);
    put(_previousCache, 0, 7.0);
    deltaCalculator.computeDelta();
    assertEquals(Sets.newHashSet(_node[1], _node[3]), deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node[0], _node[2], _node[4]), deltaCalculator.getChangedNodes());
  }

  public void changeB() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator();
    put(_cache, 1, 6.0);
    put(_previousCache, 1, 7.0);
    deltaCalculator.computeDelta();
    assertEquals(Sets.newHashSet(_node[0]), deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node[1], _node[2], _node[3], _node[4]), deltaCalculator.getChangedNodes());
  }

  public void changeC() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator();
    put(_cache, 2, 6.0);
    put(_previousCache, 2, 7.0);
    deltaCalculator.computeDelta();
    assertEquals(ImmutableSet.copyOf(DependencyGraphImpl.getDependencyNodes(_graph)), deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), deltaCalculator.getChangedNodes());
  }

  public void parameterChangeA() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator(Collections.singleton(_value[2]));
    deltaCalculator.computeDelta();
    assertEquals(Sets.newHashSet(_node[0], _node[1], _node[3]), deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node[2], _node[4]), deltaCalculator.getChangedNodes());
  }

  public void parameterChangeB() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator(Collections.singleton(_value[3]));
    deltaCalculator.computeDelta();
    assertEquals(Sets.newHashSet(_node[0], _node[1], _node[2]), deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node[3], _node[4]), deltaCalculator.getChangedNodes());
  }

  public void parameterChangeC() {
    final LiveDataDeltaCalculator deltaCalculator = deltaCalculator(Collections.singleton(_value[4]));
    deltaCalculator.computeDelta();
    assertEquals(Sets.newHashSet(_node[0], _node[1], _node[2], _node[3]), deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node[4]), deltaCalculator.getChangedNodes());
  }

}

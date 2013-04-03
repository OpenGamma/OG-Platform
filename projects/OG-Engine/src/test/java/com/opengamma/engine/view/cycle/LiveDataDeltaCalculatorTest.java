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

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cycle.LiveDataDeltaCalculator;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataDeltaCalculatorTest {

  FunctionCompilationContext _context = new FunctionCompilationContext();

  DependencyGraph _graph = getTestGraph();
  DependencyNode _node0;
  DependencyNode _node1;
  DependencyNode _node2;
  DependencyNode _node3;
  DependencyNode _node4;

  ViewComputationCache _cache;
  ViewComputationCache _previousCache;
  LiveDataDeltaCalculator _deltaCalculator;

  @BeforeMethod
  public void setUp() {
    final InMemoryViewComputationCacheSource source = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    _cache = source.getCache(UniqueId.of("Test", "ViewCycle", "1"), "Default");
    _previousCache = source.getCache(UniqueId.of("Test", "ViewCycle", "0"), "Default");
    _deltaCalculator = new LiveDataDeltaCalculator(
        _graph,
        _cache,
        _previousCache);

  }

  private ComputationTarget getTarget(final String name) {
    return new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("testdomain", name));
  }

  private DependencyNode createNode(final String name, final Set<DependencyNode> inputNodes) {
    final ComputationTarget target = getTarget(name);
    final DependencyNode node = new DependencyNode(target);
    final ValueSpecification output;
    if (inputNodes.isEmpty()) {
      final MarketDataSourcingFunction msdf = MarketDataSourcingFunction.INSTANCE;
      output = new ValueSpecification(name, target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, msdf.getUniqueId()).get());
      node.setFunction(new ParameterizedFunction(msdf, msdf.getDefaultParameters()));
    } else {
      final MockFunction mock = new MockFunction(target);
      output = new ValueSpecification(name, target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, mock.getUniqueId()).get());
      node.setFunction(mock);
      mock.addResult(new ComputedValue(output, null));
    }
    node.addOutputValue(output);
    node.addInputNodes(inputNodes);
    return node;
  }

  private void put(final ViewComputationCache cache, final DependencyNode node, final Object value) {
    final ValueSpecification spec = node.getOutputValues().iterator().next();
    cache.putSharedValue(new ComputedValue(spec, value));
  }

  /**
   * @return The test graph
   *
   *         <pre>
   *         0   1
   *          \ / \
   *           2   3
   *            \ /
   *             4
   * </pre>
   */
  private DependencyGraph getTestGraph() {
    final DependencyGraph graph = new DependencyGraph("test");
    _node0 = createNode("Node0", Collections.<DependencyNode>emptySet());
    _node1 = createNode("Node1", Collections.<DependencyNode>emptySet());
    _node2 = createNode("Node2", Sets.newHashSet(_node0, _node1));
    _node3 = createNode("Node3", Sets.newHashSet(_node1));
    _node4 = createNode("Node4", Sets.newHashSet(_node2, _node3));
    graph.addDependencyNode(_node0);
    graph.addDependencyNode(_node1);
    graph.addDependencyNode(_node2);
    graph.addDependencyNode(_node3);
    graph.addDependencyNode(_node4);
    return graph;
  }

  public void noChangeA() {
    put(_cache, _node0, 6.0);
    put(_previousCache, _node0, 6.0);

    _deltaCalculator.computeDelta();

    assertEquals(_graph.getDependencyNodes(), _deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), _deltaCalculator.getChangedNodes());
  }

  public void noChangeB() {
    put(_cache, _node1, 6.0);
    put(_previousCache, _node1, 6.0);

    _deltaCalculator.computeDelta();

    assertEquals(_graph.getDependencyNodes(), _deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), _deltaCalculator.getChangedNodes());
  }

  public void noChangeC() {
    put(_cache, _node2, 6.0);
    put(_previousCache, _node2, 6.0);

    _deltaCalculator.computeDelta();

    assertEquals(_graph.getDependencyNodes(), _deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), _deltaCalculator.getChangedNodes());
  }

  public void changeA() {
    put(_cache, _node0, 6.0);
    put(_previousCache, _node0, 7.0);

    _deltaCalculator.computeDelta();

    assertEquals(Sets.newHashSet(_node1, _node3), _deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node0, _node2, _node4), _deltaCalculator.getChangedNodes());
  }

  public void changeB() {
    put(_cache, _node1, 6.0);
    put(_previousCache, _node1, 7.0);

    _deltaCalculator.computeDelta();

    assertEquals(Sets.newHashSet(_node0), _deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node1, _node2, _node3, _node4), _deltaCalculator.getChangedNodes());
  }

  public void changeC() {
    put(_cache, _node2, 6.0);
    put(_previousCache, _node2, 7.0);

    _deltaCalculator.computeDelta();

    assertEquals(_graph.getDependencyNodes(), _deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), _deltaCalculator.getChangedNodes());
  }

}

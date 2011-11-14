/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.LiveDataDeltaCalculator;
import com.opengamma.id.UniqueId;

/**
 * 
 */
@Test
public class LiveDataDeltaCalculatorTest {
  
  FunctionCompilationContext _context = new FunctionCompilationContext();
  
  DependencyGraph _graph = getTestGraph();
  DependencyNode _node0;
  DependencyNode _node1;
  DependencyNode _node2;
  DependencyNode _node3;
  
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
  
  private ComputationTarget getTarget(String name) {
    return new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("testdomain", name));
  }
  
  private ValueRequirement getValueRequirement(String name) {
    ComputationTargetSpecification spec = getTarget(name).toSpecification();
    ValueRequirement requirement = new ValueRequirement("LiveData", spec);
    return requirement;
  }
  
  private DependencyNode createNode(String name, Set<DependencyNode> inputNodes) {
    ComputationTarget target = getTarget(name); 
    
    MarketDataSourcingFunction function = new MarketDataSourcingFunction(getValueRequirement(name));
    
    DependencyNode node = new DependencyNode(target);
    node.setFunction(function);
    node.addInputNodes(inputNodes);
    return node;
  }
  
  private void put(ViewComputationCache cache, DependencyNode node, Object value) {
    ValueSpecification spec = ((MarketDataSourcingFunction) node.getFunction().getFunction()).getMarketDataRequirement().getSecond();
    cache.putSharedValue(new ComputedValue(spec, value));
  }
  
  /**
   * @return Diamond-shaped graph
   * 
   *              0
   *             / \
   *           1     2    
   *            \   /
   *              3
   *              
   */
  private DependencyGraph getTestGraph() {
    DependencyGraph graph = new DependencyGraph("test");
    
    _node3 = createNode("Node3", Collections.<DependencyNode>emptySet());
    _node1 = createNode("Node1", Sets.newHashSet(_node3));
    _node2 = createNode("Node2", Sets.newHashSet(_node3));
    _node0 = createNode("Node0", Sets.newHashSet(_node1, _node2));

    graph.addDependencyNode(_node0);
    graph.addDependencyNode(_node1);
    graph.addDependencyNode(_node2);
    graph.addDependencyNode(_node3);
    
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
    put(_cache, _node3, 6.0);
    put(_previousCache, _node3, 6.0);
        
    _deltaCalculator.computeDelta();
    
    assertEquals(_graph.getDependencyNodes(), _deltaCalculator.getUnchangedNodes());
    assertEquals(Collections.emptySet(), _deltaCalculator.getChangedNodes());
  }
  
  public void changeA() {
    put(_cache, _node0, 6.0);
    put(_previousCache, _node0, 7.0);
        
    _deltaCalculator.computeDelta();
    
    assertEquals(Sets.newHashSet(_node1, _node2, _node3), _deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node0), _deltaCalculator.getChangedNodes());
  }
  
  public void changeB() {
    put(_cache, _node1, 6.0);
    put(_previousCache, _node1, 7.0);
        
    _deltaCalculator.computeDelta();
    
    assertEquals(Sets.newHashSet(_node2, _node3), _deltaCalculator.getUnchangedNodes());
    assertEquals(Sets.newHashSet(_node0, _node1), _deltaCalculator.getChangedNodes());
  }
  
  public void changeC() {
    put(_cache, _node3, 6.0);
    put(_previousCache, _node3, 7.0);
        
    _deltaCalculator.computeDelta();
    
    assertEquals(Collections.emptySet(), _deltaCalculator.getUnchangedNodes());
    assertEquals(_graph.getDependencyNodes(), _deltaCalculator.getChangedNodes());
  }

}

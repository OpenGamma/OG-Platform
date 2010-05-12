/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class DependencyGraphModelTest {

  @Test
  public void singleOutputSingleFunctionNode() {
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    UniqueIdentifier targetId = UniqueIdentifier.of("Scheme", "Value");
    ComputationTarget target = new ComputationTarget(targetId);
    ValueRequirement req1 = new ValueRequirement("Req-1", targetId);
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    
    ValueRequirement req2 = new ValueRequirement("Req-2", targetId);
    ValueSpecification spec2 = new ValueSpecification(req2);
    ComputedValue value2 = new ComputedValue(spec2, 15.5);
    
    MockFunction fn1 = new MockFunction(target,
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value1, value2));
    functionRepo.addFunction(fn1, fn1);

    DependencyGraphModel model = new DependencyGraphModel();
    model.setFunctionRepository(functionRepo);
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(new MapComputationTargetResolver());

    model.addTarget(target, Sets.newHashSet(req1));
    
    Collection<DependencyGraph> graphs = model.getDependencyGraphs(ComputationTargetType.PRIMITIVE);
    assertNotNull(graphs);
    assertEquals(1, graphs.size());
    DependencyGraph graph = graphs.iterator().next();
    assertNotNull(graph);
    assertEquals(target, graph.getComputationTarget());
    assertTrue(graph.getOutputValues().contains(spec1));
    assertTrue(graph.getOutputValues().contains(spec2));
    
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(fn1, node.getFunctionDefinition());
    assertTrue(node.getOutputValues().contains(spec1));
    assertTrue(node.getOutputValues().contains(spec2));
    assertTrue(node.getInputNodes().isEmpty());
    
    model.removeUnnecessaryOutputs();
    graphs = model.getDependencyGraphs(ComputationTargetType.PRIMITIVE);
    graph = graphs.iterator().next();
    assertTrue(graph.getOutputValues().contains(spec1));
    assertFalse(graph.getOutputValues().contains(spec2));
    nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    node = nodes.iterator().next();
    assertEquals(fn1, node.getFunctionDefinition());
    assertTrue(node.getOutputValues().contains(spec1));
    assertFalse(node.getOutputValues().contains(spec2));
    assertTrue(node.getInputNodes().isEmpty());
  }
  
  /**
   * When you have multiple requirements eminating from the same function,
   * should only have a single node using that function. 
   */
  @Test
  public void multipleOutputsSingleFunctionNode() {
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    UniqueIdentifier targetId = UniqueIdentifier.of("Scheme", "Value");
    ComputationTarget target = new ComputationTarget(targetId);
    ValueRequirement req1 = new ValueRequirement("Req-1", targetId);
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    
    ValueRequirement req2 = new ValueRequirement("Req-2", targetId);
    ValueSpecification spec2 = new ValueSpecification(req2);
    ComputedValue value2 = new ComputedValue(spec2, 15.5);
    
    MockFunction fn1 = new MockFunction(target,
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value1, value2));
    functionRepo.addFunction(fn1, fn1);

    DependencyGraphModel model = new DependencyGraphModel();
    model.setFunctionRepository(functionRepo);
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(new MapComputationTargetResolver());

    model.addTarget(target, Sets.newHashSet(req1));
    model.addTarget(target, Sets.newHashSet(req2));
    
    Collection<DependencyGraph> graphs = model.getDependencyGraphs(ComputationTargetType.PRIMITIVE);
    assertNotNull(graphs);
    assertEquals(1, graphs.size());
    DependencyGraph graph = graphs.iterator().next();
    assertNotNull(graph);
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(fn1, node.getFunctionDefinition());
    assertTrue(node.getOutputValues().contains(spec1));
    assertTrue(node.getOutputValues().contains(spec2));
    assertTrue(node.getInputNodes().isEmpty());
  }
  
  @Test(expected=UnsatisfiableDependencyGraphException.class)
  public void unsatisfiableDependency() {
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    UniqueIdentifier targetId = UniqueIdentifier.of("Scheme", "Value");
    ComputationTarget target = new ComputationTarget(targetId);
    ValueRequirement req1 = new ValueRequirement("Req-1", targetId);
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    
    ValueRequirement req2 = new ValueRequirement("Req-2", targetId);
    ValueSpecification spec2 = new ValueSpecification(req2);
    ComputedValue value2 = new ComputedValue(spec2, 15.5);
    
    ValueRequirement req3 = new ValueRequirement("Req-3", targetId);
    
    MockFunction fn1 = new MockFunction(target,
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value1, value2));
    functionRepo.addFunction(fn1, fn1);

    DependencyGraphModel model = new DependencyGraphModel();
    model.setFunctionRepository(functionRepo);
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(new MapComputationTargetResolver());

    model.addTarget(target, Sets.newHashSet(req1));
    model.addTarget(target, Collections.singleton(req3));
  }
}

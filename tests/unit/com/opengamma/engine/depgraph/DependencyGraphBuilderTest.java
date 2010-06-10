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
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class DependencyGraphBuilderTest {

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

    DependencyGraphBuilder model = new DependencyGraphBuilder();
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(new MapComputationTargetResolver());

    model.addTarget(target, Sets.newHashSet(req1));
    
    DependencyGraph graph = model.getDependencyGraph();
    assertNotNull(graph);
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
    assertEquals(target, node.getComputationTarget());

    graph.removeUnnecessaryValues();

    nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
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

    DependencyGraphBuilder model = new DependencyGraphBuilder();
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(new MapComputationTargetResolver());

    model.addTarget(target, Sets.newHashSet(req1));
    model.addTarget(target, Sets.newHashSet(req2));
    
    DependencyGraph graph = model.getDependencyGraph();
    assertNotNull(graph);
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
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

    DependencyGraphBuilder model = new DependencyGraphBuilder();
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(new MapComputationTargetResolver());

    model.addTarget(target, Sets.newHashSet(req1));
    model.addTarget(target, Collections.singleton(req3));
  }
  
  @Test
  public void doubleLevelNoLiveData() {
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
        Collections.singleton(req2),
        Sets.newHashSet(value1));
    MockFunction fn2 = new MockFunction(target,
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value2));
    functionRepo.addFunction(fn1, fn1);
    functionRepo.addFunction(fn2, fn2);
    
    MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    targetResolver.addTarget(target);

    DependencyGraphBuilder model = new DependencyGraphBuilder();
    model.setLiveDataAvailabilityProvider(new FixedLiveDataAvailabilityProvider());
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(targetResolver);

    model.addTarget(target, req1);
    
    DependencyGraph graph = model.getDependencyGraph();
    assertNotNull(graph);
    
    graph.removeUnnecessaryValues();

    assertTrue(graph.getOutputValues().contains(spec1));
    assertTrue(graph.getOutputValues().contains(spec2));
    
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if(ObjectUtils.equals(node.getFunctionDefinition(), fn1)) {
        assertTrue(node.getOutputValues().contains(spec1));
        assertFalse(node.getOutputValues().contains(spec2));
        assertTrue(node.getInputRequirements().contains(req2));
        assertEquals(1, node.getInputNodes().size());
        assertEquals(target, node.getComputationTarget());
      } else if(ObjectUtils.equals(node.getFunctionDefinition(), fn2)) {
        assertFalse(node.getOutputValues().contains(spec1));
        assertTrue(node.getOutputValues().contains(spec2));
        assertTrue(node.getInputRequirements().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        fail("Unexpected function definition");
      }
    }
  }
  
  @Test
  public void doubleLevelLiveData() {
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    UniqueIdentifier targetId = UniqueIdentifier.of("Scheme", "Value");
    ComputationTarget target = new ComputationTarget(targetId);
    ValueRequirement req1 = new ValueRequirement("Req-1", targetId);
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    
    ValueRequirement req2 = new ValueRequirement("Req-2", targetId);
    
    MockFunction fn1 = new MockFunction(target,
        Collections.singleton(req2),
        Sets.newHashSet(value1));
    functionRepo.addFunction(fn1, fn1);
    
    MapComputationTargetResolver targetResolver = new MapComputationTargetResolver();
    targetResolver.addTarget(target);
    
    FixedLiveDataAvailabilityProvider ldap = new FixedLiveDataAvailabilityProvider();
    ldap.addRequirement(req2);

    DependencyGraphBuilder model = new DependencyGraphBuilder();
    model.setLiveDataAvailabilityProvider(ldap);
    model.setFunctionResolver(new DefaultFunctionResolver(functionRepo));
    model.setTargetResolver(targetResolver);

    model.addTarget(target, req1);

    DependencyGraph graph = model.getDependencyGraph();
    assertNotNull(graph);
    
    graph.removeUnnecessaryValues();

    assertTrue(graph.getOutputValues().contains(spec1));
    
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if(ObjectUtils.equals(node.getFunctionDefinition(), fn1)) {
        assertTrue(node.getOutputValues().contains(spec1));
        assertTrue(node.getInputRequirements().contains(req2));
        assertEquals(1, node.getInputNodes().size());
        assertEquals(target, node.getComputationTarget());
      } else if(node.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
        assertFalse(node.getOutputValues().contains(spec1));
        assertEquals(1, node.getOutputValues().size());
        ValueSpecification outputSpec = node.getOutputValues().iterator().next();
        assertEquals(req2, outputSpec.getRequirementSpecification());
        assertTrue(node.getInputRequirements().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        fail("Unexpected function definition");
      }
    }
  }
}

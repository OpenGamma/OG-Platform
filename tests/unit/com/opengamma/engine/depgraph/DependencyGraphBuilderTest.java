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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class DependencyGraphBuilderTest {

  @Test
  public void singleOutputSingleFunctionNode() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction function = helper.addFunctionProducing1and2();

    DependencyGraphBuilder builder = helper.getBuilder();
    builder.addTarget(helper.getTarget(), Sets.newHashSet(helper.getSpec1().getRequirementSpecification()));
    
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    assertTrue(graph.getOutputValues().contains(helper.getSpec1()));
    assertTrue(graph.getOutputValues().contains(helper.getSpec2()));
    
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(function, node.getFunctionDefinition());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertTrue(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
    assertEquals(helper.getTarget(), node.getComputationTarget());

    graph.removeUnnecessaryValues();

    nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    node = nodes.iterator().next();
    assertEquals(function, node.getFunctionDefinition());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertFalse(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
  }
  
  /**
   * When you have multiple requirements eminating from the same function,
   * should only have a single node using that function. 
   */
  @Test
  public void multipleOutputsSingleFunctionNode() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction function = helper.addFunctionProducing1and2();

    DependencyGraphBuilder builder = helper.getBuilder();
    builder.addTarget(helper.getTarget(), Sets.newHashSet(helper.getSpec1().getRequirementSpecification()));
    builder.addTarget(helper.getTarget(), Sets.newHashSet(helper.getSpec2().getRequirementSpecification()));
    
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(function, node.getFunctionDefinition());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertTrue(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
  }
  
  @Test(expected=UnsatisfiableDependencyGraphException.class)
  public void unsatisfiableDependency() {    
    DepGraphTestHelper helper = new DepGraphTestHelper();
    helper.addFunctionProducing1and2();
    ValueRequirement anotherReq = new ValueRequirement("Req-3", helper.getTarget());
    
    DependencyGraphBuilder builder = helper.getBuilder();
    builder.addTarget(helper.getTarget(), Sets.newHashSet(helper.getSpec1().getRequirementSpecification()));
    builder.addTarget(helper.getTarget(), Collections.singleton(anotherReq));
  }
  
  @Test
  public void doubleLevelNoLiveData() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction fn1 = helper.addFunctionRequiring2Producing1();
    MockFunction fn2 = helper.addFunctionProducing2();

    DependencyGraphBuilder builder = helper.getBuilder();
    builder.addTarget(helper.getTarget(), helper.getSpec1().getRequirementSpecification());
    
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    
    graph.removeUnnecessaryValues();

    assertTrue(graph.getOutputValues().contains(helper.getSpec1()));
    assertTrue(graph.getOutputValues().contains(helper.getSpec2()));
    
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if(ObjectUtils.equals(node.getFunctionDefinition(), fn1)) {
        assertTrue(node.getOutputValues().contains(helper.getSpec1()));
        assertFalse(node.getOutputValues().contains(helper.getSpec2()));
        assertTrue(node.getInputRequirements().contains(helper.getSpec2().getRequirementSpecification()));
        assertEquals(1, node.getInputNodes().size());
        assertEquals(helper.getTarget(), node.getComputationTarget());
      } else if(ObjectUtils.equals(node.getFunctionDefinition(), fn2)) {
        assertFalse(node.getOutputValues().contains(helper.getSpec1()));
        assertTrue(node.getOutputValues().contains(helper.getSpec2()));
        assertTrue(node.getInputRequirements().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        fail("Unexpected function definition");
      }
    }
  }
  
  @Test
  public void doubleLevelLiveData() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction fn1 = helper.addFunctionRequiring2Producing1();
    helper.make2AvailableFromLiveData();
    
    DependencyGraphBuilder builder = helper.getBuilder();
    builder.addTarget(helper.getTarget(), helper.getSpec1().getRequirementSpecification());

    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    
    graph.removeUnnecessaryValues();

    assertTrue(graph.getOutputValues().contains(helper.getSpec1()));
    
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if(ObjectUtils.equals(node.getFunctionDefinition(), fn1)) {
        assertTrue(node.getOutputValues().contains(helper.getSpec1()));
        assertTrue(node.getInputRequirements().contains(helper.getSpec2().getRequirementSpecification()));
        assertEquals(1, node.getInputNodes().size());
        assertEquals(helper.getTarget(), node.getComputationTarget());
      } else if(node.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
        assertFalse(node.getOutputValues().contains(helper.getSpec1()));
        assertEquals(1, node.getOutputValues().size());
        ValueSpecification outputSpec = node.getOutputValues().iterator().next();
        assertEquals(helper.getSpec2().getRequirementSpecification(), outputSpec.getRequirementSpecification());
        assertTrue(node.getInputRequirements().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        fail("Unexpected function definition");
      }
    }
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the dependency graph building with trivial constructions.
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphBasicTest extends AbstractDependencyGraphBuilderTest {

  public void singleOutputSingleFunctionNode() {
    DepGraphTestHelper helper = helper();
    MockFunction function = helper.addFunctionProducing1and2();
    DependencyGraphBuilder builder = helper.createBuilder(null);
    builder.addTarget(Sets.newHashSet(helper.getRequirement1()));
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    assertTrue(graph.getOutputSpecifications().contains(helper.getSpec1()));
    assertTrue(graph.getOutputSpecifications().contains(helper.getSpec2()));
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(function, node.getFunction().getFunction());
    assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertTrue(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
    assertEquals(helper.getTarget().toSpecification(), node.getComputationTarget());
    graph.removeUnnecessaryValues();
    nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    node = nodes.iterator().next();
    assertEquals(function, node.getFunction().getFunction());
    assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertFalse(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
  }

  /**
   * When you have multiple requirements produced by the same function, should only have a single node using that function.
   */
  public void multipleOutputsSingleFunctionNode() {
    DepGraphTestHelper helper = helper();
    MockFunction function = helper.addFunctionProducing1and2();
    DependencyGraphBuilder builder = helper.createBuilder(null);
    builder.addTarget(Sets.newHashSet(helper.getRequirement1()));
    builder.addTarget(Sets.newHashSet(helper.getRequirement2()));
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(function, node.getFunction().getFunction());
    assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertTrue(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
  }

  public void unsatisfiableDependency() {
    DepGraphTestHelper helper = helper();
    helper.addFunctionProducing1and2();
    ValueRequirement anotherReq = new ValueRequirement("Req-3", helper.getTarget().toSpecification());
    DependencyGraphBuilder builder = helper.createBuilder(null);
    expectCompletion(builder, builder.getContext().resolveRequirement(helper.getRequirement1(), null, null));
    expectFailure(builder, builder.getContext().resolveRequirement(anotherReq, null, null));
  }

  public void doubleLevelNoLiveData() {
    DepGraphTestHelper helper = helper();
    MockFunction fn1 = helper.addFunctionRequiring2Producing1();
    MockFunction fn2 = helper.addFunctionProducing2();
    DependencyGraphBuilder builder = helper.createBuilder(null);
    builder.addTarget(helper.getRequirement1());
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertTrue(graph.getOutputSpecifications().contains(helper.getSpec1()));
    assertTrue(graph.getOutputSpecifications().contains(helper.getSpec2()));
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if (ObjectUtils.equals(node.getFunction().getFunction(), fn1)) {
        assertTrue(node.getOutputValues().contains(helper.getSpec1()));
        assertFalse(node.getOutputValues().contains(helper.getSpec2()));
        assertTrue(node.getInputValues().contains(helper.getSpec2()));
        assertEquals(1, node.getInputNodes().size());
        assertEquals(helper.getTarget().toSpecification(), node.getComputationTarget());
      } else if (ObjectUtils.equals(node.getFunction().getFunction(), fn2)) {
        assertFalse(node.getOutputValues().contains(helper.getSpec1()));
        assertTrue(node.getOutputValues().contains(helper.getSpec2()));
        assertTrue(node.getInputValues().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        Assert.fail("Unexpected function definition");
      }
    }
  }

  public void doubleLevelLiveData() {
    DepGraphTestHelper helper = helper();
    MockFunction fn1 = helper.addFunctionRequiring2Producing1();
    helper.make2AvailableFromLiveData();
    DependencyGraphBuilder builder = helper.createBuilder(null);
    builder.addTarget(helper.getRequirement1());
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertTrue(graph.getOutputSpecifications().contains(helper.getSpec1()));
    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if (ObjectUtils.equals(node.getFunction().getFunction(), fn1)) {
        assertTrue(node.getOutputValues().contains(helper.getSpec1()));
        assertEquals(1, node.getInputNodes().size());
        ValueSpecification inputSpec = node.getInputValues().iterator().next();
        assertEquals(helper.getSpec2().getValueName(), inputSpec.getValueName());
        assertEquals(helper.getSpec2().getTargetSpecification(), inputSpec.getTargetSpecification());
        assertEquals(helper.getTarget().toSpecification(), node.getComputationTarget());
      } else if (node.getFunction().getFunction() instanceof MarketDataSourcingFunction) {
        assertFalse(node.getOutputValues().contains(helper.getSpec1()));
        assertEquals(1, node.getOutputValues().size());
        ValueSpecification outputSpec = node.getOutputValues().iterator().next();
        assertEquals(helper.getSpec2().getValueName(), outputSpec.getValueName());
        assertEquals(helper.getSpec2().getTargetSpecification(), outputSpec.getTargetSpecification());
        assertTrue(node.getInputValues().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        Assert.fail("Unexpected function definition");
      }
    }
  }


  public void incrementalBuild() {
    final DepGraphTestHelper helper = helper();
    helper.addFunctionRequiring2Producing1();
    helper.addFunctionProducing2();
    DependencyGraphBuilder builder = helper.createBuilder(null);
    builder.setDependencyGraph(new DependencyGraph("DEFAULT"));
    builder.addTarget(helper.getRequirement2());
    final DependencyGraph graph1 = builder.getDependencyGraph();
    assertNotNull(graph1);
    assertEquals(1, graph1.getDependencyNodes().size());
    builder = helper.createBuilder(null);
    builder.setDependencyGraph(graph1);
    builder.addTarget(helper.getRequirement1());
    final DependencyGraph graph2 = builder.getDependencyGraph();
    assertNotNull(graph2);
    final Collection<DependencyNode> graph2Nodes = graph2.getDependencyNodes();
    assertEquals(2, graph2Nodes.size());
    assertTrue(graph2Nodes.containsAll(graph1.getDependencyNodes()));
    builder = helper.createBuilder(null);
    for (DependencyNode node : graph2Nodes) {
      new DependencyNode(ComputationTarget.NULL).addInputNode(node);
      if (node.getOutputValues().contains(helper.getSpec1())) {
        assertEquals(1, node.getDependentNodes().size());
      } else {
        assertEquals(2, node.getDependentNodes().size());
      }
    }
    // graph2 is now a subgraph of a larger graph containing two more nodes
    builder.setDependencyGraph(graph2);
    builder.addTarget(helper.getRequirement2Foo());
    final DependencyGraph graph3 = builder.getDependencyGraph();
    assertNotNull(graph3);
    assertEquals(2, graph3.getDependencyNodes().size());
    assertEquals(graph2.getDependencyNodes(), graph3.getDependencyNodes());
    // the build should have modified the supplied nodes
    for (DependencyNode node : graph2Nodes) {
      if (node.getOutputValues().contains(helper.getSpec1())) {
        assertEquals(0, node.getDependentNodes().size());
      } else {
        assertEquals(1, node.getDependentNodes().size());
      }
    }
  }

}

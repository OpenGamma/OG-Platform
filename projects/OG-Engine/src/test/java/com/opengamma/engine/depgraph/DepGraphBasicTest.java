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
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the dependency graph building with trivial constructions.
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphBasicTest extends AbstractDependencyGraphBuilderTest {

  public void singleOutputSingleFunctionNode() {
    TestLifecycle.begin();
    try {
      DepGraphTestHelper helper = helper();
      MockFunction function = helper.addFunctionProducing1and2();
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(Sets.newHashSet(helper.getRequirement1()));
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertTrue(DependencyGraphImpl.getAllOutputSpecifications(graph).contains(helper.getSpec1()));
      assertTrue(DependencyGraphImpl.getAllOutputSpecifications(graph).contains(helper.getSpec2()));
      Collection<DependencyNode> nodes = DependencyGraphImpl.getDependencyNodes(graph);
      assertNotNull(nodes);
      assertEquals(1, nodes.size());
      DependencyNode node = nodes.iterator().next();
      assertEquals(function.getUniqueId(), node.getFunction().getFunctionId());
      assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
      assertTrue(node.hasOutputValue(helper.getSpec1()));
      assertTrue(node.hasOutputValue(helper.getSpec2()));
      assertEquals(0, node.getInputCount());
      assertEquals(helper.getTarget().toSpecification(), node.getTarget());
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      nodes = DependencyGraphImpl.getDependencyNodes(graph);
      assertNotNull(nodes);
      assertEquals(1, nodes.size());
      node = nodes.iterator().next();
      assertEquals(function.getUniqueId(), node.getFunction().getFunctionId());
      assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
      assertTrue(node.hasOutputValue(helper.getSpec1()));
      assertFalse(node.hasOutputValue(helper.getSpec2()));
      assertEquals(0, node.getInputCount());
    } finally {
      TestLifecycle.end();
    }
  }

  /**
   * When you have multiple requirements produced by the same function, should only have a single node using that function.
   */
  public void multipleOutputsSingleFunctionNode() {
    TestLifecycle.begin();
    try {
      DepGraphTestHelper helper = helper();
      MockFunction function = helper.addFunctionProducing1and2();
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(Sets.newHashSet(helper.getRequirement1()));
      builder.addTarget(Sets.newHashSet(helper.getRequirement2()));
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      Collection<DependencyNode> nodes = DependencyGraphImpl.getDependencyNodes(graph);
      assertNotNull(nodes);
      assertEquals(1, nodes.size());
      DependencyNode node = nodes.iterator().next();
      assertEquals(function.getUniqueId(), node.getFunction().getFunctionId());
      assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
      assertTrue(node.hasOutputValue(helper.getSpec1()));
      assertTrue(node.hasOutputValue(helper.getSpec2()));
      assertEquals(0, node.getInputCount());
    } finally {
      TestLifecycle.end();
    }
  }

  public void unsatisfiableDependency() {
    TestLifecycle.begin();
    try {
      DepGraphTestHelper helper = helper();
      helper.addFunctionProducing1and2();
      ValueRequirement anotherReq = new ValueRequirement("Req-3", helper.getTarget().toSpecification());
      DependencyGraphBuilder builder = helper.createBuilder(null);
      expectCompletion(builder, builder.getContext().resolveRequirement(helper.getRequirement1(), null, null));
      expectFailure(builder, builder.getContext().resolveRequirement(anotherReq, null, null));
    } finally {
      TestLifecycle.end();
    }
  }

  public void doubleLevelNoLiveData() {
    TestLifecycle.begin();
    try {
      DepGraphTestHelper helper = helper();
      MockFunction fn1 = helper.addFunctionRequiring2Producing1();
      MockFunction fn2 = helper.addFunctionProducing2();
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      assertTrue(DependencyGraphImpl.getAllOutputSpecifications(graph).contains(helper.getSpec1()));
      assertTrue(DependencyGraphImpl.getAllOutputSpecifications(graph).contains(helper.getSpec2()));
      Collection<DependencyNode> nodes = DependencyGraphImpl.getDependencyNodes(graph);
      assertNotNull(nodes);
      assertEquals(2, nodes.size());
      for (DependencyNode node : nodes) {
        if (ObjectUtils.equals(node.getFunction().getFunctionId(), fn1.getUniqueId())) {
          assertTrue(node.hasOutputValue(helper.getSpec1()));
          assertFalse(node.hasOutputValue(helper.getSpec2()));
          assertEquals(1, node.getInputCount());
          assertEquals(helper.getSpec2(), node.getInputValue(0));
          assertEquals(helper.getTarget().toSpecification(), node.getTarget());
        } else if (ObjectUtils.equals(node.getFunction().getFunctionId(), fn2.getUniqueId())) {
          assertFalse(node.hasOutputValue(helper.getSpec1()));
          assertTrue(node.hasOutputValue(helper.getSpec2()));
          assertEquals(0, node.getInputCount());
        } else {
          Assert.fail("Unexpected function definition");
        }
      }
    } finally {
      TestLifecycle.end();
    }
  }

  public void doubleLevelLiveData() {
    TestLifecycle.begin();
    try {
      DepGraphTestHelper helper = helper();
      MockFunction fn1 = helper.addFunctionRequiring2Producing1();
      helper.make2AvailableFromLiveData();
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      assertTrue(DependencyGraphImpl.getAllOutputSpecifications(graph).contains(helper.getSpec1()));
      Collection<DependencyNode> nodes = DependencyGraphImpl.getDependencyNodes(graph);
      assertNotNull(nodes);
      assertEquals(2, nodes.size());
      for (DependencyNode node : nodes) {
        if (ObjectUtils.equals(node.getFunction().getFunctionId(), fn1.getUniqueId())) {
          assertTrue(node.hasOutputValue(helper.getSpec1()));
          assertEquals(1, node.getInputCount());
          ValueSpecification inputSpec = node.getInputValue(0);
          assertEquals(helper.getSpec2().getValueName(), inputSpec.getValueName());
          assertEquals(helper.getSpec2().getTargetSpecification(), inputSpec.getTargetSpecification());
          assertEquals(helper.getTarget().toSpecification(), node.getTarget());
        } else if (MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
          assertFalse(node.hasOutputValue(helper.getSpec1()));
          assertEquals(1, node.getOutputCount());
          ValueSpecification outputSpec = node.getOutputValue(0);
          assertEquals(helper.getSpec2().getValueName(), outputSpec.getValueName());
          assertEquals(helper.getSpec2().getTargetSpecification(), outputSpec.getTargetSpecification());
          assertEquals(0, node.getInputCount());
        } else {
          Assert.fail("Unexpected function definition");
        }
      }
    } finally {
      TestLifecycle.end();
    }
  }

  public void incrementalBuild() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      helper.addFunctionRequiring2Producing1();
      helper.addFunctionProducing2();
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.setDependencyGraph(new DependencyGraphImpl("DEFAULT", Collections.<DependencyNode>emptySet(), 0, Collections.<ValueSpecification, Set<ValueRequirement>>emptyMap()));
      builder.addTarget(helper.getRequirement2());
      final DependencyGraph graph1 = builder.getDependencyGraph();
      assertNotNull(graph1);
      assertEquals(1, graph1.getSize());
      builder = helper.createBuilder(null);
      builder.setDependencyGraph(graph1);
      builder.addTarget(helper.getRequirement1());
      final DependencyGraph graph2 = builder.getDependencyGraph();
      assertNotNull(graph2);
      final Collection<DependencyNode> graph2Nodes = DependencyGraphImpl.getDependencyNodes(graph2);
      assertEquals(2, graph2Nodes.size());
      assertTrue(graph2Nodes.containsAll(DependencyGraphImpl.getDependencyNodes(graph1)));
    } finally {
      TestLifecycle.end();
    }
  }

}

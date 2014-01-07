/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.InvalidTargetDependencyNodeFilter;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for the {@link RootDiscardingSubgrapher} class.
 */
@Test(groups = TestGroup.UNIT)
public class RootDiscardingSubgrapherTest {

  private UniqueId id(final int id) {
    return UniqueId.of("Test", Integer.toString(id));
  }

  private ComputationTargetSpecification target(final int id) {
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, id(id));
  }

  private ValueRequirement req(final int id) {
    return new ValueRequirement("V", target(id));
  }

  private NodeBuilder node(final TestDependencyGraphBuilder builder, final int id) {
    return builder.addNode("F", target(id));
  }

  /**
   * Creates a graph of the form:
   * 
   * <pre>
   *      N1 -> N2*
   * </pre>
   */
  private DependencyGraph smallGraph() {
    final TestDependencyGraphBuilder builder = new TestDependencyGraphBuilder("small");
    NodeBuilder nb = node(builder, 1);
    final ValueSpecification v1 = nb.addOutput("V");
    nb = node(builder, 2);
    nb.addInput(v1);
    nb.addTerminalOutput("V");
    return builder.buildGraph();
  }

  /**
   * Creates a graph of the form:
   * 
   * <pre>
   *    N1 ---> N4*-------
   *                       \
   *    N2 ---> N5 \        > N8*
   *        \        N7*---/
   *         ->    /
   *    N3 ---> N6
   * </pre>
   */
  private DependencyGraph largeGraph() {
    final TestDependencyGraphBuilder builder = new TestDependencyGraphBuilder("large");
    NodeBuilder nb = node(builder, 1);
    final ValueSpecification v1 = nb.addOutput("V");
    nb = node(builder, 2);
    final ValueSpecification v2 = nb.addOutput("V");
    nb = node(builder, 3);
    final ValueSpecification v3 = nb.addOutput("V");
    nb = node(builder, 4);
    nb.addInput(v1);
    final ValueSpecification v4 = nb.addTerminalOutput("V");
    nb = node(builder, 5);
    nb.addInput(v2);
    final ValueSpecification v5 = nb.addOutput("V");
    nb = node(builder, 6);
    nb.addInput(v2);
    nb.addInput(v3);
    final ValueSpecification v6 = nb.addOutput("V");
    nb = node(builder, 7);
    nb.addInput(v5);
    nb.addInput(v6);
    final ValueSpecification v7 = nb.addTerminalOutput("V");
    nb = node(builder, 8);
    nb.addInput(v4);
    nb.addInput(v7);
    nb.addTerminalOutput("V");
    return builder.buildGraph();
  }

  public void testReturnSameGraph() {
    final RootDiscardingSubgrapher filter = new RootDiscardingSubgrapher() {
      @Override
      public boolean acceptNode(final DependencyNode node) {
        return true;
      }
    };
    DependencyGraph graph = smallGraph();
    assertSame(filter.subGraph(graph, null), graph);
    graph = largeGraph();
    assertSame(filter.subGraph(graph, null), graph);
    graph = smallGraph();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), null), DependencyGraphImpl.getRootNodes(graph));
    graph = largeGraph();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), null), DependencyGraphImpl.getRootNodes(graph));
  }

  public void testReturnEmptyGraph() {
    final RootDiscardingSubgrapher filter = new RootDiscardingSubgrapher() {
      @Override
      public boolean acceptNode(final DependencyNode node) {
        return false;
      }
    };
    assertNull(filter.subGraph(smallGraph(), null));
    assertNull(filter.subGraph(largeGraph(), null));
    DependencyGraph graph = smallGraph();
    assertNull(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), null));
    graph = largeGraph();
    assertNull(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), null));
  }

  public void testLeafNode1() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(1)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 5);
    assertEquals(missing, ImmutableSet.of(req(4), req(8)));
  }

  public void testLeafNode2() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(2)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 3);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
    missing.clear();
    assertEquals(filter.subGraph(smallGraph(), missing).getSize(), 1);
    assertEquals(missing, ImmutableSet.of(req(2)));
    missing.clear();
    DependencyGraph graph = largeGraph();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 2);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
    missing.clear();
    graph = smallGraph();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 1);
    assertEquals(missing, ImmutableSet.of(req(2)));
  }

  public void testLeafNode3() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(3)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    final DependencyGraph graph = largeGraph();
    assertEquals(filter.subGraph(graph, missing).getSize(), 4);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
    missing.clear();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 2);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

  public void testLeafNodes1and3() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(1), id(3)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    final DependencyGraph graph = largeGraph();
    assertEquals(filter.subGraph(graph, missing).getSize(), 2);
    assertEquals(missing, ImmutableSet.of(req(4), req(7), req(8)));
    missing.clear();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 1);
    assertEquals(missing, ImmutableSet.of(req(4), req(7), req(8)));
  }

  public void testMiddleNode() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(5)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    final DependencyGraph graph = largeGraph();
    assertEquals(filter.subGraph(graph, missing).getSize(), 5);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
    missing.clear();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 2);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

  public void testRootNode() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(8)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    final DependencyGraph graph = largeGraph();
    assertEquals(filter.subGraph(graph, missing).getSize(), 7);
    assertEquals(missing, ImmutableSet.of(req(8)));
    missing.clear();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 2);
    assertEquals(missing, ImmutableSet.of(req(8)));
  }

  public void testRootAndLeafNode3() {
    final RootDiscardingSubgrapher filter = new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(3), id(8)));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    final DependencyGraph graph = largeGraph();
    assertEquals(filter.subGraph(graph, missing).getSize(), 4);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
    missing.clear();
    assertEquals(filter.subGraph(DependencyGraphImpl.getRootNodes(graph), graph.getTerminalOutputs(), missing).size(), 2);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

}

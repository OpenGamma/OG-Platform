/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for the {@link SubGraphingFilter} class.
 */
@Test(groups = TestGroup.UNIT)
public class SubGraphingFilterTest {

  private UniqueId id(final int id) {
    return UniqueId.of("Test", Integer.toString(id));
  }

  private ComputationTargetSpecification target(final int id) {
    return new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, id(id));
  }

  private DependencyNode node(final int id) {
    final DependencyNode node = new DependencyNode(target(id));
    node.setFunction(Mockito.mock(ParameterizedFunction.class));
    return node;
  }

  private DependencyNode tnode(final int id) {
    final DependencyNode node = node(id);
    node.addTerminalOutputValue(new ValueSpecification("V", node.getComputationTarget(), ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get()));
    return node;
  }

  private DependencyGraph graph(final DependencyNode... nodes) {
    final DependencyGraph graph = new DependencyGraph("Default");
    for (DependencyNode node : nodes) {
      graph.addDependencyNode(node);
      if (node.hasTerminalOutputValues()) {
        graph.addTerminalOutput(new ValueRequirement("V", node.getComputationTarget()), node.getTerminalOutputValues().iterator().next());
      }
    }
    return graph;
  }

  private ValueRequirement req(final int id) {
    return new ValueRequirement("V", target(id));
  }

  /**
   * Creates a graph of the form:
   * 
   * <pre>
   *      N1 -> N2*
   * </pre>
   */
  private DependencyGraph smallGraph() {
    final DependencyNode n1 = node(1);
    final DependencyNode n2 = tnode(2);
    n2.addInputNode(n1);
    return graph(n1, n2);
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
    final DependencyNode n1 = node(1);
    final DependencyNode n2 = node(2);
    final DependencyNode n3 = node(3);
    final DependencyNode n4 = tnode(4);
    final DependencyNode n5 = node(5);
    final DependencyNode n6 = node(6);
    final DependencyNode n7 = tnode(7);
    final DependencyNode n8 = tnode(8);
    n4.addInputNode(n1);
    n5.addInputNode(n2);
    n6.addInputNode(n2);
    n6.addInputNode(n3);
    n7.addInputNode(n5);
    n7.addInputNode(n6);
    n8.addInputNode(n4);
    n8.addInputNode(n7);
    return graph(n1, n2, n3, n4, n5, n6, n7, n8);
  }

  public void testReturnSameGraph() {
    final SubGraphingFilter filter = new SubGraphingFilter(new DependencyNodeFilter() {
      @Override
      public boolean accept(final DependencyNode node) {
        return true;
      }
    });
    DependencyGraph graph = smallGraph();
    assertSame(filter.subGraph(graph, null), graph);
    graph = largeGraph();
    assertSame(filter.subGraph(graph, null), graph);
    graph = smallGraph();
    assertSame(filter.subGraph(graph, null), graph);
  }

  public void testReturnEmptyGraph() {
    final SubGraphingFilter filter = new SubGraphingFilter(new DependencyNodeFilter() {
      @Override
      public boolean accept(final DependencyNode node) {
        return false;
      }
    });
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(smallGraph(), missing).getSize(), 0);
    assertEquals(missing.size(), 1);
    missing.clear();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 0);
    assertEquals(missing.size(), 3);
    missing.clear();
    assertEquals(filter.subGraph(smallGraph(), missing).getSize(), 0);
    assertEquals(missing.size(), 1);
  }

  public void testLeafNode1() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(1))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 5);
    assertEquals(missing, ImmutableSet.of(req(4), req(8)));
  }

  public void testLeafNode2() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(2))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 3);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
    missing.clear();
    assertEquals(filter.subGraph(smallGraph(), missing).getSize(), 1);
    assertEquals(missing, ImmutableSet.of(req(2)));
    missing.clear();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 3);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

  public void testLeafNode3() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(3))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 4);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

  public void testLeafNodes1and3() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(1), id(3))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 2);
    assertEquals(missing, ImmutableSet.of(req(4), req(7), req(8)));
  }

  public void testMiddleNode() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(5))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 5);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

  public void testRootNode() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(8))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 7);
    assertEquals(missing, ImmutableSet.of(req(8)));
  }

  public void testRootAndLeafNode3() {
    final SubGraphingFilter filter = new SubGraphingFilter(new InvalidTargetDependencyNodeFilter(ImmutableSet.of(id(3), id(8))));
    final Set<ValueRequirement> missing = new HashSet<ValueRequirement>();
    assertEquals(filter.subGraph(largeGraph(), missing).getSize(), 4);
    assertEquals(missing, ImmutableSet.of(req(7), req(8)));
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DependencyGraphImpl} class.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphImplTest {

  public void testEmpty() {
    final TestDependencyGraphBuilder builder = new TestDependencyGraphBuilder("Empty");
    final DependencyGraph graph = builder.buildGraph();
    assertEquals(graph.getCalculationConfigurationName(), "Empty");
    assertEquals(graph.getSize(), 0);
    assertEquals(graph.getTerminalOutputs(), Collections.<ValueSpecification, Set<ValueRequirement>>emptyMap());
    assertEquals(DependencyGraphImpl.getDependencyNodes(graph), Collections.emptySet());
    assertSame(DependencyGraphImpl.removeUnnecessaryValues(graph), graph);
  }

  public void testSimple() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("Small");
    NodeBuilder nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification spec1 = nb.addOutput("A");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification spec2 = nb.addOutput("B");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    nb.addInput(spec1);
    nb.addInput(spec2);
    final ValueSpecification spec3 = nb.addOutput("C");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification spec4 = nb.addTerminalOutput("D");
    nb.addInput(spec3);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification spec5 = nb.addTerminalOutput("E");
    nb.addInput(spec3);
    final DependencyGraph graph = gb.buildGraph();
    assertEquals(graph.getCalculationConfigurationName(), "Small");
    assertEquals(graph.getSize(), 5);
    assertEquals(
        graph.getTerminalOutputs(),
        ImmutableMap.of(spec4, Collections.singleton(new ValueRequirement("D", spec4.getTargetSpecification())), spec5,
            Collections.singleton(new ValueRequirement("E", spec5.getTargetSpecification()))));
    assertEquals(DependencyGraphImpl.getDependencyNodes(graph).size(), 5);
    final Iterator<DependencyNode> itr = graph.nodeIterator();
    // The checks below assume a specific ordering of the iterator
    assertEquals(ImmutableSet.of(itr.next().getOutputValue(0), itr.next().getOutputValue(0)), ImmutableSet.of(spec1, spec2));
    assertEquals(itr.next().getOutputValue(0), spec3);
    assertEquals(ImmutableSet.of(itr.next().getOutputValue(0), itr.next().getOutputValue(0)), ImmutableSet.of(spec4, spec5));
    assertFalse(itr.hasNext());
    assertSame(DependencyGraphImpl.removeUnnecessaryValues(graph), graph);
  }

  public void testRemoveUnnecessaryValues_1() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("Redundant");
    NodeBuilder nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification a = nb.addOutput("A");
    nb.addOutput("X");
    final ValueSpecification b = gb.addNode("Test", ComputationTargetSpecification.NULL).addOutput("B");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification c = nb.addOutput("C");
    final ValueSpecification y = nb.addTerminalOutput("Y");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification d = nb.addOutput("D");
    nb.addOutput("Z");
    nb.addInput(a);
    nb.addInput(b);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification e = nb.addOutput("E");
    nb.addInput(b);
    nb.addInput(c);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification f = nb.addTerminalOutput("F");
    nb.addInput(d);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    nb.addOutput("G");
    nb.addInput(d);
    nb.addInput(e);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    nb.addOutput("H");
    nb.addInput(e);
    final DependencyGraph graph = gb.buildGraph();
    assertEquals(graph.getRootCount(), 3);
    assertEquals(graph.getSize(), 8);
    final DependencyGraph graph2 = DependencyGraphImpl.removeUnnecessaryValues(graph);
    assertEquals(graph2.getRootCount(), 2);
    assertEquals(ImmutableSet.of(graph2.getRootNode(0).getOutputValue(0), graph2.getRootNode(1).getOutputValue(0)), ImmutableSet.of(f, y));
    assertEquals(graph2.getSize(), 5);
    assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph2), ImmutableSet.of(a, b, d, f, y));
  }

  public void testRemoveUnnecessaryValues_2() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("Redundant");
    NodeBuilder nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification a = nb.addOutput("A");
    nb.addOutput("X");
    final ValueSpecification b = gb.addNode("Test", ComputationTargetSpecification.NULL).addOutput("B");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification c = nb.addOutput("C");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification d = nb.addOutput("D");
    nb.addOutput("Z");
    nb.addInput(a);
    nb.addInput(b);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification e = nb.addOutput("E");
    nb.addInput(b);
    nb.addInput(c);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification f = nb.addTerminalOutput("F");
    nb.addInput(d);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    nb.addOutput("G");
    nb.addInput(d);
    nb.addInput(e);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    nb.addOutput("H");
    nb.addInput(e);
    final DependencyGraph graph = gb.buildGraph();
    assertEquals(graph.getRootCount(), 3);
    assertEquals(graph.getSize(), 8);
    final DependencyGraph graph2 = DependencyGraphImpl.removeUnnecessaryValues(graph);
    assertEquals(graph2.getRootCount(), 1);
    assertEquals(ImmutableSet.of(graph2.getRootNode(0).getOutputValue(0)), ImmutableSet.of(f));
    assertEquals(graph2.getSize(), 4);
    assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph2), ImmutableSet.of(a, b, d, f));
  }

  public void testRemoveUnnecessaryValues_3() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("Redundant");
    NodeBuilder nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification a = nb.addOutput("A");
    nb.addOutput("X");
    final ValueSpecification b = gb.addNode("Test", ComputationTargetSpecification.NULL).addOutput("B");
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification d = nb.addOutput("D");
    nb.addOutput("Z");
    nb.addInput(a);
    nb.addInput(b);
    nb = gb.addNode("Test", ComputationTargetSpecification.NULL);
    final ValueSpecification f = nb.addTerminalOutput("F");
    nb.addInput(d);
    final DependencyGraph graph = gb.buildGraph();
    assertEquals(graph.getRootCount(), 1);
    assertEquals(graph.getSize(), 4);
    final DependencyGraph graph2 = DependencyGraphImpl.removeUnnecessaryValues(graph);
    assertEquals(graph2.getRootCount(), 1);
    assertEquals(ImmutableSet.of(graph2.getRootNode(0).getOutputValue(0)), ImmutableSet.of(f));
    assertEquals(graph2.getSize(), 4);
    assertNotSame(graph, graph2);
    assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph2), ImmutableSet.of(a, b, d, f));
  }

  public void testRemoveUnnecessaryValues_4() {
    // [PLAT-4922] No unnecessary values but graph gets corrupted
    final ValueSpecification[] v = new ValueSpecification[5];
    final ValueRequirement[] r = new ValueRequirement[5];
    for (int i = 0; i < v.length; i++) {
      v[i] = new ValueSpecification(Integer.toString(i), ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get());
      r[i] = new ValueRequirement(Integer.toString(i), ComputationTargetSpecification.NULL);
    }
    final DependencyNodeFunction function = DependencyNodeFunctionImpl.of("Test", EmptyFunctionParameters.INSTANCE);
    final DependencyNode a = DependencyNodeImpl.of(function, ComputationTargetSpecification.NULL, new ValueSpecification[] {v[0], v[1], v[2] }, new ValueSpecification[0], new DependencyNode[0]);
    final DependencyNode b = DependencyNodeImpl.of(function, ComputationTargetSpecification.NULL, new ValueSpecification[] {v[3] }, new ValueSpecification[] {v[2] }, new DependencyNode[] {a });
    final DependencyNode c = DependencyNodeImpl.of(function, ComputationTargetSpecification.NULL, new ValueSpecification[] {v[4] }, new ValueSpecification[] {v[1], v[3] },
        new DependencyNode[] {a, b });
    final DependencyGraph graphA = new DependencyGraphImpl("Test", Collections.singleton(c), 3, ImmutableMap.of(v[0], Collections.singleton(r[0]), v[4], Collections.singleton(r[4])));
    final DependencyGraph graphB = DependencyGraphImpl.removeUnnecessaryValues(graphA);
    assertEquals(graphB, graphA);
  }

}

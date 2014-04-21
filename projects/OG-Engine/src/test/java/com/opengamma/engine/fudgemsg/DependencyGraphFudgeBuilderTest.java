/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DependencyGraphFudgeBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  ///
  // Creates a test graph:
  // 
  //    N0 N1  N4
  //      \ | /  |
  //       N2   N3
  //
  private DependencyGraph createGraph() {
    final TestDependencyGraphBuilder builder = new TestDependencyGraphBuilder("Test");
    final NodeBuilder[] nodes = new NodeBuilder[5];
    for (int i = 0; i < nodes.length; i++) {
      final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("Test", Integer.toString(i)));
      nodes[i] = builder.addNode(DependencyNodeFunctionImpl.of("Function" + (i % 2), (i == 3) ? new SimpleFunctionParameters(ImmutableMap.of("Foo", "Bar")) : EmptyFunctionParameters.INSTANCE),
          targetSpec);
    }
    nodes[0].addTerminalOutput("0x");
    nodes[1].addTerminalOutput("1x");
    final ValueSpecification v20 = nodes[2].addOutput("20");
    final ValueSpecification v21 = nodes[2].addOutput("21");
    final ValueSpecification v24 = nodes[2].addOutput("24");
    final ValueSpecification v34 = nodes[3].addOutput("34");
    nodes[4].addTerminalOutput("4x");
    nodes[0].addInput(v20);
    nodes[1].addInput(v21);
    nodes[4].addInput(v24);
    nodes[4].addInput(v34);
    return builder.buildGraph();
  }

  @Test
  public void testDependencyGraphFudgeCycle() {
    final DependencyGraph rawGraph = createGraph();
    final DependencyGraph cycledGraph = cycleObject(DependencyGraph.class, rawGraph);
    assertEquals(cycledGraph.getCalculationConfigurationName(), rawGraph.getCalculationConfigurationName());
    assertEquals(cycledGraph.getRootCount(), rawGraph.getRootCount());
    assertEquals(cycledGraph.getSize(), rawGraph.getSize());
    assertEquals(cycledGraph.getTerminalOutputs(), rawGraph.getTerminalOutputs());
    final Iterator<DependencyNode> itrRaw = rawGraph.nodeIterator();
    final Iterator<DependencyNode> itrCycled = cycledGraph.nodeIterator();
    while (itrRaw.hasNext()) {
      assertTrue(itrCycled.hasNext());
      final DependencyNode raw = itrRaw.next();
      final DependencyNode cycled = itrCycled.next();
      assertEquals(cycled.getFunction(), raw.getFunction());
      assertEquals(cycled.getTarget(), raw.getTarget());
      assertEquals(DependencyNodeImpl.getInputValues(cycled), DependencyNodeImpl.getInputValues(raw));
      assertEquals(DependencyNodeImpl.getOutputValues(cycled), DependencyNodeImpl.getOutputValues(raw));
    }
    assertFalse(itrCycled.hasNext());
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DependencyGraphFudgeBuilder}
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void testCycleSimpleGraph() {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = env.compileViewDefinition(Instant.now(), VersionCorrection.LATEST);
    final DependencyGraph graph = compiledViewDefinition.getDependencyGraphExplorer(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME).getWholeGraph();
    final DependencyGraph cycledGraph = cycleObject(DependencyGraph.class, graph);

    assertEquals(graph.getCalculationConfigurationName(), cycledGraph.getCalculationConfigurationName());
    assertEquals(graph.getAllComputationTargets(), cycledGraph.getAllComputationTargets());
    assertEquals(graph.getOutputSpecifications(), cycledGraph.getOutputSpecifications());
    assertEquals(graph.getSize(), cycledGraph.getSize());
    assertEquals(graph.getTerminalOutputSpecifications(), cycledGraph.getTerminalOutputSpecifications());
    //assertEquals(graph.getAllRequiredMarketData(), cycledGraph.getAllRequiredMarketData()); [PLAT-3126]

    for (final DependencyNode node : graph.getDependencyNodes()) {
      final boolean isRoot = graph.getRootNodes().contains(node);
      for (final ValueSpecification spec : node.getOutputValues()) {
        final DependencyNode equivalentNode = cycledGraph.getNodeProducing(spec);
        assertEquals(isRoot, cycledGraph.getRootNodes().contains(equivalentNode));
        assertEquals(node.getInputValues(), equivalentNode.getInputValues());
        assertEquals(node.getOutputValues(), equivalentNode.getOutputValues());
        assertEquals(node.getTerminalOutputValues(), equivalentNode.getTerminalOutputValues());
      }
    }
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.engine.depgraph.DepGraphTestHelper;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests ResultOutputMode
 */
@Test(groups = TestGroup.UNIT)
public class ResultOutputModeTest {

  public void testOutputModes() {
    TestLifecycle.begin();
    try {
      DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing1and2();
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(Collections.singleton(helper.getRequirement1()));
      DependencyGraph graph = builder.getDependencyGraph();
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      assertEquals(1, graph.getSize());

      // Nothing should be included in the output
      assertFalse(ResultOutputMode.NONE.shouldOutputResult(helper.getSpec1(), graph));
      assertFalse(ResultOutputMode.NONE.shouldOutputResult(helper.getSpec2(), graph));

      // The node contains one terminal output, so the function should be executed, and only this result should be included in the output
      assertTrue(ResultOutputMode.TERMINAL_OUTPUTS.shouldOutputResult(helper.getSpec1(), graph));
      assertFalse(ResultOutputMode.TERMINAL_OUTPUTS.shouldOutputResult(helper.getSpec2(), graph));

      // Everything should be included in the output
      assertTrue(ResultOutputMode.ALL.shouldOutputResult(helper.getSpec1(), graph));
      assertTrue(ResultOutputMode.ALL.shouldOutputResult(helper.getSpec2(), graph));
    } finally {
      TestLifecycle.end();
    }
  }

}

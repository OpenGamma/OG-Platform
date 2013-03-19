/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.resolver.ApplyToAllTargets;
import com.opengamma.engine.function.resolver.ComputationTargetFilter;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link DependencyGraph}.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphTest {

  public void testSubgraphFilter() {
    final DependencyGraph graph = new DependencyGraph("Default");
    for (int i = 0; i < 10; i++) {
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Foo", Integer.toString(i)));
      final DependencyNode node = new DependencyNode(target);
      node.setFunction(new MockFunction(target));
      graph.addDependencyNode(node);
    }
    DependencyGraph filtered = graph.subGraph(ApplyToAllTargets.INSTANCE);
    assertEquals(filtered.getDependencyNodes(), graph.getDependencyNodes());
    filtered = graph.subGraph(new ComputationTargetFilter(new DefaultComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST)) {
      @Override
      public boolean accept(final ComputationTarget target) {
        return target.getUniqueId().getValue().compareTo("5") >= 0;
      }
    });
    assertEquals(filtered.getDependencyNodes().size(), 5);
  }

}

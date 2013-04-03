/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test DependencyNode.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyNodeTest {
  
  public void testDependentNodes() {
    String domain = "test";
    
    DependencyNode node0 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(domain, "0")));
    DependencyNode node1 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(domain, "1")));
    DependencyNode node2 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(domain, "2")));
    DependencyNode node3 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(domain, "3")));
    DependencyNode node4 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(domain, "4")));
    DependencyNode node5 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of(domain, "5")));
    
    node0.addInputNode(node1);
    node0.addInputNode(node2);
    node0.addInputNode(node3);
    
    node2.addInputNode(node1);
    node2.addInputNode(node3);

    node4.addInputNode(node3);
    
    AssertJUnit.assertEquals(0, node5.getDependentNodes().size());
    AssertJUnit.assertEquals(1, node2.getDependentNodes().size());
    AssertJUnit.assertTrue(node2.getDependentNodes().contains(node0));
    AssertJUnit.assertEquals(2, node1.getDependentNodes().size());
    AssertJUnit.assertTrue(node1.getDependentNodes().contains(node0));
    AssertJUnit.assertTrue(node1.getDependentNodes().contains(node2));
    AssertJUnit.assertEquals(3, node3.getDependentNodes().size());
    AssertJUnit.assertTrue(node3.getDependentNodes().contains(node0));
    AssertJUnit.assertTrue(node3.getDependentNodes().contains(node2));
    AssertJUnit.assertTrue(node3.getDependentNodes().contains(node4));
  }
}

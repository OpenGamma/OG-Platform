/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import org.junit.Assert;
import org.junit.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;

/**
 * Test DependencyNode.
 */
public class DependencyNodeTest {
  
  @Test
  public void testDependentNodes() {
    IdentificationScheme domain = IdentificationScheme.of("test");
    
    DependencyNode node0 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, Identifier.of(domain, "0")));
    DependencyNode node1 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, Identifier.of(domain, "1")));
    DependencyNode node2 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, Identifier.of(domain, "2")));
    DependencyNode node3 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, Identifier.of(domain, "3")));
    DependencyNode node4 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, Identifier.of(domain, "4")));
    DependencyNode node5 = new DependencyNode(new ComputationTarget(ComputationTargetType.PRIMITIVE, Identifier.of(domain, "5")));
    
    node0.addInputNode(node1);
    node0.addInputNode(node2);
    node0.addInputNode(node3);
    
    node2.addInputNode(node1);
    node2.addInputNode(node3);

    node4.addInputNode(node3);
    
    Assert.assertEquals(0, node5.getDependentNodes().size());
    Assert.assertEquals(1, node2.getDependentNodes().size());
    Assert.assertTrue(node2.getDependentNodes().contains(node0));
    Assert.assertEquals(2, node1.getDependentNodes().size());
    Assert.assertTrue(node1.getDependentNodes().contains(node0));
    Assert.assertTrue(node1.getDependentNodes().contains(node2));
    Assert.assertEquals(3, node3.getDependentNodes().size());
    Assert.assertTrue(node3.getDependentNodes().contains(node0));
    Assert.assertTrue(node3.getDependentNodes().contains(node2));
    Assert.assertTrue(node3.getDependentNodes().contains(node4));
  }
}

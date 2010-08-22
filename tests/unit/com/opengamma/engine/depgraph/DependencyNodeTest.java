/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;

/**
 * Test DependencyNode.
 */
public class DependencyNodeTest {
  
  @Test
  public void testDependentNodes() {
    IdentificationScheme domain = new IdentificationScheme("test");
    
    FunctionDefinition function = new MockFunction(new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD"));
    
    DependencyNode node1 = new DependencyNode(function, 
        new ComputationTarget(ComputationTargetType.PRIMITIVE, new Identifier(domain, "1")),
        Collections.<DependencyNode>emptySet(),
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueSpecification>emptySet());
    DependencyNode node3 = new DependencyNode(function, 
        new ComputationTarget(ComputationTargetType.PRIMITIVE, new Identifier(domain, "3")),
        Collections.<DependencyNode>emptySet(),
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueSpecification>emptySet());
    DependencyNode node5 = new DependencyNode(function, 
        new ComputationTarget(ComputationTargetType.PRIMITIVE, new Identifier(domain, "5")),
        Collections.<DependencyNode>emptySet(),
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueSpecification>emptySet());
    
    DependencyNode node4 = new DependencyNode(function, 
        new ComputationTarget(ComputationTargetType.PRIMITIVE, new Identifier(domain, "4")),
        Sets.newHashSet(node3),
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueSpecification>emptySet());

    DependencyNode node2 = new DependencyNode(function, 
        new ComputationTarget(ComputationTargetType.PRIMITIVE, new Identifier(domain, "2")),
        Sets.newHashSet(node1, node3),
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueSpecification>emptySet());
    
    DependencyNode node0 = new DependencyNode(function, 
        new ComputationTarget(ComputationTargetType.PRIMITIVE, new Identifier(domain, "0")),
        Sets.newHashSet(node1, node2, node3),
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueSpecification>emptySet());

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

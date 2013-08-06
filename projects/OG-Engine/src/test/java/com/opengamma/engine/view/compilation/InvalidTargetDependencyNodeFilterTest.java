/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for the {@link InvalidTargetDependencyNodeFilter} class.
 */
@Test(groups = TestGroup.UNIT)
public class InvalidTargetDependencyNodeFilterTest {

  public void testAccept() {
    final Set<UniqueId> invalid = ImmutableSet.of(UniqueId.of("Sec", "1"), UniqueId.of("Pos", "2"), UniqueId.of("Pos", "3", "X"));
    final InvalidTargetDependencyNodeFilter filter = new InvalidTargetDependencyNodeFilter(invalid);
    assertTrue(filter.accept(new DependencyNode(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "1")))));
    assertFalse(filter.accept(new DependencyNode(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "2")))));
    assertTrue(filter.accept(new DependencyNode(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "2", "V")))));
    assertTrue(filter.accept(new DependencyNode(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "3")))));
    assertFalse(filter.accept(new DependencyNode(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "3", "X")))));
    assertTrue(filter.accept(new DependencyNode(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "3", "Y")))));
    assertTrue(filter.accept(new DependencyNode(ComputationTargetSpecification.NULL)));
  }
}

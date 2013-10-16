/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueSpecification;
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
    final DependencyNodeFunction function = DependencyNodeFunctionImpl.of(MarketDataSourcingFunction.INSTANCE);
    final Set<ValueSpecification> outputs = Collections.<ValueSpecification>emptySet();
    final Map<ValueSpecification, DependencyNode> inputs = Collections.<ValueSpecification, DependencyNode>emptyMap();
    assertTrue(filter.acceptNode(new DependencyNodeImpl(function, new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "1")), outputs, inputs)));
    assertFalse(filter.acceptNode(new DependencyNodeImpl(function, new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "2")), outputs, inputs)));
    assertTrue(filter.acceptNode(new DependencyNodeImpl(function, new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "2", "V")), outputs, inputs)));
    assertTrue(filter.acceptNode(new DependencyNodeImpl(function, new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "3")), outputs, inputs)));
    assertFalse(filter.acceptNode(new DependencyNodeImpl(function, new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "3", "X")), outputs, inputs)));
    assertTrue(filter.acceptNode(new DependencyNodeImpl(function, new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "3", "Y")), outputs, inputs)));
    assertTrue(filter.acceptNode(new DependencyNodeImpl(function, ComputationTargetSpecification.NULL, outputs, inputs)));
  }

}

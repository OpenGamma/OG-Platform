/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DependencyNodeImpl} class.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyNodeImplTest {

  public void testBasicOperation() {
    final DependencyNodeFunction function = DependencyNodeFunctionImpl.of("Test", EmptyFunctionParameters.INSTANCE);
    final ComputationTargetSpecification target = ComputationTargetSpecification.NULL;
    final ValueSpecification output = new ValueSpecification("Foo", target, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    final Collection<ValueSpecification> outputs = Collections.singleton(output);
    Map<ValueSpecification, DependencyNode> inputs = Collections.emptyMap();
    final DependencyNode node1 = new DependencyNodeImpl(function, target, outputs, inputs);
    assertEquals(node1.getFunction(), function);
    assertEquals(node1.getTarget(), target);
    assertEquals(node1.getInputCount(), 0);
    assertEquals(node1.getOutputCount(), 1);
    assertEquals(node1.getOutputValue(0), output);
    assertEquals(DependencyNodeImpl.getInputValues(node1), inputs.keySet());
    assertEquals(DependencyNodeImpl.getInputs(node1), inputs);
    assertEquals(DependencyNodeImpl.getInputValueArray(node1).length, 0);
    assertEquals(DependencyNodeImpl.getOutputValues(node1), ImmutableSet.copyOf(outputs));
    assertEquals(DependencyNodeImpl.getOutputValueArray(node1)[0], output);
    inputs = ImmutableMap.of(output, node1);
    final DependencyNode node2 = new DependencyNodeImpl(function, target, Collections.<ValueSpecification>emptySet(), inputs);
    assertEquals(node2.getInputCount(), 1);
    assertEquals(node2.getInputValue(0), output);
    assertSame(node2.getInputNode(0), node1);
  }

  // TODO: Test the various update operations

}

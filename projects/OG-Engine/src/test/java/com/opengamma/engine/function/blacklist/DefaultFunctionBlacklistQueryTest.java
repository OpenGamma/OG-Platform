/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultFunctionBlacklistQuery} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultFunctionBlacklistQueryTest {

  private final ParameterizedFunction _function1;
  private final ParameterizedFunction _function2;
  private final ParameterizedFunction _function3;
  private final ParameterizedFunction _function4;
  private final ComputationTargetSpecification _target1;
  private final ComputationTargetSpecification _target2;
  private final Set<ValueSpecification> _inputs1;
  private final Set<ValueSpecification> _inputs2;
  private final Set<ValueSpecification> _outputs1;
  private final Set<ValueSpecification> _outputs2;

  public DefaultFunctionBlacklistQueryTest() {
    _function1 = new ParameterizedFunction(new MockFunction("F1", null), new EmptyFunctionParameters());
    _function2 = new ParameterizedFunction(new MockFunction("F2", null), new EmptyFunctionParameters());
    _function3 = new ParameterizedFunction(new MockFunction("F1", null), new SimpleFunctionParameters());
    _function4 = new ParameterizedFunction(new MockFunction("F2", null), new SimpleFunctionParameters());
    _target1 = ComputationTargetSpecification.of(UniqueId.of("Test", "Foo"));
    _target2 = ComputationTargetSpecification.of(UniqueId.of("Test", "Bar"));
    _inputs1 = Collections.singleton(new ValueSpecification("Foo", _target1, ValueProperties.with(ValuePropertyNames.FUNCTION, "X").get()));
    _inputs2 = Collections.singleton(new ValueSpecification("Bar", _target1, ValueProperties.with(ValuePropertyNames.FUNCTION, "X").get()));
    _outputs1 = Collections.singleton(new ValueSpecification("Foo", _target2, ValueProperties.with(ValuePropertyNames.FUNCTION, "Y").get()));
    _outputs2 = Collections.singleton(new ValueSpecification("Bar", _target2, ValueProperties.with(ValuePropertyNames.FUNCTION, "Y").get()));
  }

  public void testEmpty() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    assertTrue(bl.isEmpty());
    assertFalse(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_function1, _target1));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
  }

  public void testSingleEntry_wildcard() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRule(new FunctionBlacklistRule());
    assertFalse(bl.isEmpty());
    assertTrue(bl.isBlacklisted(_function1));
    assertTrue(bl.isBlacklisted(_target1));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
  }

  public void testSingleEntry_function() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRule(new FunctionBlacklistRule(_function1));
    assertFalse(bl.isEmpty());
    assertTrue(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertFalse(bl.isBlacklisted(_function3));
    assertFalse(bl.isBlacklisted(_function4));
    assertFalse(bl.isBlacklisted(_target1));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target1));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
  }

  public void testSingleEntry_functionIdentifier() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    rule.setFunctionIdentifier("F1");
    bl.addRule(rule);
    assertFalse(bl.isEmpty());
    assertTrue(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertTrue(bl.isBlacklisted(_function3));
    assertFalse(bl.isBlacklisted(_function4));
    assertFalse(bl.isBlacklisted(_target1));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target1));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function3, _target1));
    assertTrue(bl.isBlacklisted(_function3, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function4, _target1));
    assertFalse(bl.isBlacklisted(_function4, _target1, _inputs1, _outputs1));
  }

  public void testSingleEntry_functionParameters() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    rule.setFunctionParameters(new EmptyFunctionParameters());
    bl.addRule(rule);
    assertFalse(bl.isEmpty());
    assertTrue(bl.isBlacklisted(_function1));
    assertTrue(bl.isBlacklisted(_function2));
    assertFalse(bl.isBlacklisted(_function3));
    assertFalse(bl.isBlacklisted(_function4));
    assertFalse(bl.isBlacklisted(_target1));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function2, _target1));
    assertTrue(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function3, _target1));
    assertFalse(bl.isBlacklisted(_function3, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function4, _target1));
    assertFalse(bl.isBlacklisted(_function4, _target1, _inputs1, _outputs1));
  }

  public void testSingleEntry_target() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRule(new FunctionBlacklistRule(_target1));
    assertFalse(bl.isEmpty());
    assertFalse(bl.isBlacklisted(_function1));
    assertTrue(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_target2));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertFalse(bl.isBlacklisted(_function1, _target2));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
  }

  public void testSingleEntry_functionTarget() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRule(new FunctionBlacklistRule(_function1, _target1));
    assertFalse(bl.isEmpty());
    assertFalse(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertFalse(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_target2));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertFalse(bl.isBlacklisted(_function1, _target2));
    assertFalse(bl.isBlacklisted(_function2, _target1));
    assertFalse(bl.isBlacklisted(_function2, _target2));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target2, _inputs1, _outputs1));
  }

  public void testSingleEntry_inputs() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    rule.setInputs(_inputs1);
    bl.addRule(rule);
    assertFalse(bl.isEmpty());
    assertFalse(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs2, _outputs1));
  }

  public void testSingleEntry_outputs() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    rule.setOutputs(_outputs1);
    bl.addRule(rule);
    assertFalse(bl.isEmpty());
    assertFalse(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs2));
  }

  public void testSingleEntry_full() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRule(new FunctionBlacklistRule(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isEmpty());
    assertFalse(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs2, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs2));
  }

  public void testMultipleEntries() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRules(Arrays.asList(
        new FunctionBlacklistRule(_function1), new FunctionBlacklistRule(_target1),
        new FunctionBlacklistRule(_function2, _target2), new FunctionBlacklistRule(_function3, _target2, _inputs1, _outputs1)));
    assertFalse(bl.isEmpty());
    assertTrue(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertTrue(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_target2));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target2));
    assertTrue(bl.isBlacklisted(_function2, _target1));
    assertTrue(bl.isBlacklisted(_function2, _target2));
    assertFalse(bl.isBlacklisted(_function3, _target2));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1, _target2, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function2, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function2, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function2, _target2, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function3, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function3, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function3, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function3, _target2, _inputs2, _outputs2));
  }

  public void testRemoveRule() {
    final DefaultFunctionBlacklistQuery bl = new DefaultFunctionBlacklistQuery(new EmptyFunctionBlacklist());
    bl.addRule(new FunctionBlacklistRule(_function1));
    bl.addRule(new FunctionBlacklistRule(_target1));
    bl.addRule(new FunctionBlacklistRule(_function2, _target2));
    bl.addRule(new FunctionBlacklistRule(_function3, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertTrue(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_target2));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target2));
    assertTrue(bl.isBlacklisted(_function2, _target1));
    assertTrue(bl.isBlacklisted(_function2, _target2));
    assertFalse(bl.isBlacklisted(_function3, _target2));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1, _target2, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function2, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function2, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function2, _target2, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function3, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function3, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function3, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function3, _target2, _inputs2, _outputs2));
    bl.removeRule(new FunctionBlacklistRule(_target1));
    bl.removeRule(new FunctionBlacklistRule(_function3, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertFalse(bl.isBlacklisted(_target1)); // changed
    assertFalse(bl.isBlacklisted(_target2));
    assertTrue(bl.isBlacklisted(_function1, _target1));
    assertTrue(bl.isBlacklisted(_function1, _target2));
    assertFalse(bl.isBlacklisted(_function2, _target1)); // changed
    assertTrue(bl.isBlacklisted(_function2, _target2));
    assertFalse(bl.isBlacklisted(_function3, _target2));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1, _target1, _inputs2, _outputs2));
    assertTrue(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function1, _target2, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1)); // changed
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs2, _outputs2)); // changed
    assertTrue(bl.isBlacklisted(_function2, _target2, _inputs1, _outputs1));
    assertTrue(bl.isBlacklisted(_function2, _target2, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function3, _target1, _inputs1, _outputs1)); // changed
    assertFalse(bl.isBlacklisted(_function3, _target1, _inputs2, _outputs2)); // changed
    assertFalse(bl.isBlacklisted(_function3, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function3, _target2, _inputs2, _outputs2));
    bl.removeRules(Arrays.asList(new FunctionBlacklistRule(_function1), new FunctionBlacklistRule(_function2, _target2)));
    // No rules left
    assertFalse(bl.isBlacklisted(_function1));
    assertFalse(bl.isBlacklisted(_function2));
    assertFalse(bl.isBlacklisted(_target1));
    assertFalse(bl.isBlacklisted(_target2));
    assertFalse(bl.isBlacklisted(_function1, _target1));
    assertFalse(bl.isBlacklisted(_function1, _target2));
    assertFalse(bl.isBlacklisted(_function2, _target1));
    assertFalse(bl.isBlacklisted(_function2, _target2));
    assertFalse(bl.isBlacklisted(_function3, _target2));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target1, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function1, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function1, _target2, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target1, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function2, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function2, _target2, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function3, _target1, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function3, _target1, _inputs2, _outputs2));
    assertFalse(bl.isBlacklisted(_function3, _target2, _inputs1, _outputs1));
    assertFalse(bl.isBlacklisted(_function3, _target2, _inputs2, _outputs2));
    assertTrue(bl.isEmpty());
  }

}

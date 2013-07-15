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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultManageableFunctionBlacklist} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultManageableFunctionBlacklistTest {

  private final ParameterizedFunction _function;
  private final ComputationTargetSpecification _target;
  private final Set<ValueSpecification> _inputs;
  private final Set<ValueSpecification> _outputs;

  public DefaultManageableFunctionBlacklistTest() {
    _function = new ParameterizedFunction(new MockFunction("F1", null), new EmptyFunctionParameters());
    _target = ComputationTargetSpecification.of(UniqueId.of("Test", "Foo"));
    _inputs = Collections.singleton(new ValueSpecification("Foo", _target, ValueProperties.with(ValuePropertyNames.FUNCTION, "X").get()));
    _outputs = Collections.singleton(new ValueSpecification("Bar", _target, ValueProperties.with(ValuePropertyNames.FUNCTION, "Y").get()));
  }

  public void testEmpty() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final DefaultManageableFunctionBlacklist bl = new DefaultManageableFunctionBlacklist("TEST", executor);
      final DefaultFunctionBlacklistQuery qry = new DefaultFunctionBlacklistQuery(bl);
      assertFalse(qry.isBlacklisted(_function));
      assertFalse(qry.isBlacklisted(_target));
      assertFalse(qry.isBlacklisted(_function, _target));
      assertFalse(qry.isBlacklisted(_function, _target, _inputs, _outputs));
    } finally {
      executor.shutdown();
    }
  }

  public void testAddAndRemoveRule() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final DefaultManageableFunctionBlacklist bl = new DefaultManageableFunctionBlacklist("TEST", executor);
      final DefaultFunctionBlacklistQuery qry = new DefaultFunctionBlacklistQuery(bl);
      bl.addBlacklistRule(new FunctionBlacklistRule(_function));
      assertTrue(qry.isBlacklisted(_function));
      assertFalse(qry.isBlacklisted(_target));
      assertTrue(qry.isBlacklisted(_function, _target));
      assertTrue(qry.isBlacklisted(_function, _target, _inputs, _outputs));
      bl.removeBlacklistRule(new FunctionBlacklistRule(_function));
      assertFalse(qry.isBlacklisted(_function));
      assertFalse(qry.isBlacklisted(_target));
      assertFalse(qry.isBlacklisted(_function, _target));
      assertFalse(qry.isBlacklisted(_function, _target, _inputs, _outputs));
    } finally {
      executor.shutdown();
    }
  }

  public void testAndAndRemoveMultipleRule() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final DefaultManageableFunctionBlacklist bl = new DefaultManageableFunctionBlacklist("TEST", executor);
      final DefaultFunctionBlacklistQuery qry = new DefaultFunctionBlacklistQuery(bl);
      bl.addBlacklistRules(Arrays.asList(new FunctionBlacklistRule(_function), new FunctionBlacklistRule(_target)));
      assertTrue(qry.isBlacklisted(_function));
      assertTrue(qry.isBlacklisted(_target));
      assertTrue(qry.isBlacklisted(_function, _target));
      assertTrue(qry.isBlacklisted(_function, _target, _inputs, _outputs));
      bl.removeBlacklistRules(Arrays.asList(new FunctionBlacklistRule(_function), new FunctionBlacklistRule(_target)));
      assertFalse(qry.isBlacklisted(_function));
      assertFalse(qry.isBlacklisted(_target));
      assertFalse(qry.isBlacklisted(_function, _target));
      assertFalse(qry.isBlacklisted(_function, _target, _inputs, _outputs));
    } finally {
      executor.shutdown();
    }
  }

}

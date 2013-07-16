/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link DefaultManageableFunctionBlacklist} class.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class DefaultManageableFunctionBlacklistExpiryTest {
  // broken out from DefaultManageableFunctionBlacklistTest as this is slower

  private final ParameterizedFunction _function;
  private final ComputationTargetSpecification _target;
  private final Set<ValueSpecification> _inputs;
  private final Set<ValueSpecification> _outputs;

  public DefaultManageableFunctionBlacklistExpiryTest() {
    _function = new ParameterizedFunction(new MockFunction("F1", null), new EmptyFunctionParameters());
    _target = ComputationTargetSpecification.of(UniqueId.of("Test", "Foo"));
    _inputs = Collections.singleton(new ValueSpecification("Foo", _target, ValueProperties.with(ValuePropertyNames.FUNCTION, "X").get()));
    _outputs = Collections.singleton(new ValueSpecification("Bar", _target, ValueProperties.with(ValuePropertyNames.FUNCTION, "Y").get()));
  }

  @Test(invocationCount = 5, successPercentage = 19)
  public void testExpiry() throws Exception {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final int timeout = (int) Timeout.standardTimeoutSeconds();
      final DefaultManageableFunctionBlacklist bl = new DefaultManageableFunctionBlacklist("TEST", executor, timeout);
      bl.addBlacklistRule(new FunctionBlacklistRule(_function));
      final DefaultFunctionBlacklistQuery qry = new DefaultFunctionBlacklistQuery(bl);
      assertTrue(qry.isBlacklisted(_function));
      assertFalse(qry.isBlacklisted(_target));
      assertTrue(qry.isBlacklisted(_function, _target));
      assertTrue(qry.isBlacklisted(_function, _target, _inputs, _outputs));
      Thread.sleep(timeout * 2000);
      assertFalse(qry.isBlacklisted(_function));
      assertFalse(qry.isBlacklisted(_target));
      assertFalse(qry.isBlacklisted(_function, _target));
      assertFalse(qry.isBlacklisted(_function, _target, _inputs, _outputs));
    } finally {
      executor.shutdown();
    }
  }

}

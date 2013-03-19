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

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MultipleFunctionBlacklistQuery} class.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleFunctionBlacklistQueryTest {

  private final String _functionIdentifier = "Func";
  private final ComputationTarget _target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "Test"));
  private final CompiledFunctionDefinition _function = new MockFunction(_functionIdentifier, _target);
  private final FunctionParameters _functionParameters = new EmptyFunctionParameters();
  private final ParameterizedFunction _parameterizedFunction = new ParameterizedFunction(_function, _functionParameters);
  private final ComputationTargetSpecification _targetSpecification = _target.toSpecification();
  private final Set<ValueSpecification> _inputs = Collections.<ValueSpecification>emptySet();
  private final Set<ValueSpecification> _outputs = Collections.<ValueSpecification>emptySet();
  private final CalculationJobItem _jobItem = new CalculationJobItem(
      _functionIdentifier, _functionParameters, _targetSpecification, _inputs, _outputs, ExecutionLogMode.INDICATORS);

  public void testNone() {
    final MultipleFunctionBlacklistQuery q = new MultipleFunctionBlacklistQuery(Collections.<FunctionBlacklistQuery>emptySet());
    assertFalse(q.isBlacklisted(_jobItem));
    assertFalse(q.isBlacklisted(_targetSpecification));
    assertFalse(q.isBlacklisted(_parameterizedFunction));
    assertFalse(q.isBlacklisted(_parameterizedFunction, _targetSpecification));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters, _targetSpecification));
    assertFalse(q.isBlacklisted(_parameterizedFunction, _targetSpecification, _inputs, _outputs));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters, _targetSpecification, _inputs, _outputs));
  }

  public void testOne() {
    final FunctionBlacklistQuery u = Mockito.mock(FunctionBlacklistQuery.class);
    final MultipleFunctionBlacklistQuery q = new MultipleFunctionBlacklistQuery(Collections.singleton(u));
    Mockito.when(u.isBlacklisted(_jobItem)).thenReturn(Boolean.TRUE);
    Mockito.when(u.isBlacklisted(_parameterizedFunction)).thenReturn(Boolean.TRUE);
    Mockito.when(u.isBlacklisted(_functionIdentifier, _functionParameters)).thenReturn(Boolean.TRUE);
    Mockito.when(u.isBlacklisted(_parameterizedFunction, _targetSpecification, _inputs, _outputs)).thenReturn(Boolean.TRUE);
    assertTrue(q.isBlacklisted(_jobItem));
    assertFalse(q.isBlacklisted(_targetSpecification));
    assertTrue(q.isBlacklisted(_parameterizedFunction));
    assertFalse(q.isBlacklisted(_parameterizedFunction, _targetSpecification));
    assertTrue(q.isBlacklisted(_functionIdentifier, _functionParameters));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters, _targetSpecification));
    assertTrue(q.isBlacklisted(_parameterizedFunction, _targetSpecification, _inputs, _outputs));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters, _targetSpecification, _inputs, _outputs));
  }

  public void testTwo() {
    final FunctionBlacklistQuery u1 = Mockito.mock(FunctionBlacklistQuery.class);
    final FunctionBlacklistQuery u2 = Mockito.mock(FunctionBlacklistQuery.class);
    final MultipleFunctionBlacklistQuery q = new MultipleFunctionBlacklistQuery(Arrays.asList(u1, u2));
    Mockito.when(u1.isBlacklisted(_jobItem)).thenReturn(Boolean.TRUE);
    Mockito.when(u2.isBlacklisted(_targetSpecification)).thenReturn(Boolean.TRUE);
    Mockito.when(u1.isBlacklisted(_parameterizedFunction)).thenReturn(Boolean.TRUE);
    Mockito.when(u2.isBlacklisted(_parameterizedFunction, _targetSpecification)).thenReturn(Boolean.TRUE);
    Mockito.when(u1.isBlacklisted(_functionIdentifier, _functionParameters)).thenReturn(Boolean.TRUE);
    Mockito.when(u1.isBlacklisted(_parameterizedFunction, _targetSpecification, _inputs, _outputs)).thenReturn(Boolean.TRUE);
    assertTrue(q.isBlacklisted(_jobItem));
    assertTrue(q.isBlacklisted(_targetSpecification));
    assertTrue(q.isBlacklisted(_parameterizedFunction));
    assertTrue(q.isBlacklisted(_parameterizedFunction, _targetSpecification));
    assertTrue(q.isBlacklisted(_functionIdentifier, _functionParameters));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters, _targetSpecification));
    assertTrue(q.isBlacklisted(_parameterizedFunction, _targetSpecification, _inputs, _outputs));
    assertFalse(q.isBlacklisted(_functionIdentifier, _functionParameters, _targetSpecification, _inputs, _outputs));
  }

}

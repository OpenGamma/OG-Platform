/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Possible solution to the resolution of a value requirement.
 */
/* package */final class ResolvedValue {

  private final ValueSpecification _valueSpecification;
  private final ParameterizedFunction _function;
  private final ComputationTarget _target;
  private final Set<ValueSpecification> _functionInputs;

  public ResolvedValue(final ValueSpecification valueSpecification, final ParameterizedFunction function, final ComputationTarget target, final Set<ValueSpecification> functionInputs) {
    assert valueSpecification != null;
    assert function != null;
    assert target != null;
    assert functionInputs != null;
    _valueSpecification = valueSpecification;
    _function = function;
    _target = target;
    _functionInputs = functionInputs;
  }

  public ValueSpecification getValueSpecification() {
    return _valueSpecification;
  }

  public ParameterizedFunction getFunction() {
    return _function;
  }

  public ComputationTarget getComputationTarget() {
    return _target;
  }

  public Set<ValueSpecification> getFunctionInputs() {
    return _functionInputs;
  }

  @Override
  public String toString() {
    return _valueSpecification + " from " + _function + "(" + _functionInputs + ")";
  }

}

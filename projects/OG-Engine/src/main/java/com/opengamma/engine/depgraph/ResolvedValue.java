/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Possible solution to the resolution of a value requirement.
 */
/* package */final class ResolvedValue {

  private final ValueSpecification _valueSpecification;
  private final DependencyNodeFunction _function;
  private final Set<ValueSpecification> _functionInputs;
  private final Set<ValueSpecification> _functionOutputs;

  /**
   * Creates a new instance.
   * <p>
   * The {@code valueSpecification} specification must be a normalized/canonical form.
   * 
   * @param valueSpecification the resolved value specification, as it will appear in the dependency graph, not null
   * @param function the function identifier and parameters, not null
   * @param functionInputs the resolved input specifications, as they will appear in the dependency graph, not null
   * @param functionOutputs the resolved output specifications, as they will appear in the dependency graph, not null
   */
  public ResolvedValue(final ValueSpecification valueSpecification, final DependencyNodeFunction function, final Set<ValueSpecification> functionInputs,
      final Set<ValueSpecification> functionOutputs) {
    assert valueSpecification != null;
    assert function != null;
    assert functionInputs != null;
    assert functionOutputs != null;
    assert functionOutputs.contains(valueSpecification);
    assert !functionInputs.contains(valueSpecification);
    _valueSpecification = valueSpecification;
    _function = function;
    _functionInputs = functionInputs;
    _functionOutputs = functionOutputs;
  }

  public ValueSpecification getValueSpecification() {
    return _valueSpecification;
  }

  public DependencyNodeFunction getFunction() {
    return _function;
  }

  public Set<ValueSpecification> getFunctionInputs() {
    return _functionInputs;
  }

  public Set<ValueSpecification> getFunctionOutputs() {
    return _functionOutputs;
  }

  @Override
  public String toString() {
    return _valueSpecification + " from " + _function + "(" + _functionInputs + ")";
  }

}

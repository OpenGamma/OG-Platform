/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * An empty set of function inputs.
 */
public final class EmptyFunctionInputs implements FunctionInputs {

  public EmptyFunctionInputs() {
  }

  @Override
  public Collection<ComputedValue> getAllValues() {
    return Collections.emptySet();
  }

  @Override
  public Object getValue(final ValueRequirement requirement) {
    return null;
  }

  @Override
  public ComputedValue getComputedValue(final ValueRequirement requirement) {
    return null;
  }

  @Override
  public Object getValue(final String requirementName) {
    return null;
  }

  @Override
  public ComputedValue getComputedValue(final String requirementName) {
    return null;
  }

  @Override
  public Collection<ValueSpecification> getMissingValues() {
    return Collections.emptySet();
  }

}

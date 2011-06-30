/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public abstract class AbstractDummyLabelledMatrix2DFunction extends AbstractFunction.NonCompiledInvoker {

  private final ComputationTargetType _targetType;
  
  public AbstractDummyLabelledMatrix2DFunction(ComputationTargetType targetType) {
    _targetType = targetType;
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Random r = new Random();
    double[][] values = new double[4][5];
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 5; j++) {
        values[i][j] = r.nextDouble() * 10;
      }
    }
    Double[] xKeys = new Double[] {1d, 2d, 3d, 4d, 5d};
    String[] xLabels = getLabels("x", 5);
    Double[] yKeys = new Double[] {1d, 2d, 3d, 4d};
    String[] yLabels = getLabels("y", 4);
    DoubleLabelledMatrix2D matrix = new DoubleLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, values);
    ComputedValue computedValue = new ComputedValue(getValueSpecification(target), matrix);
    return Collections.singleton(computedValue);
  }
  
  private String[] getLabels(String prefix, int count) {
    String[] result = new String[count];
    for (int i = 0; i < count; i++) {
      result[i] = prefix + Integer.toString(i);
    }
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == _targetType;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification(target));
  }

  private ValueSpecification getValueSpecification(ComputationTarget target) {
    return new ValueSpecification("RandomMatrix", target.toSpecification(), createValueProperties().get());
  }

}

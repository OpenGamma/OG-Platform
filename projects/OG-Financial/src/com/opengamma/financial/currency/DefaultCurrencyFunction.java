/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * If no currency is explicitly requested, inject the view's default currency. This function should never
 * be added to a dependency graph as the input will always match the output.
 */
public class DefaultCurrencyFunction extends AbstractFunction.NonCompiledInvoker {

  private final ComputationTargetType _targetType;
  private final Set<String> _valueNames;

  public DefaultCurrencyFunction(final ComputationTargetType targetType, final String valueName) {
    _targetType = targetType;
    _valueNames = Collections.singleton(valueName);
  }

  public DefaultCurrencyFunction(final ComputationTargetType targetType, final String... valueNames) {
    _targetType = targetType;
    _valueNames = new HashSet<String>(Arrays.asList(valueNames));
  }

  protected Set<String> getValueNames() {
    return _valueNames;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    throw new IllegalStateException("This function should never be executed");
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return getTargetType() == target.getType();
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints().copy().with(ValuePropertyNames.CURRENCY, DefaultCurrencyInjectionFunction.getViewDefaultCurrencyISO(context)).get();
    final ValueRequirement required = new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), constraints);
    return Collections.singleton(required);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    // Maximal set of outputs is the valueNames with the infinite property set but no currency
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = ValueProperties.all().withoutAny(ValuePropertyNames.CURRENCY);
    if (getValueNames().size() == 1) {
      return Collections.singleton(new ValueSpecification(getValueNames().iterator().next(), targetSpec, properties));
    } else {
      final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
      for (String valueName : getValueNames()) {
        result.add(new ValueSpecification(valueName, targetSpec, properties));
      }
      return result;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // Pass the inputs through unchanged - will cause suppression of this node from the graph
    return inputs.keySet();
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Abstract function for injecting a default property into the dependency graph.
 */
public abstract class DefaultPropertyFunction extends AbstractFunction.NonCompiledInvoker {

  private final ComputationTargetType _targetType;
  private final String _propertyName;
  private final Set<String> _valueNames;

  protected DefaultPropertyFunction(final ComputationTargetType targetType, final String propertyName, final String valueName) {
    _targetType = targetType;
    _propertyName = propertyName;
    _valueNames = Collections.singleton(valueName);
  }

  protected DefaultPropertyFunction(final ComputationTargetType targetType, final String propertyName, final String... valueNames) {
    _targetType = targetType;
    _propertyName = propertyName;
    _valueNames = new HashSet<String>(Arrays.asList(valueNames));
  }

  protected Set<String> getValueNames() {
    return _valueNames;
  }

  public boolean hasValueName(final String valueName) {
    return getValueNames().contains(valueName);
  }

  protected String getPropertyName() {
    return _propertyName;
  }

  /**
   * Returns the default value(s) to set for the property. If a default value is
   * not available, must return null.
   * 
   * @param context the function compilation context, not null
   * @param target the computation target, not null
   * @param desiredValue the initial requirement, lacking the property to be injected, will
   *        be null when called from {@link #canApplyTo}
   * @return the default values or null if there is no default to inject
   */
  protected abstract Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue);

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    throw new IllegalStateException("This function should never be executed");
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    final Set<String> defaults = getDefaultValue(context, target, null);
    return defaults != null;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Set<String> defaults = getDefaultValue(context, target, desiredValue);
    if (defaults == null) {
      // If canApplyTo is overloaded, we can't assert that gDV returns non-null so check
      return null;
    }
    final ValueProperties.Builder constraints = desiredValue.getConstraints().copy();
    if (defaults.isEmpty()) {
      constraints.withAny(getPropertyName());
    } else {
      constraints.with(getPropertyName(), defaults);
    }
    return Collections.singleton(new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), constraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    // Maximal set of outputs is the valueNames with the infinite property minus the injected property
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = ValueProperties.all().withoutAny(getPropertyName());
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

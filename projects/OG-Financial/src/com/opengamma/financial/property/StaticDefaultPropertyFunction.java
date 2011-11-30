/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Abstract function for injecting default properties into the dependency graph. A single
 * property and one or more value names are defined at construction to simplify evaluation
 * of default values.
 */
public abstract class StaticDefaultPropertyFunction extends DefaultPropertyFunction {

  private final String _propertyName;
  private final Set<String> _valueNames;

  protected StaticDefaultPropertyFunction(final ComputationTargetType targetType, final String propertyName, final boolean permitWithout, final String valueName) {
    super(targetType, permitWithout);
    _propertyName = propertyName;
    _valueNames = Collections.singleton(valueName);
  }

  protected StaticDefaultPropertyFunction(final ComputationTargetType targetType, final String propertyName, final boolean permitWithout, final String... valueNames) {
    super(targetType, permitWithout);
    _propertyName = propertyName;
    _valueNames = new HashSet<String>(Arrays.asList(valueNames));
  }

  protected Set<String> getValueNames() {
    return _valueNames;
  }

  protected String getPropertyName() {
    return _propertyName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (String valueName : getValueNames()) {
      defaults.addValuePropertyName(valueName, getPropertyName());
    }
  }

  /**
   * Returns the default value(s) to set for the property. If a default value is
   * not available, must return null.
   * 
   * @param context the function compilation context, not null
   * @param target the computation target, not null
   * @param desiredValue the initial requirement, not null
   * @return the default values or null if there is no default to inject
   */
  protected abstract Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue);

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    assert getPropertyName().equals(propertyName);
    return getDefaultValue(context, target, desiredValue);
  }

}

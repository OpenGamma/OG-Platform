/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class YieldCurveDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.YIELD_CURVE,
    ValueRequirementNames.YIELD_CURVE_JACOBIAN,
  };
  private final String _absoluteTolerance;
  private final String _relativeTolerance;
  private final String _maxIterations;
  private final String _decomposition;
  private final String _useFiniteDifference;
  private final String[] _applicableCurrencies;

  public YieldCurveDefaults(final String absoluteTolerance, final String relativeTolerance, final String maxIterations, final String decomposition,
      final String useFiniteDifference, final String... applicableCurrencies) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(absoluteTolerance, "absolute tolerance");
    ArgumentChecker.notNull(relativeTolerance, "relative tolerance");
    ArgumentChecker.notNull(maxIterations, "max iterations");
    ArgumentChecker.notNull(decomposition, "decomposition");
    ArgumentChecker.notNull(useFiniteDifference, "use finite difference");
    ArgumentChecker.notNull(applicableCurrencies, "applicable currencies");
    _absoluteTolerance = absoluteTolerance;
    _relativeTolerance = relativeTolerance;
    _maxIterations = maxIterations;
    _decomposition = decomposition;
    _useFiniteDifference = useFiniteDifference;
    _applicableCurrencies = applicableCurrencies;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getUniqueId() == null) {
      return false;
    }
    for (final String applicableCurrencyName : _applicableCurrencies) {
      if (applicableCurrencyName.equals(target.getUniqueId().getValue())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
      defaults.addValuePropertyName(valueRequirement, MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
      defaults.addValuePropertyName(valueRequirement, MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
      defaults.addValuePropertyName(valueRequirement, MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION);
      defaults.addValuePropertyName(valueRequirement, MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION.equals(propertyName)) {
      return Collections.singleton(_decomposition);
    }
    if (MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE.equals(propertyName)) {
      return Collections.singleton(_absoluteTolerance);
    }
    if (MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE.equals(propertyName)) {
      return Collections.singleton(_relativeTolerance);
    }
    if (MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS.equals(propertyName)) {
      return Collections.singleton(_maxIterations);
    }
    if (MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE.equals(propertyName)) {
      return Collections.singleton(_useFiniteDifference);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return PriorityClass.ABOVE_NORMAL;
  }
}

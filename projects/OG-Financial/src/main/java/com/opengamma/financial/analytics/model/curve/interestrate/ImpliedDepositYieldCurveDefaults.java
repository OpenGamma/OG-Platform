/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated This function sets defaults for deprecated yield curve calculation functions.
 */
@Deprecated
public class ImpliedDepositYieldCurveDefaults extends DefaultPropertyFunction {
  /** The value requirement names to which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.YIELD_CURVE,
    ValueRequirementNames.YIELD_CURVE_JACOBIAN,
    ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX,
    ValueRequirementNames.YIELD_CURVE_SERIES,
    ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES
  };
  /** The absolute tolerance */
  private final String _absoluteTolerance;
  /** The relative tolerance */
  private final String _relativeTolerance;
  /** The maximum number of iterations */
  private final String _maxIterations;
  /** The matrix decomposition method */
  private final String _decomposition;
  /** Whether to use finite difference or analytic derivatives */
  private final String _useFiniteDifference;
  /** The currencies for which these defaults apply */
  private final String[] _applicableCurrencies;

  /**
   * @param absoluteTolerance The absolute tolerance used in root-finding
   * @param relativeTolerance The relative tolerance use in root-finding
   * @param maxIterations The maximum number of iterations used in root-finding
   * @param decomposition The matrix decomposition method used in root-finding
   * @param useFiniteDifference True if calculations should use finite difference in root-finding, otherwise analytic derivatives are used
   * @param applicableCurrencies The currencies for which these defaults apply
   */
  public ImpliedDepositYieldCurveDefaults(final String absoluteTolerance, final String relativeTolerance, final String maxIterations, final String decomposition,
      final String useFiniteDifference, final String... applicableCurrencies) {
    super(ComputationTargetType.CURRENCY, true);
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
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationMethods = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethods == null || curveCalculationMethods.size() != 1) {
      return super.getRequirements(context, target, desiredValue);
    }
    final String curveCalculationMethod = Iterables.getOnlyElement(curveCalculationMethods);
    if (!curveCalculationMethod.equals(ImpliedDepositCurveFunction.IMPLIED_DEPOSIT)) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
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

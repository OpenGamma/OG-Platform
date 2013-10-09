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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated This function sets defaults for deprecated yield curve calculation functions.
 */
@Deprecated
public class FXImpliedYieldCurveDefaults extends DefaultPropertyFunction {
  /** The value requirement names for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.YIELD_CURVE,
    ValueRequirementNames.YIELD_CURVE_JACOBIAN,
    ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX,
    ValueRequirementNames.YIELD_CURVE_SERIES
  };
  /** The interpolator name */
  private final String _interpolatorName;
  /** The left extrapolator name */
  private final String _leftExtrapolatorName;
  /** The right extrapolator name */
  private final String _rightExtrapolatorName;
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
   * @param interpolatorName The interpolator name
   * @param leftExtrapolatorName The left extrapolator name
   * @param rightExtrapolatorName The right extrapolator name
   * @param useFiniteDifference True if calculations should use finite difference in root-finding, otherwise analytic derivatives are used
   * @param applicableCurrencies The currencies for which these defaults apply
   */
  public FXImpliedYieldCurveDefaults(final String absoluteTolerance, final String relativeTolerance, final String maxIterations, final String decomposition,
      final String useFiniteDifference, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName,
      final String... applicableCurrencies) {
    super(ComputationTargetType.CURRENCY, true);
    ArgumentChecker.notNull(absoluteTolerance, "absolute tolerance");
    ArgumentChecker.notNull(relativeTolerance, "relative tolerance");
    ArgumentChecker.notNull(maxIterations, "max iterations");
    ArgumentChecker.notNull(decomposition, "decomposition");
    ArgumentChecker.notNull(useFiniteDifference, "use finite difference");
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(applicableCurrencies, "applicable currencies");
    _absoluteTolerance = absoluteTolerance;
    _relativeTolerance = relativeTolerance;
    _maxIterations = maxIterations;
    _decomposition = decomposition;
    _useFiniteDifference = useFiniteDifference;
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
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
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.X_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationMethods = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD);
    if (curveCalculationMethods == null || curveCalculationMethods.size() != 1) {
      return super.getRequirements(context, target, desiredValue);
    }
    final String curveCalculationMethod = Iterables.getOnlyElement(curveCalculationMethods);
    if (!curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
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
    if (InterpolatedDataProperties.X_INTERPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_interpolatorName);
    }
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
    }
    return null;
  }
}

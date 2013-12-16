/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve;

import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_INTEREST;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.YTM;
import static com.opengamma.engine.value.ValueRequirementNames.Z_SPREAD;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Sets root-finding defaults for yield curves.
 */
public class MultiCurvePricingDefaults extends DefaultPropertyFunction {
  /** The value requirement names for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ACCRUED_INTEREST,
    CLEAN_PRICE,
    CONVEXITY,
    MACAULAY_DURATION,
    MODIFIED_DURATION,
    PRESENT_VALUE,
    YTM,
    Z_SPREAD
  };
  /** The absolute tolerance */
  private final Set<String> _absoluteTolerance;
  /** The relative tolerance */
  private final Set<String> _relativeTolerance;
  /** The maximum number of iterations */
  private final Set<String> _maxIterations;

  /**
   * @param absoluteTolerance The absolute tolerance for the root-finder
   * @param relativeTolerance The relative tolerance for the root-finder
   * @param maxIterations The maximum number of iterations
   */
  public MultiCurvePricingDefaults(final String absoluteTolerance, final String relativeTolerance, final String maxIterations) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(absoluteTolerance, "absoluteTolerance");
    ArgumentChecker.notNull(relativeTolerance, "relativeTolerance");
    ArgumentChecker.notNull(maxIterations, "maxIterations");
    _absoluteTolerance = Collections.singleton(absoluteTolerance);
    _relativeTolerance = Collections.singleton(relativeTolerance);
    _maxIterations = Collections.singleton(maxIterations);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
      defaults.addValuePropertyName(valueRequirement, PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
      defaults.addValuePropertyName(valueRequirement, PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    switch(propertyName) {
      case PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE:
        return _absoluteTolerance;
      case PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE:
        return _relativeTolerance;
      case PROPERTY_ROOT_FINDER_MAX_ITERATIONS:
        return _maxIterations;
      default:
        return null;
    }
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PRICE_INDEX_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
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
 * Sets up default properties for curve construction.
 */
public class CurveDefaults extends DefaultPropertyFunction {
  /** The value requirements for which these properties apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    YIELD_CURVE,
    PRICE_INDEX_CURVE,
    CURVE_BUNDLE,
    JACOBIAN_BUNDLE
  };
  /** The absolute tolerance for the root-finder */
  private final String _absoluteTolerance;
  /** The relative tolerance for the root-finder */
  private final String _relativeTolerance;
  /** The maximum number of iterations for the root-finder */
  private final String _maxIterations;

  /**
   * @param absoluteTolerance The absolute tolerance, not null
   * @param relativeTolerance The relative tolerance, not null
   * @param maxIterations The maximum number of iterations, not null
   */
  public CurveDefaults(final String absoluteTolerance, final String relativeTolerance, final String maxIterations) {
    super(ComputationTargetType.NULL, true);
    ArgumentChecker.notNull(absoluteTolerance, "absolute tolerance");
    ArgumentChecker.notNull(relativeTolerance, "relative tolerance");
    ArgumentChecker.notNull(maxIterations, "maximum iterations");
    _absoluteTolerance = absoluteTolerance;
    _relativeTolerance = relativeTolerance;
    _maxIterations = maxIterations;
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
    if (PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE.equals(propertyName)) {
      return Collections.singleton(_absoluteTolerance);
    }
    if (PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE.equals(propertyName)) {
      return Collections.singleton(_relativeTolerance);
    }
    if (PROPERTY_ROOT_FINDER_MAX_ITERATIONS.equals(propertyName)) {
      return Collections.singleton(_maxIterations);
    }
    return null;
  }
}

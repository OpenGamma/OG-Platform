/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Sets defaults for <b>all</b> curves that are calculated using the FX implied method.
 * @deprecated Use the version that takes information about how to calculate the curve
 * @see FXImpliedYieldCurveDefaults
 */
@Deprecated
public class FXImpliedYieldCurveDefaultsDeprecated extends DefaultPropertyFunction {
  /** The interpolator name */
  private final String _interpolatorName;
  /** The left extrapolator name */
  private final String _leftExtrapolatorName;
  /** The right extrapolator name */
  private final String _rightExtrapolatorName;

  /**
   * @param interpolatorName The interpolator name
   * @param leftExtrapolatorName The left extrapolator name
   * @param rightExtrapolatorName The right extrapolator name
   */
  public FXImpliedYieldCurveDefaultsDeprecated(final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    super(ComputationTargetType.CURRENCY, true);
    ArgumentChecker.notNull(interpolatorName, "interpolator name");
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.YIELD_CURVE, YieldCurveFunction.PROPERTY_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.YIELD_CURVE, YieldCurveFunction.PROPERTY_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.YIELD_CURVE, YieldCurveFunction.PROPERTY_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_interpolatorName);
    }
    if (YieldCurveFunction.PROPERTY_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (YieldCurveFunction.PROPERTY_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
    }
    return null;
  }
}

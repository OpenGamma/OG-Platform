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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Sets default values for the extrapolation methods for interpolated yield curves.
 */
public class InterpolatedYieldCurveDefaults extends DefaultPropertyFunction {
  /** The left extrapolator name */
  private final String _leftExtrapolatorName;
  /** The right extrapolator name */
  private final String _rightExtrapolatorName;
  /** The currencies for which these defaults apply */
  private final String[] _applicableCurrencyNames;

  /**
   * @param leftExtrapolatorName The left extrapolator name, not null
   * @param rightExtrapolatorName The right extrapolator name, not null
   * @param applicableCurrencyNames The applicable currency names, not null
   */
  public InterpolatedYieldCurveDefaults(final String leftExtrapolatorName, final String rightExtrapolatorName,
      final String... applicableCurrencyNames) {
    super(ComputationTargetType.CURRENCY, true);
    ArgumentChecker.notNull(leftExtrapolatorName, "left extrapolator name");
    ArgumentChecker.notNull(rightExtrapolatorName, "right extrapolator name");
    ArgumentChecker.notNull(applicableCurrencyNames, "applicable currency names");
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _applicableCurrencyNames = applicableCurrencyNames;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UniqueId uid = target.getUniqueId();
    if (uid == null) {
      return false;
    }
    if (target.getUniqueId() == null) {
      return false;
    }
    for (final String applicableCurrencyName : _applicableCurrencyNames) {
      if (applicableCurrencyName.equals(uid.getValue())) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.YIELD_CURVE, InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    defaults.addValuePropertyName(ValueRequirementNames.YIELD_CURVE, InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_leftExtrapolatorName);
    }
    if (InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME.equals(propertyName)) {
      return Collections.singleton(_rightExtrapolatorName);
    }
    return null;
  }
}

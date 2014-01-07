/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 * For {@link ValueRequirementNames#POSITION_DELTA} and {@link ValueRequirementNames#POSITION_GAMMA},
 * includes default value {@link ValuePropertyNames#SCALE} to existing defaults as specified in {@link InterestRateFutureOptionBlackDefaults}
 * @deprecated The functions for which these defaults apply are deprecated. See comment in {@link InterestRateFutureOptionBlackFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPositionDeltaGammaScaleDefaults extends InterestRateFutureOptionBlackDefaults {
  private final String _defaultScale;

  public InterestRateFutureOptionBlackPositionDeltaGammaScaleDefaults(final String... scaleCurrencyCurveConfigAndSurfaceNames) {
    super(Arrays.copyOfRange(scaleCurrencyCurveConfigAndSurfaceNames, 1, scaleCurrencyCurveConfigAndSurfaceNames.length));
    ArgumentChecker.isTrue((scaleCurrencyCurveConfigAndSurfaceNames.length - 1) % 3 == 0, 
        "Input array must begin with a double representing the scale factor to apply, " +
            "then follow with one curve config and surface name per currency");
    _defaultScale = scaleCurrencyCurveConfigAndSurfaceNames[0];
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    defaults.addValuePropertyName(ValueRequirementNames.POSITION_DELTA, ValuePropertyNames.SCALE);
    defaults.addValuePropertyName(ValueRequirementNames.POSITION_GAMMA, ValuePropertyNames.SCALE);
    defaults.addValuePropertyName(ValueRequirementNames.VALUE_DELTA, ValuePropertyNames.SCALE);
    defaults.addValuePropertyName(ValueRequirementNames.VALUE_GAMMA, ValuePropertyNames.SCALE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.SCALE.equals(propertyName)) {
      return Collections.singleton(_defaultScale);
    }
    return super.getDefaultValue(context, target, desiredValue, propertyName);
  }
}

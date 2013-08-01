/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackDefaults;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class InterestRateFutureOptionBlackThetaDefaults extends InterestRateFutureOptionBlackDefaults {
  private final String _defaultNumberOfDays;

  public InterestRateFutureOptionBlackThetaDefaults(final String... daysCurrencyCurveConfigAndSurfaceNames) {
    super(Arrays.copyOfRange(daysCurrencyCurveConfigAndSurfaceNames, 1, daysCurrencyCurveConfigAndSurfaceNames.length));
    ArgumentChecker.isTrue((daysCurrencyCurveConfigAndSurfaceNames.length - 1) % 3 == 0, 
        "Input array must begin with a number of days then follow with one curve config and surface name per currency");
    _defaultNumberOfDays = daysCurrencyCurveConfigAndSurfaceNames[0];
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    defaults.addValuePropertyName(ValueRequirementNames.VALUE_THETA, ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD);
    defaults.addValuePropertyName(ValueRequirementNames.POSITION_THETA, ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD.equals(propertyName)) {
      return Collections.singleton(_defaultNumberOfDays);
    }
    return super.getDefaultValue(context, target, desiredValue, propertyName);
  }
}

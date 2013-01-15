/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaults;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class InterestRateFutureThetaDefaults extends InterestRateFutureDefaults {
  private final String _defaultNumberOfDays;

  public InterestRateFutureThetaDefaults(final String defaultNumberOfDays, final String... currencyAndCurveConfigNames) {
    super(currencyAndCurveConfigNames);
    ArgumentChecker.notNull(defaultNumberOfDays, "default number of days");
    _defaultNumberOfDays = defaultNumberOfDays;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    defaults.addValuePropertyName(ValueRequirementNames.VALUE_THETA, ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD.equals(propertyName)) {
      return Collections.singleton(_defaultNumberOfDays);
    }
    return super.getDefaultValue(context, target, desiredValue, propertyName);
  }
}

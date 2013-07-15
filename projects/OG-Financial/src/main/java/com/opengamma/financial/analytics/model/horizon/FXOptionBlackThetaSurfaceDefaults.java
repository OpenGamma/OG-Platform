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
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackSurfaceDefaults;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class FXOptionBlackThetaSurfaceDefaults extends FXOptionBlackSurfaceDefaults {
  private final String _defaultNumberOfDays;

  public FXOptionBlackThetaSurfaceDefaults(final String defaultNumberOfDays, final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName, final String... surfaceNamesBuCurrencyPair) {
    super(interpolatorName, leftExtrapolatorName, rightExtrapolatorName, surfaceNamesBuCurrencyPair);
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

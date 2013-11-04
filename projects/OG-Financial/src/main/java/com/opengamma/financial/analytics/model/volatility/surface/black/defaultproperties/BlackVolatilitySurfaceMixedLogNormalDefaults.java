/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;

/**
 *
 */
public class BlackVolatilitySurfaceMixedLogNormalDefaults extends BlackVolatilitySurfaceDefaults {
  private final String _weightingFunction;

  public BlackVolatilitySurfaceMixedLogNormalDefaults(final String timeAxis, final String yAxis,
      final String volatilityTransform, final String timeInterpolator, final String timeLeftExtrapolator, final String timeRightExtrapolator, final String weightingFunction) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL);
    _weightingFunction = weightingFunction;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (final String valueRequirement : getValueRequirements()) {
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> commonDefaults = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (commonDefaults != null) {
      return commonDefaults;
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_MIXED_LOG_NORMAL_WEIGHTING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_weightingFunction);
    }
    return null;
  }
}

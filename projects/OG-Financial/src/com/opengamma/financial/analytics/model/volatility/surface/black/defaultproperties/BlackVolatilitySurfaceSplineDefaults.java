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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;


/**
 *
 */
public class BlackVolatilitySurfaceSplineDefaults extends BlackVolatilitySurfaceDefaults {
  private final String _yInterpolator;
  private final String _yLeftExtrapolator;
  private final String _yRightExtrapolator;

  public BlackVolatilitySurfaceSplineDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String forwardCurveName, final String forwardCurveCalculationMethod, final String surfaceName,
      final String yInterpolator, final String yLeftExtrapolator, final String yRightExtrapolator) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, forwardCurveName, forwardCurveCalculationMethod, surfaceName);
    ArgumentChecker.notNull(yInterpolator, "y interpolator");
    ArgumentChecker.notNull(yLeftExtrapolator, "y left extrapolator");
    ArgumentChecker.notNull(yRightExtrapolator, "y right extrapolator");
    _yInterpolator = yInterpolator;
    _yLeftExtrapolator = yLeftExtrapolator;
    _yRightExtrapolator = yRightExtrapolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> surfaceDefaults = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (surfaceDefaults != null) {
      return surfaceDefaults;
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_yInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_yLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_yRightExtrapolator);
    }
    return null;
  }

}

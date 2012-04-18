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
public class BlackVolatilitySurfaceSplineInterpolatorDefaults extends BlackVolatilitySurfaceInterpolatorDefaults {
  private final String _splineInterpolator;
  private final String _splineLeftExtrapolator;
  private final String _splineRightExtrapolator;

  public BlackVolatilitySurfaceSplineInterpolatorDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String splineInterpolator, final String splineLeftExtrapolator, final String splineRightExtrapolator) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator);
    ArgumentChecker.notNull(splineInterpolator, "spline interpolator");
    ArgumentChecker.notNull(splineLeftExtrapolator, "spline left extrapolator");
    ArgumentChecker.notNull(splineRightExtrapolator, "spline right extrapolator");
    _splineInterpolator = splineInterpolator;
    _splineLeftExtrapolator = splineLeftExtrapolator;
    _splineRightExtrapolator = splineRightExtrapolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> commonProperties = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (commonProperties != null) {
      return commonProperties;
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_splineInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_splineLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_splineRightExtrapolator);
    }
    return null;
  }
}

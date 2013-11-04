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
import com.opengamma.util.ArgumentChecker;

/**
 * Adds default properties, as supplied in FunctionConfiguration (eg DemoStandardFunctionConfiguration), to the BlackVolatilitySurfaceInterpolatorFunction
 * BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR
 * BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE
 */
public class BlackVolatilitySurfaceSplineDefaults extends BlackVolatilitySurfaceDefaults {
  private final String _splineInterpolator;
  private final String _splineLeftExtrapolator;
  private final String _splineRightExtrapolator;
  private final String _splineExtrapolatorFailBehaviour;

  public BlackVolatilitySurfaceSplineDefaults(final String timeAxis, final String yAxis, final String volatilityTransform,
      final String timeInterpolator, final String timeLeftExtrapolator, final String timeRightExtrapolator,
      final String splineInterpolator, final String splineLeftExtrapolator, final String splineRightExtrapolator, final String splineExtrapolatorFailBehaviour) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, BlackVolatilitySurfacePropertyNamesAndValues.SPLINE);
    ArgumentChecker.notNull(splineInterpolator, "spline interpolator");
    ArgumentChecker.notNull(splineLeftExtrapolator, "spline left extrapolator");
    ArgumentChecker.notNull(splineRightExtrapolator, "spline right extrapolator");
    ArgumentChecker.notNull(splineExtrapolatorFailBehaviour, "spline extrapolator failure behaviour not set");
    _splineInterpolator = splineInterpolator;
    _splineLeftExtrapolator = splineLeftExtrapolator;
    _splineRightExtrapolator = splineRightExtrapolator;
    _splineExtrapolatorFailBehaviour = splineExtrapolatorFailBehaviour;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (final String valueRequirement : getValueRequirements()) {
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE);
    }
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
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE.equals(propertyName)) {
      return Collections.singleton(_splineExtrapolatorFailBehaviour);
    }
    return null;
  }
}

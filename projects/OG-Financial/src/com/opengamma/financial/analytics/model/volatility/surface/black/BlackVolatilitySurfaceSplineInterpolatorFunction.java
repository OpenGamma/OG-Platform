/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR;
import static com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR;

import java.util.Set;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 */
public class BlackVolatilitySurfaceSplineInterpolatorFunction extends BlackVolatilitySurfaceInterpolatorFunction {

  @Override
  protected Set<ValueRequirement> getSpecificRequirements(final ValueProperties constraints) {
    return BlackVolatilitySurfaceUtils.ensureSplineVolatilityInterpolatorProperties(constraints);
  }

  @Override
  protected GeneralSmileInterpolator getSmileInterpolator(final ValueRequirement desiredValue) {
    final String interpolatorName = desiredValue.getConstraint(PROPERTY_SPLINE_INTERPOLATOR);
    final String leftExtrapolatorName = desiredValue.getConstraint(PROPERTY_SPLINE_LEFT_EXTRAPOLATOR);
    final String rightExtrapolatorName = desiredValue.getConstraint(PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR);
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    return new SmileInterpolatorSpline(interpolator);
  }

  @Override
  protected ValueProperties getResultProperties() {
    return BlackVolatilitySurfaceUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get()).get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return BlackVolatilitySurfaceUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get(), desiredValue).get();
  }
}

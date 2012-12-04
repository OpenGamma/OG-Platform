/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTargetType;

/**
 *
 */
public class BlackVolatilitySurfaceSplineSecurityDefaults extends BlackVolatilitySurfaceSplineDefaults {

  public BlackVolatilitySurfaceSplineSecurityDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String splineInterpolator, final String splineLeftExtrapolator, final String splineRightExtrapolator,
      final String splineExtrapolatorFailBehaviour) {
    super(ComputationTargetType.SECURITY, timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, splineInterpolator, splineLeftExtrapolator,
        splineRightExtrapolator, splineExtrapolatorFailBehaviour);
  }

}

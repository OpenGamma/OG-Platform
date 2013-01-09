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
public class BlackVolatilitySurfaceMixedLogNormalPrimitiveDefaults extends BlackVolatilitySurfaceMixedLogNormalDefaults {

  public BlackVolatilitySurfaceMixedLogNormalPrimitiveDefaults(final String timeAxis, final String yAxis, final String volatilityTransform,
      final String timeInterpolator, final String timeLeftExtrapolator, final String timeRightExtrapolator, final String weightingFunction) {
    super(ComputationTargetType.PRIMITIVE, timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, weightingFunction);
  }


}

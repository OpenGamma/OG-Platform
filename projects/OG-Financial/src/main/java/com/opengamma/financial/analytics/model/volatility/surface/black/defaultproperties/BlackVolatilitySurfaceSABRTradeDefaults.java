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
public class BlackVolatilitySurfaceSABRTradeDefaults extends BlackVolatilitySurfaceSABRDefaults {

  public BlackVolatilitySurfaceSABRTradeDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String sabrModel, final String weightingFunction, final String useExternalBeta, final String externalBeta) {
    super(ComputationTargetType.TRADE, timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, sabrModel, weightingFunction, useExternalBeta,
        externalBeta);
  }

}

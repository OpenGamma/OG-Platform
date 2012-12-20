/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import com.opengamma.engine.ComputationTargetType;

/**
 *
 */
public class SABRIRFutureOptionNLSSTradeDefaults extends SABRIRFutureOptionNLSSDefaults {

  public SABRIRFutureOptionNLSSTradeDefaults(final String priority, final String xInterpolatorName, final String yInterpolatorName, final String leftXExtrapolatorName,
      final String rightXExtrapolatorName, final String leftYExtrapolatorName, final String rightYExtrapolatorName, final String useFixedAlpha, final String useFixedBeta, final String useFixedRho,
      final String useFixedNu, final String alpha, final String beta, final String rho, final String nu, final String error) {
    super(priority, xInterpolatorName, yInterpolatorName, leftXExtrapolatorName, rightXExtrapolatorName, leftYExtrapolatorName, rightYExtrapolatorName, useFixedAlpha,
        useFixedBeta, useFixedRho, useFixedNu, alpha, beta, rho, nu, error, ComputationTargetType.TRADE);
  }


}

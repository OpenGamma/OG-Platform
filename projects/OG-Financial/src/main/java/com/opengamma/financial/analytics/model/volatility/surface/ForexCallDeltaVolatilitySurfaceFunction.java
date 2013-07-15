/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface;

import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;

/**
 * 
 */
public class ForexCallDeltaVolatilitySurfaceFunction extends ForexPutCallDeltaVolatilitySurfaceFunction {

  @Override
  protected String getVolatilitySurfaceQuoteType() {
    return SurfaceAndCubeQuoteType.CALL_DELTA;
  }

  @Override
  protected double getTransformedDelta(final double delta) {
    return delta / 100.;
  }
}

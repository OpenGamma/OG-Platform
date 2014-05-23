/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Implementation of a provider of Black smile for options on STIR futures. The volatility is time to expiration/strike/delay dependent.
 * The "delay" is the time between expiration of the option and last trading date of the underlying futures.
 */
public class BlackSTIRFuturesSmileProviderDiscount extends BlackSTIRFuturesSmileProvider {

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The SABR parameters.
   * @param index The cap/floor index.
   */
  public BlackSTIRFuturesSmileProviderDiscount(final MulticurveProviderDiscount multicurveProvider, final Surface<Double, Double, Double> parameters, final IborIndex index) {
    super(multicurveProvider, parameters, index);
  }

  @Override
  public BlackSTIRFuturesSmileProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new BlackSTIRFuturesSmileProviderDiscount(multicurveProvider, getBlackParameters(), getFuturesIndex());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}

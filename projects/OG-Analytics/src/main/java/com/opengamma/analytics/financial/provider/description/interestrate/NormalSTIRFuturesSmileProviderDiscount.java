/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Implementation of a provider of normal volatility (Bachelier model) smile for options on STIR futures. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying futures.
 */
public class NormalSTIRFuturesSmileProviderDiscount extends NormalSTIRFuturesSmileProvider {

  /**
   * @param multicurveProvider The multi-curves provider.
   * @param parameters The normal volatility parameters.
   * @param index The cap/floor index.
   */
  public NormalSTIRFuturesSmileProviderDiscount(MulticurveProviderDiscount multicurveProvider, Surface<Double, Double, Double> parameters, final IborIndex index) {
    super(multicurveProvider, parameters, index);
  }

  @Override
  public NormalSTIRFuturesSmileProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new NormalSTIRFuturesSmileProviderDiscount(multicurveProvider, getNormalParameters(), getFuturesIndex());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}

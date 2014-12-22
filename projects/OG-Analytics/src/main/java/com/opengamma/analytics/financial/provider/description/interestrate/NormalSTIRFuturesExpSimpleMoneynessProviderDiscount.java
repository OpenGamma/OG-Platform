/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.math.surface.Surface;


/**
 * Implementation of a provider of normal volatility (Bachelier model) smile for options on STIR futures. The volatility is time to expiration/strike/delay dependent. 
 */
public class NormalSTIRFuturesExpSimpleMoneynessProviderDiscount extends NormalSTIRFuturesExpSimpleMoneynessProvider {

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The normal volatility parameters.
   * @param index The cap/floor index.
   */
  public NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(MulticurveProviderDiscount multicurveProvider,
      Surface<Double, Double, Double> parameters, IborIndex index) {
    super(multicurveProvider, parameters, index);
  }

  @Override
  public NormalSTIRFuturesExpSimpleMoneynessProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(multicurveProvider, getNormalParameters(),
        getFuturesIndex());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

}

/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;


/**
 * Implementation of a provider of normal volatility (Bachelier model) smile for options on STIR futures. The volatility is time to expiration/strike dependent. 
 */
public class NormalSTIRFuturesExpSimpleMoneynessProviderDiscount extends NormalSTIRFuturesExpSimpleMoneynessProvider {

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The normal volatility parameters.
   * @param index The index underlying the futures for which the date is valid.
   * @param moneynessOnPrice Flag indicating if the moneyness is on the price (true) or on the rate (false).
   */
  public NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(MulticurveProviderDiscount multicurveProvider,
      Surface<Double, Double, Double> parameters, IborIndex index, boolean moneynessOnPrice)  {
    super(multicurveProvider, parameters, index, moneynessOnPrice);
  }

  @Override
  public NormalSTIRFuturesExpSimpleMoneynessProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = getMulticurveProvider().copy();
    return new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(multicurveProvider, getNormalParameters(),
        getFuturesIndex(), isMoneynessOnPrice());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

  @Override
  public NormalSTIRFuturesExpSimpleMoneynessProviderDiscount withMulticurve(MulticurveProviderInterface multicurve) {
    ArgumentChecker.isTrue(multicurve instanceof MulticurveProviderDiscount,
        "multicurve should be MulticurveProviderDiscount");
    MulticurveProviderDiscount casted = (MulticurveProviderDiscount) multicurve;
    return new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(casted, getNormalParameters(), getFuturesIndex(),
        isMoneynessOnPrice());
  }
}

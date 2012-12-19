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
public class NormalSTIRFuturesSmileProvider implements NormalSTIRFuturesSmileProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The Black volatility cube. Not null.
   * TODO: Change to a cube (with the delay dimension).
   */
  private final Surface<Double, Double, Double> _parameters;
  /**
   * The underlying swaps generators.
   */
  private final IborIndex _index;

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The normal volatility parameters.
   * @param index The cap/floor index.
   */
  public NormalSTIRFuturesSmileProvider(MulticurveProviderInterface multicurveProvider, Surface<Double, Double, Double> parameters, final IborIndex index) {
    _multicurveProvider = multicurveProvider;
    _parameters = parameters;
    _index = index;
  }

  @Override
  public NormalSTIRFuturesSmileProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new NormalSTIRFuturesSmileProvider(multicurveProvider, _parameters, _index);
  }

  @Override
  public double getVolatility(double expiry, double strike, double delay) {
    //TODO: delay is not used.
    return _parameters.getZValue(expiry, strike);
  }

  @Override
  public IborIndex getFuturesIndex() {
    return _index;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

  /**
   * Returns the Black parameters.
   * @return The parameters.
   */
  public Surface<Double, Double, Double> getNormalParameters() {
    return _parameters;
  }

}

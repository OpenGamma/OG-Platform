/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.math.surface.Surface;

/**
 * Implementation of a provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent. 
 */
public class BlackBondFuturesSmileProvider implements BlackBondFuturesSmileProviderInterface {

  /**
   * The multicurve provider.
   */
  private final IssuerProviderInterface _issuerProvider;
  /**
   * The Black volatility surface. Not null. The dimensions are expiry and strike.
   */
  private final Surface<Double, Double, Double> _parameters;

  // TODO: Add a reference to the underlying.
  //  /**
  //   * The underlying swaps generators.
  //   */
  //  private final IborIndex _index;

  /**
   * Constructor.
   * @param issuerProvider The issuer and multi-curve provider.
   * @param parameters The Black parameters.
   */
  public BlackBondFuturesSmileProvider(IssuerProviderInterface issuerProvider, Surface<Double, Double, Double> parameters) {
    _issuerProvider = issuerProvider;
    _parameters = parameters;
  }

  @Override
  public BlackBondFuturesSmileProvider copy() {
    IssuerProviderInterface multicurveProvider = _issuerProvider.copy();
    return new BlackBondFuturesSmileProvider(multicurveProvider, _parameters);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _issuerProvider;
  }

  @Override
  public double getVolatility(double expiry, double strike) {
    return _parameters.getZValue(expiry, strike);
  }

  /**
   * Returns the Black parameters.
   * @return The parameters.
   */
  public Surface<Double, Double, Double> getBlackParameters() {
    return _parameters;
  }

}

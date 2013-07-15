/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

/**
 * Implementation of a provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent. 
 */
public class BlackBondFuturesSmilePriceProvider implements BlackBondFuturesSmilePriceProviderInterface {

  /**
   * The Black bond futures smile provider.
   */
  private final BlackBondFuturesSmileProviderInterface _blackProvider;
  /**
   * The underlying bond futures price.
   */
  private final double _price;

  // TODO: Add a reference to the underlying.
  //  /**
  //   * The underlying swaps generators.
  //   */
  //  private final IborIndex _index;

  /**
   * Constructor.
   * @param provider 
   * @param price The underlying bond futures price.
   */
  public BlackBondFuturesSmilePriceProvider(final BlackBondFuturesSmileProviderInterface provider, final double price) {
    _blackProvider = provider;
    _price = price;
  }

  @Override
  public BlackBondFuturesSmilePriceProvider copy() {
    BlackBondFuturesSmileProviderInterface blackProvider = _blackProvider.copy();
    return new BlackBondFuturesSmilePriceProvider(blackProvider, _price);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _blackProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _blackProvider.getIssuerProvider();
  }

  @Override
  public double getFuturesPrice() {
    return _price;
  }

  @Override
  public BlackBondFuturesSmileProviderInterface getBlackProvider() {
    return _blackProvider;
  }

}

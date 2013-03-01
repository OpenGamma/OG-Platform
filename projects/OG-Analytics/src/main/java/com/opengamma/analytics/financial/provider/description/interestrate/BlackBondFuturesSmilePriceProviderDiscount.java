/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

/**
 * Implementation for swaption SABR parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class BlackBondFuturesSmilePriceProviderDiscount extends BlackBondFuturesSmilePriceProvider {

  /**
   * Constructor.
   * @param blackProvider The Black provider.
   * @param price The underlying bond futures price.
   */
  public BlackBondFuturesSmilePriceProviderDiscount(final BlackBondFuturesSmileProviderDiscount blackProvider, final double price) {
    super(blackProvider, price);
  }

  @Override
  public BlackBondFuturesSmilePriceProviderDiscount copy() {
    BlackBondFuturesSmileProviderDiscount blackProvider = getBlackProvider().copy();
    return new BlackBondFuturesSmilePriceProviderDiscount(blackProvider, getFuturesPrice());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

  @Override
  public IssuerProviderDiscount getIssuerProvider() {
    return (IssuerProviderDiscount) super.getIssuerProvider();
  }

  @Override
  public BlackBondFuturesSmileProviderDiscount getBlackProvider() {
    return (BlackBondFuturesSmileProviderDiscount) super.getBlackProvider();
  }

}

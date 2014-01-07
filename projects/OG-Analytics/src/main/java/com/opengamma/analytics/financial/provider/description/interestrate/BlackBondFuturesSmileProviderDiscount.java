/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.math.surface.Surface;

/**
 * Implementation for Black parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class BlackBondFuturesSmileProviderDiscount extends BlackBondFuturesSmileProvider {

  /**
   * @param issuerProvider The issuer and multi-curve provider.
   * @param parameters The Black parameters.
   */
  public BlackBondFuturesSmileProviderDiscount(final IssuerProviderDiscount issuerProvider, final Surface<Double, Double, Double> parameters) {
    super(issuerProvider, parameters);
  }

  @Override
  public BlackBondFuturesSmileProviderDiscount copy() {
    final IssuerProviderDiscount issuerProvider = getIssuerProvider().copy();
    return new BlackBondFuturesSmileProviderDiscount(issuerProvider, getBlackParameters());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return getIssuerProvider().getMulticurveProvider();
  }

  @Override
  public IssuerProviderDiscount getIssuerProvider() {
    return (IssuerProviderDiscount) super.getIssuerProvider();
  }

}

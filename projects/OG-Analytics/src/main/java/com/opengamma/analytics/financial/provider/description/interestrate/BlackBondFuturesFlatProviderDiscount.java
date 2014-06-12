/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Implementation for Black parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class BlackBondFuturesFlatProviderDiscount extends BlackBondFuturesFlatProvider {

  /**
   * @param issuerProvider The issuer and multi-curve provider.
   * @param parameters The Black parameters.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public BlackBondFuturesFlatProviderDiscount(final IssuerProviderDiscount issuerProvider, final Surface<Double, Double, Double> parameters, final LegalEntity legalEntity) {
    super(issuerProvider, parameters, legalEntity);
  }

  @Override
  public BlackBondFuturesFlatProviderDiscount copy() {
    final IssuerProviderDiscount issuerProvider = getIssuerProvider().copy();
    return new BlackBondFuturesFlatProviderDiscount(issuerProvider, getBlackParameters(), getLegalEntity());
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

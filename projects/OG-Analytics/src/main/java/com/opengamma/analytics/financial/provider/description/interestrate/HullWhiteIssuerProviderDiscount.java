/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a provider with discounting, forward, credit curves and Hull-White parameters on one issuer curve.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class HullWhiteIssuerProviderDiscount extends HullWhiteIssuerProvider {

  /**
   * Constructor from exiting multicurveProvider and Hull-White parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param issuer The issuer provider.
   * @param parameters The Hull-White one factor parameters.
   * @param issuerCcyHW The issuer/currency for which the Hull-White parameters are valid (Hull-White on the issuer discounting curve).
   */
  public HullWhiteIssuerProviderDiscount(final IssuerProviderDiscount issuer, HullWhiteOneFactorPiecewiseConstantParameters parameters, final Pair<String, Currency> issuerCcyHW) {
    super(issuer, parameters, issuerCcyHW);
  }

  /**
   * Returns the MulticurveProvider from which the HullWhiteOneFactorProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

  @Override
  public IssuerProviderDiscount getIssuerProvider() {
    return (IssuerProviderDiscount) super.getIssuerProvider();
  }

}

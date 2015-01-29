/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation for Black parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class BlackBondFuturesExpLogMoneynessProviderDiscount extends BlackBondFuturesExpLogMoneynessProvider {

  /**
   * @param issuerProvider The issuer and multi-curve provider.
   * @param parameters The Black parameters.
   * @param legalEntity The legal entity of the bonds underlying the futures for which the volatility data is valid.
   */
  public BlackBondFuturesExpLogMoneynessProviderDiscount(final IssuerProviderInterface issuerProvider,
      final Surface<Double, Double, Double> parameters, final LegalEntity legalEntity) {
    super(issuerProvider, parameters, legalEntity);
    ArgumentChecker.isTrue(issuerProvider instanceof IssuerProviderDiscount ||
        issuerProvider instanceof IssuerProviderIssuerAnnuallyCompoundeding,
        "issuerProvider should be IssuerProviderDiscount or contain IssuerProviderDiscount");
  }

  @Override
  public BlackBondFuturesExpLogMoneynessProviderDiscount copy() {
    IssuerProviderInterface issuerProvider = getIssuerProvider().copy();
    return new BlackBondFuturesExpLogMoneynessProviderDiscount(issuerProvider, getBlackParameters(), getLegalEntity());
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    IssuerProviderInterface issuerProvider = getIssuerProvider();
    if (issuerProvider instanceof IssuerProviderDiscount) {
      return ((IssuerProviderDiscount) issuerProvider).getMulticurveProvider();
    }
    return ((IssuerProviderIssuerAnnuallyCompoundeding) issuerProvider).getMulticurveProvider();
  }
}

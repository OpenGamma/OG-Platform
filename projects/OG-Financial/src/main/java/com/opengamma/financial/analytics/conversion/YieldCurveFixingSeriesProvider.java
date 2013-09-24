/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;

/**
 *
 */
public class YieldCurveFixingSeriesProvider extends FinancialSecurityVisitorSameValueAdapter<Set<ExternalIdBundle>> {
  private final ConventionBundleSource _conventionBundleSource;

  public YieldCurveFixingSeriesProvider(final ConventionBundleSource conventionBundleSource) {
    super(Collections.<ExternalIdBundle>emptySet());
    _conventionBundleSource = conventionBundleSource;
  }

  @Override
  public Set<ExternalIdBundle> visitFRASecurity(final FRASecurity security) {
    return Collections.singleton(_conventionBundleSource.getConventionBundle(security.getUnderlyingId()).getIdentifiers());
  }

  @Override
  public Set<ExternalIdBundle> visitSwapSecurity(final SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final Set<ExternalIdBundle> idBundles = new HashSet<>();
    if (payLeg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) payLeg;
      idBundles.add(_conventionBundleSource.getConventionBundle(floatLeg.getFloatingReferenceRateId()).getIdentifiers());
    }
    if (receiveLeg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) receiveLeg;
      idBundles.add(_conventionBundleSource.getConventionBundle(floatLeg.getFloatingReferenceRateId()).getIdentifiers());
    }
    return idBundles;
  }

  @Override
  public Set<ExternalIdBundle> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return Collections.singleton(security.getExternalIdBundle());
  }

}

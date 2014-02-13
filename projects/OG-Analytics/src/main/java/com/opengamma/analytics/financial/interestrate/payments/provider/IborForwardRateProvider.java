/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Default implementation for returning a forward rate from a curve for a given index, forward start and end, and year
 * fraction.
 */
public final class IborForwardRateProvider implements ForwardRateProvider<IborIndex> {
  
  private static final IborForwardRateProvider INSTANCE = new IborForwardRateProvider();
  
  private IborForwardRateProvider() {
  }
  
  public static IborForwardRateProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public <T extends DepositIndexCoupon<IborIndex>> double getRate(
      final MulticurveProviderInterface multicurves,
      final T coupon,
      final double fixingPeriodStartTime,
      double fixingPeriodEndTime,
      double fixingPeriodYearFraction) {
    return multicurves.getSimplyCompoundForwardRate(
        coupon.getIndex(), fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodYearFraction);
  }
}

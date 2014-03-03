/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

public final class OvernightForwardRateProvider implements ForwardRateProvider<IndexON> {

  /**
   * Singleton instance.
   */
  private static final OvernightForwardRateProvider INSTANCE = new OvernightForwardRateProvider();
  
  /**
   * Singleton constructor.
   */
  private OvernightForwardRateProvider() {
  }
  
  public static OvernightForwardRateProvider getInstance() {
    return INSTANCE;
  }
  
  @Override
  public <T extends DepositIndexCoupon<IndexON>> double getRate(MulticurveProviderInterface multicurves, T coupon, double fixingPeriodStartTime, double fixingPeriodEndTime,
      double fixingPeriodYearFraction) {
    return multicurves.getSimplyCompoundForwardRate(
        coupon.getIndex(), fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodYearFraction);
  }
}

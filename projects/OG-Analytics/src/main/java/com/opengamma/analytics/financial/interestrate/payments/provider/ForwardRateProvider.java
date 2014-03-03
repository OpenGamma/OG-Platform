/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Forward rate provider.
 * 
 * @param <T> the coupon to return the correspondng forward rate
 */
public interface ForwardRateProvider<U extends IndexDeposit> { // <T extends DepositIndexCoupon<U>, U extends IndexDeposit> {

  /**
   * Returns a forward rate for a specified fixing start and end, and year fraction.
   * 
   * @param multicurves the provider containing curves
   * @param coupon the coupon to return the corresponding forward rate
   * @param fixingPeriodStartTime the start of the forward period
   * @param fixingPeriodEndTime the end of the forward period
   * @param fixingPeriodYearFraction the year fraction of the period.
   * @return a forward rate.
   */
  <T extends DepositIndexCoupon<U>> double getRate(
      MulticurveProviderInterface multicurves,
      T coupon,
      double fixingPeriodStartTime,
      double fixingPeriodEndTime,
      double fixingPeriodYearFraction);
}

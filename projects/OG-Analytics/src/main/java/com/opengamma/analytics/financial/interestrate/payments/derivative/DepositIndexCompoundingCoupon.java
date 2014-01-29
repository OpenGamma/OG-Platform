/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;

/**
 * Interface for compounding coupons with deposit-like indices.
 * 
 * @param <T> The index type.
 */
public interface DepositIndexCompoundingCoupon<T extends IndexDeposit> extends DepositIndexCoupon<T> {

  /**
   * Returns the fixing times for the different remaining periods.
   * @return The times.
   */
  double[] getFixingTimes();
  
  /**
   * Gets the fixing period start times (in years).
   * @return The times.
   */
  double[] getFixingPeriodStartTimes();
  
  /**
   * Gets the fixing period end times (in years).
   * @return The times.
   */
  double[] getFixingPeriodEndTimes();

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  double[] getFixingPeriodAccrualFactors();
}

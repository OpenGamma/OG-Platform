/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import com.opengamma.financial.Region;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a fixed interest rate leg of a swap.
 */
public class FixedInterestRateLeg extends InterestRateLeg {
  private final double _fixedRate; // TODO: Elaine 28-May-2010 -- change rate from double to InterestRateType.

  /**
   * @param dayCount The daycount
   * @param frequency The frequency
   * @param region The region
   * @param businessDayConvention The business day convention
   * @param notional The notional
   * @param fixedRate the fixed interest rate as a decimal (e,g, 5% = 0.05)
   */
  public FixedInterestRateLeg(final DayCount dayCount, final Frequency frequency, final Region region, final BusinessDayConvention businessDayConvention, final InterestRateNotional notional,
      final double fixedRate) {
    super(dayCount, frequency, region, businessDayConvention, notional);
    ArgumentChecker.notNegative(fixedRate, "rate");
    _fixedRate = fixedRate;
  }

  /**
   * @return the fixed interest rate as a decimal (e.g. 5% = 0.05)
   */
  public double getRate() {
    return _fixedRate;
  }
}

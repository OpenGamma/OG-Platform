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

/**
 * 
 *
 */
public abstract class InterestRateLeg extends SwapLeg {

  public InterestRateLeg(final DayCount dayCount, final Frequency frequency, final Region region, final BusinessDayConvention businessDayConvention, final InterestRateNotional notional) {
    super(dayCount, frequency, region, businessDayConvention, notional);
  }
}

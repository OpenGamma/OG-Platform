package com.opengamma.financial.convention.businessday;

import javax.time.Instant;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class BusinessDayConvention {

  public abstract Instant getAdjustedDate(Instant date);

  protected boolean isWeekendOrHoliday(Instant date) {
    return false;
  }
}

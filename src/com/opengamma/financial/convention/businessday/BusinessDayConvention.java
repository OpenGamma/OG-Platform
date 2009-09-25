package com.opengamma.financial.convention.businessday;

import javax.time.Instant;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class BusinessDayConvention {
  // TODO use ZonedDateTime.with for all of this
  public abstract Instant getAdjustedDate(Instant date);

  protected boolean isWeekendOrHoliday(final Instant date) {
    return false;
  }
}

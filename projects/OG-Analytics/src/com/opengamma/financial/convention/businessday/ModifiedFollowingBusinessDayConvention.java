/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * The modified following business day convention.
 * <p>
 * This chooses the next working day following a non-working day, unless that
 * date is in a different month, in which case the date is adjusted to be the
 * preceding business day.
 */
public class ModifiedFollowingBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private static final BusinessDayConvention FOLLOWING = new FollowingBusinessDayConvention();
  private static final BusinessDayConvention PRECEDING = new PrecedingBusinessDayConvention();

  @Override
  public LocalDate adjustDate(final Calendar workingDays, final LocalDate date) {
    final LocalDate followingDate = FOLLOWING.adjustDate(workingDays, date);
    if (followingDate.getMonthOfYear() == date.getMonthOfYear()) {
      return followingDate;
    }
    return PRECEDING.adjustDate(workingDays, date);
  }

  @Override
  public String getConventionName() {
    return "Modified Following";
  }

}

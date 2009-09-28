/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

/**
 * Adjusts a date to the following business day, unless that date is in a
 * different month, in which case the date is adjusted to be the preceding
 * business day.
 * 
 * Also known as "Modified Following".
 * 
 * @author emcleod
 */

public class ModifiedBusinessDayConvention extends BusinessDayConvention {
  private static final BusinessDayConvention FOLLOWING = new FollowingBusinessDayConvention();
  private static final BusinessDayConvention PRECEDING = new PrecedingBusinessDayConvention();

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    final LocalDate followingDate = FOLLOWING.adjustDate(date);
    if (followingDate.getMonthOfYear() != date.getMonthOfYear()) {
      return PRECEDING.adjustDate(date);
    }
    return followingDate;
  }

}

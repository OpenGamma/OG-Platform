/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.threeten.bp.LocalDate;

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
    if (followingDate.getMonth() == date.getMonth()) {
      return followingDate;
    }
    return PRECEDING.adjustDate(workingDays, date);
  }

  @Override
  public String getName() {
    return "Modified Following";
  }

}

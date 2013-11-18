/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * The modified preceding business day convention.
 * <p>
 * This chooses the previous working day before a non-working day, unless than date is in a different month. 
 * In that case, the date is adjusted to be the following business day. 
 */
public class ModifiedPrecedingBusinessDayConvention extends AbstractBusinessDayConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  private static final BusinessDayConvention PRECEDING = new PrecedingBusinessDayConvention();
  private static final BusinessDayConvention FOLLOWING = new FollowingBusinessDayConvention();

  @Override
  public LocalDate adjustDate(final Calendar workingDayCalendar, final LocalDate date) {
    final LocalDate precedingDate = PRECEDING.adjustDate(workingDayCalendar, date);
    if (precedingDate.getMonth() == date.getMonth()) {
      return precedingDate;
    }
    return FOLLOWING.adjustDate(workingDayCalendar, date);
  }

  @Override
  public String getName() {
    return "Modified Preceding";
  }

}

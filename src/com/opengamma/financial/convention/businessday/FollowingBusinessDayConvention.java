/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;

/**
 * Adjusts a date to the following business day.
 * 
 * @author emcleod
 */

public class FollowingBusinessDayConvention extends BusinessDayConvention {

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    final LocalDate adjusted = LocalDate.date(date);
    if (isWeekendOrHoliday(date)) {
      final DateAdjuster adjuster = DateAdjusters.next(date.getDayOfWeek());
      return adjustDate(adjuster.adjustDate(adjusted));
    }
    return adjusted;
  }

}

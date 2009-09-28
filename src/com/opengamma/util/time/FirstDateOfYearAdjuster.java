/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.field.MonthOfYear;

/**
 * A date adjuster that provides the first date of a year given a date.
 * 
 * @author emcleod
 */
public class FirstDateOfYearAdjuster implements DateAdjuster {

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    return LocalDate.date(date.getYear(), MonthOfYear.JANUARY, 1);
  }

}

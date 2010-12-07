/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.core.convention.Calendar;

/**
 * 
 */
public class HolidayDateRemovalFunction {
  private static final LocalDate[] EMPTY_ARRAY = new LocalDate[0];

  public LocalDate[] getStrippedSchedule(final LocalDate[] dates, final Calendar holidays) {
    Validate.notNull(dates, "date");
    Validate.notNull(holidays, "holidays");
    final List<LocalDate> stripped = new ArrayList<LocalDate>();
    for (final LocalDate date : dates) {
      if (holidays.isWorkingDay(date)) {
        stripped.add(date);
      }
    }
    return stripped.toArray(EMPTY_ARRAY);
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.time.DateUtils;

/**
 * Generator of random time-series for testing/demo purposes.
 */
public final class RandomTimeSeriesGenerator {

  /**
   * Restricted constructor.
   */
  private RandomTimeSeriesGenerator() {
  }

  /**
   * Generates a random time-series for the specified number of days.
   * 
   * @param numDays  the number of days
   * @return the time-series, not null
   */
  public static LocalDateDoubleTimeSeries makeRandomTimeSeries(int numDays) {
    LocalDate previousWeekDay = DateUtils.previousWeekDay();
    return makeRandomTimeSeries(previousWeekDay, numDays);
  }

  /**
   * Generates a random time-series for the specified number of days from a start date.
   * 
   * @param startDate  the start date, not null
   * @param numDays  the number of days
   * @return the time-series, not null
   */
  public static LocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate startDate, int numDays) {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate current = startDate;
    bld.put(current, Math.random());
    while (bld.size() < numDays) {
      if (isWeekday(current)) {
        bld.put(current, Math.random());
      }
      current = current.plusDays(1);
    }
    return bld.build();
  }

  /**
   * Determine if the date is a weekday (not Saturday or Sunday).
   * 
   * @param day  the day-of-week, not null
   * @return true if Monday to Friday
   */
  private static boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }

}

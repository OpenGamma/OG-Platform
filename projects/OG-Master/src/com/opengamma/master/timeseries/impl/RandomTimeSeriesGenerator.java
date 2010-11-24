/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

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
  public static MapLocalDateDoubleTimeSeries makeRandomTimeSeries(int numDays) {
    LocalDate previousWeekDay = DateUtil.previousWeekDay();
    return makeRandomTimeSeries(previousWeekDay, numDays);
  }

  /**
   * Generates a random time-series for the specified number of days from a start date.
   * 
   * @param startDate  the start date, not null
   * @param numDays  the number of days
   * @return the time-series, not null
   */
  public static MapLocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate startDate, int numDays) {
    MapLocalDateDoubleTimeSeries tsMap = new MapLocalDateDoubleTimeSeries();
    LocalDate current = startDate;
    tsMap.putDataPoint(current, Math.random());
    while (tsMap.size() < numDays) {
      if (isWeekday(current)) {
        tsMap.putDataPoint(current, Math.random());
      }
      current = current.plusDays(1);
    }
    return tsMap;
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

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Utility class to generate random timeseries for testing/demo purposes
 */
public final class RandomTimeSeriesGenerator {
  
  /**
   * No object creation allowed
   */
  private RandomTimeSeriesGenerator() {
  }
  
  public static MapLocalDateDoubleTimeSeries makeRandomTimeSeries(int numDays) {
    LocalDate previousWeekDay = DateUtil.previousWeekDay();
    return makeRandomTimeSeries(previousWeekDay, numDays);
  }

  public static MapLocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate start, int numDays) {
    MapLocalDateDoubleTimeSeries tsMap = new MapLocalDateDoubleTimeSeries();
    LocalDate current = start;
    tsMap.putDataPoint(current, Math.random());
    while (tsMap.size() < numDays) {
      if (isWeekday(current)) {
        tsMap.putDataPoint(current, Math.random());
      }
      current = current.plusDays(1);
    }
    return tsMap;
  }
  
  private static boolean isWeekday(LocalDate day) {
    return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
  }

}

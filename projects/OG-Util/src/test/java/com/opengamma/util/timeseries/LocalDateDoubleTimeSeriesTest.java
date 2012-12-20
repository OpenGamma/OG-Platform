/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

/**
 * Test LocalDateDoubleTimeSeries.
 */
public abstract class LocalDateDoubleTimeSeriesTest extends DoubleTimeSeriesTest<LocalDate> {

  @Override
  public LocalDate[] testTimes() {
    LocalDate one = LocalDate.of(2010, MonthOfYear.FEBRUARY, 8);
    LocalDate two = LocalDate.of(2010, MonthOfYear.FEBRUARY, 9);
    LocalDate three = LocalDate.of(2010, MonthOfYear.FEBRUARY, 10);
    LocalDate four = LocalDate.of(2010, MonthOfYear.FEBRUARY, 11);
    LocalDate five = LocalDate.of(2010, MonthOfYear.FEBRUARY, 12);
    LocalDate six = LocalDate.of(2010, MonthOfYear.FEBRUARY, 13);
    return new LocalDate[] {one, two, three, four, five, six };
  }

  @Override
  public LocalDate[] testTimes2() {
    LocalDate one = LocalDate.of(2010, MonthOfYear.FEBRUARY, 11);
    LocalDate two = LocalDate.of(2010, MonthOfYear.FEBRUARY, 12);
    LocalDate three = LocalDate.of(2010, MonthOfYear.FEBRUARY, 13);
    LocalDate four = LocalDate.of(2010, MonthOfYear.FEBRUARY, 14);
    LocalDate five = LocalDate.of(2010, MonthOfYear.FEBRUARY, 15);
    LocalDate six = LocalDate.of(2010, MonthOfYear.FEBRUARY, 16);
    return new LocalDate[] {one, two, three, four, five, six };
  }

  @Override
  public LocalDate[] emptyTimes() {
    return new LocalDate[] {};
  }
  
}

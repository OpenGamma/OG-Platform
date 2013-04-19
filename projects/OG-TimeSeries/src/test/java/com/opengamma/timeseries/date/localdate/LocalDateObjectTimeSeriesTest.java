/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.timeseries.BigDecimalObjectTimeSeriesTest;

/**
 * Abstract test for LocalDateObjectTimeSeries.
 */
public abstract class LocalDateObjectTimeSeriesTest extends BigDecimalObjectTimeSeriesTest<LocalDate> {

  @Override
  protected LocalDate[] testTimes() {
    LocalDate one = LocalDate.of(2010, Month.FEBRUARY, 8);
    LocalDate two = LocalDate.of(2010, Month.FEBRUARY, 9);
    LocalDate three = LocalDate.of(2010, Month.FEBRUARY, 10);
    LocalDate four = LocalDate.of(2010, Month.FEBRUARY, 11);
    LocalDate five = LocalDate.of(2010, Month.FEBRUARY, 12);
    LocalDate six = LocalDate.of(2010, Month.FEBRUARY, 13);
    return new LocalDate[] { one, two, three, four, five, six };
  }

  @Override
  protected LocalDate[] testTimes2() {
    LocalDate one = LocalDate.of(2010, Month.FEBRUARY, 11);
    LocalDate two = LocalDate.of(2010, Month.FEBRUARY, 12);
    LocalDate three = LocalDate.of(2010, Month.FEBRUARY, 13);
    LocalDate four = LocalDate.of(2010, Month.FEBRUARY, 14);
    LocalDate five = LocalDate.of(2010, Month.FEBRUARY, 15);
    LocalDate six = LocalDate.of(2010, Month.FEBRUARY, 16);
    return new LocalDate[] { one, two, three, four, five, six };
  } 

  @Override
  protected LocalDate[] emptyTimes() {
    return new LocalDate[] {};
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

/**
 * 
 */
public abstract class LocalDateObjectTimeSeriesTest extends BigDecimalObjectTimeSeriesTest<LocalDate> {
  @Override
  public LocalDate[] testTimes() {
    LocalDate one = LocalDate.of(2010, Month.FEBRUARY, 8);
    LocalDate two = LocalDate.of(2010, Month.FEBRUARY, 9);
    LocalDate three = LocalDate.of(2010, Month.FEBRUARY, 10);
    LocalDate four = LocalDate.of(2010, Month.FEBRUARY, 11);
    LocalDate five = LocalDate.of(2010, Month.FEBRUARY, 12);
    LocalDate six = LocalDate.of(2010, Month.FEBRUARY, 13);
    return new LocalDate[] { one, two, three, four, five, six };
  }

  @Override
  public LocalDate[] testTimes2() {
    LocalDate one = LocalDate.of(2010, Month.FEBRUARY, 11);
    LocalDate two = LocalDate.of(2010, Month.FEBRUARY, 12);
    LocalDate three = LocalDate.of(2010, Month.FEBRUARY, 13);
    LocalDate four = LocalDate.of(2010, Month.FEBRUARY, 14);
    LocalDate five = LocalDate.of(2010, Month.FEBRUARY, 15);
    LocalDate six = LocalDate.of(2010, Month.FEBRUARY, 16);
    return new LocalDate[] { one, two, three, four, five, six };
  } 

  @Override
  public LocalDate[] emptyTimes() {
    return new LocalDate[] {};
  }
}

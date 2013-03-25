/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.sql.Date;
import java.util.Calendar;

/**
 * Test.
 */
public abstract class SQLDateDoubleTimeSeriesTest extends DoubleTimeSeriesTest<Date> {

  @Override
  protected Date[] testTimes() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 8); // feb
    Date one = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 9);
    Date two = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 10);
    Date three = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 11);
    Date four = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 12);
    Date five = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 13);
    Date six = new Date(cal.getTimeInMillis());
    return new Date[] {one, two, three, four, five, six };
  }

  @Override
  protected Date[] testTimes2() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 11); // feb
    Date one = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 12);
    Date two = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 13);
    Date three = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 14);
    Date four = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 15);
    Date five = new Date(cal.getTimeInMillis());
    cal.set(2010, 1, 16);
    Date six = new Date(cal.getTimeInMillis());
    return new Date[] {one, two, three, four, five, six };
  }

  @Override
  protected Date[] emptyTimes() {
    return new Date[] {};
  }

}

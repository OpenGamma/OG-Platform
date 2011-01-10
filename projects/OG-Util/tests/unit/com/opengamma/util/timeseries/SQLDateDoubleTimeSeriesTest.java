/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Calendar;
import java.sql.Date;

import org.junit.Ignore;

/**
 * 
 *
 * @author jim
 */
@Ignore
public abstract class SQLDateDoubleTimeSeriesTest extends DoubleTimeSeriesTest<Date> {
  @Override
  public Date[] testTimes() {
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
    return new Date[] { one, two, three, four, five, six };
  }

  @Override
  public Date[] testTimes2() {
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
    return new Date[] { one, two, three, four, five, six };
  } 

  @Override
  public Date[] emptyTimes() {
    return new Date[] {};
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Calendar;
import java.util.Date;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class DateDoubleTimeSeriesTest extends DoubleTimeSeriesTest<Date> {

  @Override
  protected Date[] testTimes() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 8); // feb
    Date one = cal.getTime();
    cal.set(2010, 1, 9);
    Date two = cal.getTime();
    cal.set(2010, 1, 10);
    Date three = cal.getTime();
    cal.set(2010, 1, 11);
    Date four = cal.getTime();
    cal.set(2010, 1, 12);
    Date five = cal.getTime();
    cal.set(2010, 1, 13);
    Date six = cal.getTime();
    return new Date[] {one, two, three, four, five, six };
  }

  @Override
  protected Date[] testTimes2() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2010, 1, 11); // feb
    Date one = cal.getTime();
    cal.set(2010, 1, 12);
    Date two = cal.getTime();
    cal.set(2010, 1, 13);
    Date three = cal.getTime();
    cal.set(2010, 1, 14);
    Date four = cal.getTime();
    cal.set(2010, 1, 15);
    Date five = cal.getTime();
    cal.set(2010, 1, 16);
    Date six = cal.getTime();
    return new Date[] {one, two, three, four, five, six };
  }

  @Override
  protected Date[] emptyTimes() {
    return new Date[] {};
  }

}

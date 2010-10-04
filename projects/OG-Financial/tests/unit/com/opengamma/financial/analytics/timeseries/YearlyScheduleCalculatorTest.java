/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;

import org.junit.Test;

/**
 * 
 */
public class YearlyScheduleCalculatorTest {
  private static final Schedule CALCULATOR = new YearlyScheduleCalculator();

  @Test(expected = IllegalArgumentException.class)
  public void testNullStart() {
    CALCULATOR.getSchedule(null, LocalDate.of(2000, 1, 1), true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEnd() {
    CALCULATOR.getSchedule(LocalDate.of(2000, 1, 1), null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAfterEnd() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 1, 1), LocalDate.of(2000, 1, 1), true);
  }

  public void testStartAndEndSame() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, false);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void test() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 12, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 2, 9);
    final int years = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, years);
    assertEquals(backward.length, years);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], LocalDate.of(2000, 2, 9));
    assertEquals(forward[years - 1], LocalDate.of(2010, 1, 1));
    assertEquals(backward[years - 1], endDate);
    for (int i = 1; i < years; i++) {
      assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
      assertEquals(forward[i].getMonthOfYear(), startDate.getMonthOfYear());
      assertEquals(forward[i].getDayOfMonth(), startDate.getDayOfMonth());
      assertEquals(backward[i].getYear() - backward[i - 1].getYear(), 1);
      assertEquals(backward[i].getMonthOfYear(), endDate.getMonthOfYear());
      assertEquals(backward[i].getDayOfMonth(), endDate.getDayOfMonth());
    }
  }
}

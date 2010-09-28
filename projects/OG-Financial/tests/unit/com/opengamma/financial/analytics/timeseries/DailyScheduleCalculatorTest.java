/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class DailyScheduleCalculatorTest {
  private static final Schedule CALCULATOR = new DailyScheduleCalculator();

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

  @Test
  public void testSameStartAndEnd() {
    final LocalDate date = LocalDate.of(2000, 1, 1);
    final LocalDate[] forward = CALCULATOR.getSchedule(date, date, false);
    final LocalDate[] backward = CALCULATOR.getSchedule(date, date, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], date);
    assertEquals(backward[0], date);
  }

  @Test
  public void test() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    final LocalDate endDate = LocalDate.of(2002, 2, 9);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false);
    final LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true);
    final int days = 771;
    assertEquals(forward.length, days);
    assertEquals(backward.length, days);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], startDate);
    assertEquals(forward[days - 1], endDate);
    assertEquals(forward[days - 1], endDate);
    for (int i = 1; i < days; i++) {
      assertEquals(forward[i], backward[i]);
      assertEquals(DateUtil.getDaysBetween(forward[i], forward[i - 1]), 1);
    }
  }
}

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
public class MonthlyScheduleCalculatorTest {
  private static final Schedule CALCULATOR = new MonthlyScheduleCalculator();

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
    LocalDate endDate = LocalDate.of(2000, 1, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2002, 2, 1);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2002, 2, 9);
    final int months = 26;
    forward = CALCULATOR.getSchedule(startDate, endDate, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forward.length, months);
    assertEquals(backward.length, months);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], LocalDate.of(2000, 1, 9));
    assertEquals(forward[months - 1], LocalDate.of(2002, 2, 1));
    assertEquals(backward[months - 1], endDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthOfYear().getValue() - forward[i - 1].getMonthOfYear().getValue(), 1);
        assertEquals(backward[i].getMonthOfYear().getValue() - backward[i - 1].getMonthOfYear().getValue(), 1);
      } else {
        assertEquals(forward[i].getMonthOfYear().getValue() - forward[i - 1].getMonthOfYear().getValue(), -11);
        assertEquals(backward[i].getMonthOfYear().getValue() - backward[i - 1].getMonthOfYear().getValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 1);
      assertEquals(backward[i].getDayOfMonth(), 9);
    }
  }
}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.junit.Test;

/**
 * 
 */
public class YearlyScheduleOnDayAndMonthCalculatorTest {
  private static final int DAY_OF_MONTH = 15;
  private static final MonthOfYear MONTH_OF_YEAR = MonthOfYear.APRIL;
  private static final Schedule CALCULATOR = new YearlyScheduleOnDayAndMonthCalculator(DAY_OF_MONTH, MONTH_OF_YEAR);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDay() {
    new YearlyScheduleOnDayAndMonthCalculator(-DAY_OF_MONTH, MONTH_OF_YEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadDay() {
    new YearlyScheduleOnDayAndMonthCalculator(31, MonthOfYear.FEBRUARY);
  }

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

  @Test(expected = IllegalArgumentException.class)
  public void testSameDayBadDates() {
    final LocalDate date = LocalDate.of(2001, MONTH_OF_YEAR, 12);
    CALCULATOR.getSchedule(date, date, false);
  }

  @Test
  public void testSameDayGoodDates() {
    final LocalDate date = LocalDate.of(2001, MONTH_OF_YEAR, DAY_OF_MONTH);
    final LocalDate[] result = CALCULATOR.getSchedule(date, date, false);
    assertEquals(result.length, 1);
    assertEquals(result[0], date);
  }

  @Test
  public void test() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 3, 1);
    LocalDate[] forwards = CALCULATOR.getSchedule(startDate, endDate, false);
    LocalDate[] backwards = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forwards.length, 0);
    assertEquals(backwards.length, 0);
    startDate = LocalDate.of(2000, 5, 1);
    endDate = LocalDate.of(2001, 3, 1);
    forwards = CALCULATOR.getSchedule(startDate, endDate, false);
    backwards = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forwards.length, 0);
    assertEquals(backwards.length, 0);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 5, 1);
    final int years = 11;
    forwards = CALCULATOR.getSchedule(startDate, endDate, false);
    backwards = CALCULATOR.getSchedule(startDate, endDate, true);
    assertEquals(forwards.length, years);
    assertEquals(backwards.length, years);
    assertEquals(forwards[0], LocalDate.of(2000, MONTH_OF_YEAR, DAY_OF_MONTH));
    assertEquals(backwards[0], LocalDate.of(2000, MONTH_OF_YEAR, DAY_OF_MONTH));
    assertEquals(forwards[years - 1], LocalDate.of(2010, MONTH_OF_YEAR, DAY_OF_MONTH));
    assertEquals(backwards[years - 1], LocalDate.of(2010, MONTH_OF_YEAR, DAY_OF_MONTH));
    for (int i = 1; i < years; i++) {
      assertEquals(forwards[i].getYear() - forwards[i - 1].getYear(), 1);
      assertEquals(forwards[i].getMonthOfYear(), MONTH_OF_YEAR);
      assertEquals(forwards[i].getDayOfMonth(), DAY_OF_MONTH);
      assertEquals(backwards[i].getYear() - backwards[i - 1].getYear(), 1);
      assertEquals(backwards[i].getMonthOfYear(), MONTH_OF_YEAR);
      assertEquals(backwards[i].getDayOfMonth(), DAY_OF_MONTH);
    }
  }
}

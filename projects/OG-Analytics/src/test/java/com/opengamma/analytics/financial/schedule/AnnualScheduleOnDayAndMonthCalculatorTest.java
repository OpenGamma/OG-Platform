/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnnualScheduleOnDayAndMonthCalculatorTest extends ScheduleCalculatorTestCase {
  private static final int DAY_OF_MONTH = 15;
  private static final Month MONTH_OF_YEAR = Month.APRIL;
  private static final AnnualScheduleOnDayAndMonthCalculator CALCULATOR = new AnnualScheduleOnDayAndMonthCalculator(DAY_OF_MONTH, MONTH_OF_YEAR);

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test(expectedExceptions = DateTimeException.class)
  public void testNegativeDay() {
    new AnnualScheduleOnDayAndMonthCalculator(-DAY_OF_MONTH, MONTH_OF_YEAR);
  }

  @Test(expectedExceptions = DateTimeException.class)
  public void testBadDay() {
    new AnnualScheduleOnDayAndMonthCalculator(31, Month.FEBRUARY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSameDayBadDates1() {
    final LocalDate date = LocalDate.of(2001, MONTH_OF_YEAR.getValue(), 12);
    CALCULATOR.getSchedule(date, date, false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSameDayBadDates2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2001, MONTH_OF_YEAR.getValue(), 12);
    CALCULATOR.getSchedule(date, date, false, true);
  }

  @Test
  public void testSameDayGoodDates1() {
    final LocalDate date = LocalDate.of(2001, MONTH_OF_YEAR.getValue(), DAY_OF_MONTH);
    final LocalDate[] result = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(result.length, 1);
    assertEquals(result[0], date);
  }

  @Test
  public void testSameDayGoodDates2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2001, MONTH_OF_YEAR.getValue(), DAY_OF_MONTH);
    final ZonedDateTime[] result = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(result.length, 1);
    assertEquals(result[0], date);
  }

  @Test
  public void test1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 3, 1);
    LocalDate[] forwards = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forwards.length, 0);
    startDate = LocalDate.of(2000, 5, 1);
    endDate = LocalDate.of(2001, 3, 1);
    forwards = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forwards.length, 0);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 5, 1);
    final int years = 11;
    forwards = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forwards.length, years);
    assertEquals(forwards[0], LocalDate.of(2000, MONTH_OF_YEAR.getValue(), DAY_OF_MONTH));
    assertEquals(forwards[years - 1], LocalDate.of(2010, MONTH_OF_YEAR.getValue(), DAY_OF_MONTH));
    for (int i = 1; i < years; i++) {
      assertEquals(forwards[i].getYear() - forwards[i - 1].getYear(), 1);
      assertEquals(forwards[i].getMonth(), MONTH_OF_YEAR);
      assertEquals(forwards[i].getDayOfMonth(), DAY_OF_MONTH);
    }
    assertArrayEquals(forwards, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forwards, CALCULATOR.getSchedule(startDate, endDate));
  }

  @Test
  public void test2() {
    ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtils.getUTCDate(2000, 3, 1);
    ZonedDateTime[] forwards = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forwards.length, 0);
    startDate = DateUtils.getUTCDate(2000, 5, 1);
    endDate = DateUtils.getUTCDate(2001, 3, 1);
    forwards = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forwards.length, 0);
    startDate = DateUtils.getUTCDate(2000, 1, 1);
    endDate = DateUtils.getUTCDate(2010, 5, 1);
    final int years = 11;
    forwards = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forwards.length, years);
    assertEquals(forwards[0], DateUtils.getUTCDate(2000, MONTH_OF_YEAR.getValue(), DAY_OF_MONTH));
    assertEquals(forwards[years - 1], DateUtils.getUTCDate(2010, MONTH_OF_YEAR.getValue(), DAY_OF_MONTH));
    for (int i = 1; i < years; i++) {
      assertEquals(forwards[i].getYear() - forwards[i - 1].getYear(), 1);
      assertEquals(forwards[i].getMonth(), MONTH_OF_YEAR);
      assertEquals(forwards[i].getDayOfMonth(), DAY_OF_MONTH);
    }
    assertArrayEquals(forwards, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forwards, CALCULATOR.getSchedule(startDate, endDate));
  }
}

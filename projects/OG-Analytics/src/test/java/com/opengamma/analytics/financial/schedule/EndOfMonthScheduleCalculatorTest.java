/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EndOfMonthScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final EndOfMonthScheduleCalculator CALCULATOR = new EndOfMonthScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid1() {
    CALCULATOR.getSchedule(LocalDate.of(2001, 2, 3), LocalDate.of(2001, 2, 3), false, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid2() {
    CALCULATOR.getSchedule(DateUtils.getUTCDate(2001, 2, 3), DateUtils.getUTCDate(2001, 2, 3), false, true);
  }

  @Test
  public void testSameDates1() {
    final LocalDate date = LocalDate.of(2001, 1, 31);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, true, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testSameDates2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2001, 1, 31);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date, true, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testNoEndDateInRange1() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    final LocalDate endDate = LocalDate.of(2000, 1, 30);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 0);
    assertEquals(backward.length, 0);
  }

  @Test
  public void testNoEndDateInRange2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2000, 1, 30);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 0);
    assertEquals(backward.length, 0);
  }

  @Test
  public void testStartDateIsEnd1() {
    final LocalDate startDate = LocalDate.of(2002, 1, 31);
    final LocalDate endDate = LocalDate.of(2002, 2, 9);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], startDate);
  }

  @Test
  public void testStartDateIsEnd2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2002, 1, 31);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2002, 2, 9);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    final ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], startDate);
  }

  @Test
  public void test1() {
    final LocalDate startDate = LocalDate.of(2000, 1, 1);
    final LocalDate endDate = LocalDate.of(2002, 2, 9);
    final int months = 25;
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, months);
    final LocalDate firstDate = LocalDate.of(2000, 1, 31);
    assertEquals(forward[0], firstDate);
    final LocalDate lastDate = LocalDate.of(2002, 1, 31);
    assertEquals(forward[months - 1], lastDate);
    LocalDate d1;
    for (int i = 1; i < months; i++) {
      d1 = forward[i];
      if (d1.getYear() == forward[i - 1].getYear()) {
        assertEquals(d1.getMonthValue() - forward[i - 1].getMonthValue(), 1);
      } else {
        assertEquals(d1.getMonthValue() - forward[i - 1].getMonthValue(), -11);
      }
      assertEquals(d1.getDayOfMonth(), d1.lengthOfMonth());
    }
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, false), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, false), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate), forward);
  }

  @Test
  public void test2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2002, 2, 9);
    final int months = 25;
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, months);
    final ZonedDateTime firstDate = DateUtils.getUTCDate(2000, 1, 31);
    assertEquals(forward[0], firstDate);
    final ZonedDateTime lastDate = DateUtils.getUTCDate(2002, 1, 31);
    assertEquals(forward[months - 1], lastDate);
    ZonedDateTime d1;
    for (int i = 1; i < months; i++) {
      d1 = forward[i];
      if (d1.getYear() == forward[i - 1].getYear()) {
        assertEquals(d1.getMonthValue() - forward[i - 1].getMonthValue(), 1);
      } else {
        assertEquals(d1.getMonthValue() - forward[i - 1].getMonthValue(), -11);
      }
      assertEquals(d1.getDayOfMonth(), d1.toLocalDate().lengthOfMonth());
    }
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, false), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, true, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, false), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), forward);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate), forward);
  }
}

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
public class MonthlyScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final MonthlyScheduleCalculator CALCULATOR = new MonthlyScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAfterEnd() {
    CALCULATOR.getSchedule(DateUtils.getUTCDate(2001, 1, 1), DateUtils.getUTCDate(2000, 1, 1), true, true);
  }

  @Test
  public void testStartAndEndSame1() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, true, false);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testStartAndEndSame2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2001, 2, 13);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date, true, false);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testRecursive1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 1, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2002, 2, 1);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2002, 2, 9);
    final int months = 26;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, months);
    assertEquals(backward.length, months);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], LocalDate.of(2000, 1, 9));
    assertEquals(forward[months - 1], LocalDate.of(2002, 2, 1));
    assertEquals(backward[months - 1], endDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), 1);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), 1);
      } else {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), -11);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 1);
      assertEquals(backward[i].getDayOfMonth(), 9);
    }
  }

  @Test
  public void testRecursive2() {
    ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtils.getUTCDate(2000, 1, 30);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = DateUtils.getUTCDate(2002, 2, 1);
    endDate = DateUtils.getUTCDate(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = DateUtils.getUTCDate(2000, 1, 1);
    endDate = DateUtils.getUTCDate(2002, 2, 9);
    final int months = 26;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, months);
    assertEquals(backward.length, months);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], DateUtils.getUTCDate(2000, 1, 9));
    assertEquals(forward[months - 1], DateUtils.getUTCDate(2002, 2, 1));
    assertEquals(backward[months - 1], endDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), 1);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), 1);
      } else {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), -11);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 1);
      assertEquals(backward[i].getDayOfMonth(), 9);
    }
  }

  @Test
  public void test1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 1, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, false);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2002, 2, 1);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2002, 2, 9);
    final int months = 26;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertEquals(forward.length, months);
    assertEquals(backward.length, months);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], LocalDate.of(2000, 1, 9));
    assertEquals(forward[months - 1], LocalDate.of(2002, 2, 1));
    assertEquals(backward[months - 1], endDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), 1);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), 1);
      } else {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), -11);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 1);
      assertEquals(backward[i].getDayOfMonth(), 9);
    }
  }

  @Test
  public void test2() {
    ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtils.getUTCDate(2000, 1, 30);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, false);
    ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = DateUtils.getUTCDate(2002, 2, 1);
    endDate = DateUtils.getUTCDate(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = DateUtils.getUTCDate(2000, 1, 1);
    endDate = DateUtils.getUTCDate(2002, 2, 9);
    final int months = 26;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, false);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertEquals(forward.length, months);
    assertEquals(backward.length, months);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], DateUtils.getUTCDate(2000, 1, 9));
    assertEquals(forward[months - 1], DateUtils.getUTCDate(2002, 2, 1));
    assertEquals(backward[months - 1], endDate);
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), 1);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), 1);
      } else {
        assertEquals(forward[i].getMonthValue() - forward[i - 1].getMonthValue(), -11);
        assertEquals(backward[i].getMonthValue() - backward[i - 1].getMonthValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 1);
      assertEquals(backward[i].getDayOfMonth(), 9);
    }
  }

  @Test
  public void testEndOfMonth1() {
    final LocalDate startDate = LocalDate.of(2009, 11, 30);
    final LocalDate endDate = LocalDate.of(2010, 8, 31);
    final LocalDate[] fromStart = new LocalDate[] {LocalDate.of(2009, 11, 30), LocalDate.of(2009, 12, 30), LocalDate.of(2010, 1, 30), LocalDate.of(2010, 2, 28),
        LocalDate.of(2010, 3, 30), LocalDate.of(2010, 4, 30), LocalDate.of(2010, 5, 30), LocalDate.of(2010, 6, 30), LocalDate.of(2010, 7, 30),
        LocalDate.of(2010, 8, 30)};
    final LocalDate[] fromStartRecursive = new LocalDate[] {LocalDate.of(2009, 11, 30), LocalDate.of(2009, 12, 30), LocalDate.of(2010, 1, 30),
        LocalDate.of(2010, 2, 28), LocalDate.of(2010, 3, 28), LocalDate.of(2010, 4, 28), LocalDate.of(2010, 5, 28), LocalDate.of(2010, 6, 28),
        LocalDate.of(2010, 7, 28), LocalDate.of(2010, 8, 28)};
    final LocalDate[] fromEnd = new LocalDate[] {LocalDate.of(2009, 11, 30), LocalDate.of(2009, 12, 31), LocalDate.of(2010, 1, 31), LocalDate.of(2010, 2, 28),
        LocalDate.of(2010, 3, 31), LocalDate.of(2010, 4, 30), LocalDate.of(2010, 5, 31), LocalDate.of(2010, 6, 30), LocalDate.of(2010, 7, 31),
        LocalDate.of(2010, 8, 31)};
    final LocalDate[] fromEndRecursive = new LocalDate[] {LocalDate.of(2009, 12, 28), LocalDate.of(2010, 1, 28),
        LocalDate.of(2010, 2, 28), LocalDate.of(2010, 3, 30), LocalDate.of(2010, 4, 30), LocalDate.of(2010, 5, 30), LocalDate.of(2010, 6, 30),
        LocalDate.of(2010, 7, 31), LocalDate.of(2010, 8, 31)};
    assertArrayEquals(fromStart, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(fromStartRecursive, CALCULATOR.getSchedule(startDate, endDate, false, true));
    assertArrayEquals(fromEnd, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(fromEndRecursive, CALCULATOR.getSchedule(startDate, endDate, true, true));
  }

  @Test
  public void testEndOfMonth2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2009, 11, 30);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2010, 8, 31);
    final ZonedDateTime[] fromStart = new ZonedDateTime[] {DateUtils.getUTCDate(2009, 11, 30), DateUtils.getUTCDate(2009, 12, 30), DateUtils.getUTCDate(2010, 1, 30), DateUtils.getUTCDate(2010, 2, 28),
        DateUtils.getUTCDate(2010, 3, 30), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 30), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 30),
        DateUtils.getUTCDate(2010, 8, 30)};
    final ZonedDateTime[] fromStartRecursive = new ZonedDateTime[] {DateUtils.getUTCDate(2009, 11, 30), DateUtils.getUTCDate(2009, 12, 30), DateUtils.getUTCDate(2010, 1, 30),
        DateUtils.getUTCDate(2010, 2, 28), DateUtils.getUTCDate(2010, 3, 28), DateUtils.getUTCDate(2010, 4, 28), DateUtils.getUTCDate(2010, 5, 28), DateUtils.getUTCDate(2010, 6, 28),
        DateUtils.getUTCDate(2010, 7, 28), DateUtils.getUTCDate(2010, 8, 28)};
    final ZonedDateTime[] fromEnd = new ZonedDateTime[] {DateUtils.getUTCDate(2009, 11, 30), DateUtils.getUTCDate(2009, 12, 31), DateUtils.getUTCDate(2010, 1, 31), DateUtils.getUTCDate(2010, 2, 28),
        DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 31),
        DateUtils.getUTCDate(2010, 8, 31)};
    final ZonedDateTime[] fromEndRecursive = new ZonedDateTime[] {DateUtils.getUTCDate(2009, 12, 28), DateUtils.getUTCDate(2010, 1, 28),
        DateUtils.getUTCDate(2010, 2, 28), DateUtils.getUTCDate(2010, 3, 30), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 30), DateUtils.getUTCDate(2010, 6, 30),
        DateUtils.getUTCDate(2010, 7, 31), DateUtils.getUTCDate(2010, 8, 31)};
    assertArrayEquals(fromStart, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(fromStartRecursive, CALCULATOR.getSchedule(startDate, endDate, false, true));
    assertArrayEquals(fromEnd, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(fromEndRecursive, CALCULATOR.getSchedule(startDate, endDate, true, true));
  }
}

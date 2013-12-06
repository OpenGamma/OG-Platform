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
public class AnnualScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final AnnualScheduleCalculator CALCULATOR = new AnnualScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test
  public void testStartAndEndSame1() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testStartAndEndSame2() {
    final ZonedDateTime date = DateUtils.getUTCDate(2001, 2, 13);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void test1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 12, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 2, 9);
    final int years = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, years);
    assertEquals(backward.length, years);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], LocalDate.of(2000, 2, 9));
    assertEquals(forward[years - 1], LocalDate.of(2010, 1, 1));
    assertEquals(backward[years - 1], endDate);
    for (int i = 1; i < years; i++) {
      assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
      assertEquals(forward[i].getMonth(), startDate.getMonth());
      assertEquals(forward[i].getDayOfMonth(), startDate.getDayOfMonth());
      assertEquals(backward[i].getYear() - backward[i - 1].getYear(), 1);
      assertEquals(backward[i].getMonth(), endDate.getMonth());
      assertEquals(backward[i].getDayOfMonth(), endDate.getDayOfMonth());
    }
  }

  @Test
  public void test2() {
    ZonedDateTime startDate = DateUtils.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtils.getUTCDate(2000, 12, 30);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = DateUtils.getUTCDate(2000, 1, 1);
    endDate = DateUtils.getUTCDate(2010, 2, 9);
    final int years = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, years);
    assertEquals(backward.length, years);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], DateUtils.getUTCDate(2000, 2, 9));
    assertEquals(forward[years - 1], DateUtils.getUTCDate(2010, 1, 1));
    assertEquals(backward[years - 1], endDate);
    for (int i = 1; i < years; i++) {
      assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
      assertEquals(forward[i].getMonth(), startDate.getMonth());
      assertEquals(forward[i].getDayOfMonth(), startDate.getDayOfMonth());
      assertEquals(backward[i].getYear() - backward[i - 1].getYear(), 1);
      assertEquals(backward[i].getMonth(), endDate.getMonth());
      assertEquals(backward[i].getDayOfMonth(), endDate.getDayOfMonth());
    }
  }

  @Test
  public void testEndOfMonth1() {
    final LocalDate startDate = LocalDate.of(2000, 2, 29);
    final LocalDate endDate = LocalDate.of(2008, 2, 29);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    final LocalDate[] recursive = new LocalDate[] {LocalDate.of(2000, 2, 29), LocalDate.of(2001, 2, 28), LocalDate.of(2002, 2, 28), LocalDate.of(2003, 2, 28), LocalDate.of(2004, 2, 28),
        LocalDate.of(2005, 2, 28), LocalDate.of(2006, 2, 28), LocalDate.of(2007, 2, 28), LocalDate.of(2008, 2, 28)};
    final LocalDate[] nonRecursive = new LocalDate[] {LocalDate.of(2000, 2, 29), LocalDate.of(2001, 2, 28), LocalDate.of(2002, 2, 28), LocalDate.of(2003, 2, 28), LocalDate.of(2004, 2, 29),
        LocalDate.of(2005, 2, 28), LocalDate.of(2006, 2, 28), LocalDate.of(2007, 2, 28), LocalDate.of(2008, 2, 29)};
    assertArrayEquals(forward, nonRecursive);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), recursive);
  }

  @Test
  public void testEndOfMonth2() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2000, 2, 29);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2008, 2, 29);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    final ZonedDateTime[] recursive = new ZonedDateTime[] {DateUtils.getUTCDate(2000, 2, 29), DateUtils.getUTCDate(2001, 2, 28), DateUtils.getUTCDate(2002, 2, 28), DateUtils.getUTCDate(2003, 2, 28),
        DateUtils.getUTCDate(2004, 2, 28),
        DateUtils.getUTCDate(2005, 2, 28), DateUtils.getUTCDate(2006, 2, 28), DateUtils.getUTCDate(2007, 2, 28), DateUtils.getUTCDate(2008, 2, 28)};
    final ZonedDateTime[] nonRecursive = new ZonedDateTime[] {DateUtils.getUTCDate(2000, 2, 29), DateUtils.getUTCDate(2001, 2, 28), DateUtils.getUTCDate(2002, 2, 28), DateUtils.getUTCDate(2003, 2, 28),
        DateUtils.getUTCDate(2004, 2, 29),
        DateUtils.getUTCDate(2005, 2, 28), DateUtils.getUTCDate(2006, 2, 28), DateUtils.getUTCDate(2007, 2, 28), DateUtils.getUTCDate(2008, 2, 29)};
    assertArrayEquals(forward, nonRecursive);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), recursive);
  }
}

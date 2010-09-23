/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * Test DayCount.
 */
public class DayCountTest {

  private static final double EPS = 1e-9;

  @Test
  public void testOneOne() {
    final DayCount daycount = new OneOneDayCount();
    try {
      daycount.getBasis(null);
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    assertEquals(daycount.getDayCountFraction(DateUtil.getUTCDate(2009, 1, 1), DateUtil.getUTCDate(2009, 1, 1)), 1, EPS);
  }

  @Test
  public void testThirtyThreeSixty() {
    final DayCount convention = DayCountFactory.INSTANCE.getDayCount("30/360"); // new ThirtyThreeSixtyDayCount();
    final double basis = convention.getBasis(null);
    testOneYearNoLeapYears(convention);
    testOneYearOneLeapYear(convention);
    testFourMonths(convention, 1. / 3);
    testOneDayNoLeapYear(convention);
    testOneDayLeapYear(convention, 2. / basis);
    testFirstDayThirtyDayAdjustment(convention, 30. / basis);
    testSecondDayThirtyDayAdjustment(convention, 33. / basis);
  }

  @Test
  public void testThirtyEThreeSixty() {
    final DayCount convention = DayCountFactory.INSTANCE.getDayCount("30E/360"); // new ThirtyEThreeSixtyDayCount();
    final double basis = convention.getBasis(null);
    testOneYearNoLeapYears(convention);
    testOneYearOneLeapYear(convention);
    testFourMonths(convention, 1. / 3);
    testOneDayNoLeapYear(convention);
    testOneDayLeapYear(convention, 2 / basis);
    testFirstDayThirtyDayAdjustment(convention, 30. / basis);
    testSecondDayThirtyDayAdjustment(convention, 32. / basis);
  }

  @Test
  public void testThirtyEThreeSixtyISDA() {
    final DayCount convention = DayCountFactory.INSTANCE.getDayCount("30E/360 (ISDA)"); // new ThirtyEThreeSixtyISDADayCount();
    final double basis = convention.getBasis(null);
    testOneYearNoLeapYears(convention);
    testOneYearOneLeapYear(convention);
    testFourMonths(convention, 1. / 3);
    testOneDayNoLeapYear(convention);
    testOneDayLeapYear(convention, 1. / basis);
    testFirstDayThirtyDayAdjustment(convention, 30. / basis);
    testSecondDayThirtyDayAdjustment(convention, 32. / basis);
  }

  @Test
  public void testActualThreeSixtyFiveFixed() {
    final DayCount convention = DayCountFactory.INSTANCE.getDayCount("A/365F"); // new ActualThreeSixtyFiveFixedDayCount();
    final double basis = convention.getBasis(null);
    testOneYearNoLeapYears(convention);
    testOneYearOneLeapYear(convention, 1. / basis);
    testFourMonths(convention, 121. / basis);
    testOneDayNoLeapYear(convention);
    testOneDayLeapYear(convention, 1. / basis);
    testFirstDayThirtyDayAdjustment(convention, 31. / basis);
    testSecondDayThirtyDayAdjustment(convention, 33. / basis);
  }

  @Test
  public void testActualThreeSixty() {
    final DayCount convention = DayCountFactory.INSTANCE.getDayCount("A/360"); // new ActualThreeSixtyDayCount();
    final double basis = convention.getBasis(null);
    testOneYearNoLeapYears(convention, 5. / basis);
    testOneYearOneLeapYear(convention, 6. / basis);
    testFourMonths(convention, 121. / basis);
    testOneDayNoLeapYear(convention);
    testOneDayLeapYear(convention, 1. / basis);
    testFirstDayThirtyDayAdjustment(convention, 31. / basis);
    testSecondDayThirtyDayAdjustment(convention, 33. / basis);
  }

  @Test
  public void testActualActual() {
    final DayCount convention = DayCountFactory.INSTANCE.getDayCount("Act/Act (ISDA)"); // new ActualActualISDADayCount();
    final ZonedDateTime d1 = DateUtil.getUTCDate(2007, 12, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2008, 2, 1);
    final double basis = convention.getBasis(d1);
    final double leapYearBasis = convention.getBasis(d2);
    testOneYearNoLeapYears(convention);
    testOneYearOneLeapYear(convention);
    testFourMonths(convention, 121. / leapYearBasis);
    testOneDayNoLeapYear(convention, 1. / basis);
    testOneDayLeapYear(convention, 1. / leapYearBasis);
    testFirstDayThirtyDayAdjustment(convention, 31. / basis);
    testSecondDayThirtyDayAdjustment(convention, 33. / basis);
    // overlap start of leap year
    assertFractionEquals(convention, d1, d2, 31. / basis + 31. / leapYearBasis);
    // overlap end of leap year
    assertFractionEquals(convention, DateUtil.getUTCDate(2008, 12, 1), DateUtil.getUTCDate(2009, 2, 1), 31. / leapYearBasis + 31. / basis);
    // overlap leap year completely
    assertFractionEquals(convention, d1, DateUtil.getUTCDate(2009, 2, 1), 1. + 62. / basis);
    // overlap multiple leap years
    assertFractionEquals(convention, DateUtil.getUTCDate(1997, 12, 1), DateUtil.getUTCDate(2009, 2, 1), 31. / basis + 11 + 31. / basis);
  }

  private void assertFractionEquals(final DayCount convention, final ZonedDateTime d1, final ZonedDateTime d2, final double frac) {
    assertEquals(frac, convention.getDayCountFraction(d1, d2), EPS);
  }

  private void testOneYearNoLeapYears(final DayCount convention) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2002, 1, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2003, 1, 1);
    assertFractionEquals(convention, d1, d2, 1);
  }

  private void testOneYearNoLeapYears(final DayCount convention, final double x) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2002, 1, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2003, 1, 1);
    assertFractionEquals(convention, d1, d2, 1 + x);
  }

  private void testOneYearOneLeapYear(final DayCount convention) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2004, 1, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2005, 1, 1);
    assertFractionEquals(convention, d1, d2, 1);
  }

  private void testOneYearOneLeapYear(final DayCount convention, final double x) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2004, 1, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2005, 1, 1);
    assertFractionEquals(convention, d1, d2, 1 + x);
  }

  private void testFourMonths(final DayCount convention, final double fraction) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2008, 2, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2008, 6, 1);
    assertFractionEquals(convention, d1, d2, fraction);
  }

  private void testOneDayNoLeapYear(final DayCount convention) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2009, 9, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2009, 9, 2);
    assertFractionEquals(convention, d1, d2, 1. / convention.getBasis(d1));
  }

  private void testOneDayNoLeapYear(final DayCount convention, final double fraction) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2009, 9, 1);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2009, 9, 2);
    assertFractionEquals(convention, d1, d2, fraction);
  }

  private void testOneDayLeapYear(final DayCount convention, final double fraction) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2008, 2, 29);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2008, 3, 1);
    assertFractionEquals(convention, d1, d2, fraction);
  }

  private void testFirstDayThirtyDayAdjustment(final DayCount convention, final double fraction) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2009, 9, 30);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2009, 10, 31);
    assertFractionEquals(convention, d1, d2, fraction);
  }

  private void testSecondDayThirtyDayAdjustment(final DayCount convention, final double fraction) {
    final ZonedDateTime d1 = DateUtil.getUTCDate(2009, 9, 28);
    final ZonedDateTime d2 = DateUtil.getUTCDate(2009, 10, 31);
    assertFractionEquals(convention, d1, d2, fraction);
  }

}

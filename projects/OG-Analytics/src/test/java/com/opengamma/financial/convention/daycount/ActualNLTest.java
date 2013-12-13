/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ActualNLTest {
  private static final DayCount ACTUAL_NL = DayCountFactory.of("Actual/NL");
  private static final double EPS = 1e-12;

  @Test
  public void testSameYearNoLeapYear() {
    final LocalDate firstDate = LocalDate.of(2011, 1, 31);
    final LocalDate secondDate = LocalDate.of(2011, 3, 15);
    assertEquals(43. / 365, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
  }

  @Test
  public void testSameYearLeapYear() {
    LocalDate firstDate = LocalDate.of(2012, 1, 31);
    LocalDate secondDate = LocalDate.of(2012, 2, 15);
    assertEquals(15. / 365, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
    secondDate = LocalDate.of(2012, 3, 15);
    assertEquals(43. / 365, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
    firstDate = LocalDate.of(2012, 3, 31);
    secondDate = LocalDate.of(2012, 5, 15);
    assertEquals(45. / 365, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
    firstDate = LocalDate.of(2012, 2, 28);
    secondDate = LocalDate.of(2012, 2, 29);
    assertEquals(0, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
    firstDate = LocalDate.of(2012, 2, 29);
    secondDate = LocalDate.of(2012, 3, 15);
    assertEquals(15 / 365., ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
  }

  @Test
  public void testDifferentYearNoLeapDays() {
    final LocalDate firstDate = LocalDate.of(2010, 1, 31);
    LocalDate secondDate = LocalDate.of(2011, 1, 31);
    assertEquals(1, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
    secondDate = LocalDate.of(2012, 1, 31);
    assertEquals(2, ACTUAL_NL.getDayCountFraction(firstDate, secondDate), EPS);
  }

  @Test
  public void testDifferentYearLeapDays() {
    LocalDate firstDate = LocalDate.of(2012, 1, 31);
    LocalDate secondDate = LocalDate.of(2013, 1, 31);
    assertEquals(ACTUAL_NL.getDayCountFraction(firstDate, secondDate), 1, EPS);
    secondDate = LocalDate.of(2016, 1, 31);
    assertEquals(ACTUAL_NL.getDayCountFraction(firstDate, secondDate), 4, EPS);
    secondDate = LocalDate.of(2017, 1, 31);
    assertEquals(ACTUAL_NL.getDayCountFraction(firstDate, secondDate), 5, EPS);
    firstDate = LocalDate.of(2012, 7, 31);
    secondDate = LocalDate.of(2013, 7, 31);
    assertEquals(ACTUAL_NL.getDayCountFraction(firstDate, secondDate), 1, EPS);
    secondDate = LocalDate.of(2016, 7, 31);
    assertEquals(ACTUAL_NL.getDayCountFraction(firstDate, secondDate), 4, EPS);
    secondDate = LocalDate.of(2017, 7, 31);
    assertEquals(ACTUAL_NL.getDayCountFraction(firstDate, secondDate), 5, EPS);
  }
}

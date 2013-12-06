/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.datasets.CalendarECBSettlements;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the next or n-th (non-)good business date from a starting point.
 */
@Test(groups = TestGroup.UNIT)
public class CalendarBusinessDateUtilsTest {

  private static final Calendar ECB = new CalendarECBSettlements();

  @Test
  public void nextGoodBusinessDate() {
    LocalDate startDate = LocalDate.of(2013, 12, 4);
    LocalDate nextGoodComputed = CalendarBusinessDateUtils.nextGoodBusinessDate(startDate, ECB);
    assertEquals("CalendarBusinessDateUtils: nextGoodBusinessDate", startDate, nextGoodComputed);
    LocalDate startDate2 = LocalDate.of(2013, 12, 11);
    LocalDate nextGoodComputed2 = CalendarBusinessDateUtils.nextGoodBusinessDate(startDate2, ECB);
    LocalDate nextGoodExpected2 = startDate2.plusDays(1);
    assertEquals("CalendarBusinessDateUtils: nextGoodBusinessDate", nextGoodExpected2, nextGoodComputed2);
  }

  @Test
  public void nextNonGoodBusinessDate() {
    LocalDate startDate = LocalDate.of(2013, 12, 4);
    LocalDate nextNonGoodComputed = CalendarBusinessDateUtils.nextNonGoodBusinessDate(startDate, ECB);
    LocalDate nextNonGoodExpected = LocalDate.of(2013, 12, 11);
    assertEquals("CalendarBusinessDateUtils: nextNonGoodBusinessDate", nextNonGoodExpected, nextNonGoodComputed);
    LocalDate nextNonGoodComputed2 = CalendarBusinessDateUtils.nextNonGoodBusinessDate(nextNonGoodExpected, ECB);
    assertEquals("CalendarBusinessDateUtils: nextNonGoodBusinessDate", nextNonGoodExpected, nextNonGoodComputed2);
  }

  @Test
  public void nthGoodBusinessDate() {
    LocalDate startDate = LocalDate.of(2013, 12, 4);
    LocalDate firstGoodComputed = CalendarBusinessDateUtils.nthGoodBusinessDate(startDate, ECB, 1);
    assertEquals("CalendarBusinessDateUtils: nthGoodBusinessDate", startDate, firstGoodComputed);
    LocalDate startDate2 = LocalDate.of(2013, 12, 11);
    LocalDate firstGoodComputed2 = CalendarBusinessDateUtils.nthGoodBusinessDate(startDate2, ECB, 1);
    LocalDate firstGoodExpected2 = startDate2.plusDays(1);
    assertEquals("CalendarBusinessDateUtils: nthGoodBusinessDate", firstGoodExpected2, firstGoodComputed2);
    int number = 15;
    LocalDate nthGoodComputed = CalendarBusinessDateUtils.nthGoodBusinessDate(startDate, ECB, number);
    LocalDate nthGoodExpected = startDate.plusDays(number);
    assertEquals("CalendarBusinessDateUtils: nthGoodBusinessDate", nthGoodExpected, nthGoodComputed);
    int number2 = 45;
    LocalDate nthGoodComputed2 = CalendarBusinessDateUtils.nthGoodBusinessDate(startDate, ECB, number2);
    LocalDate nthGoodExpected2 = startDate.plusDays(number2 + 1);
    assertEquals("CalendarBusinessDateUtils: nthGoodBusinessDate", nthGoodExpected2, nthGoodComputed2);
  }

  @Test
  public void nthNonGoodBusinessDate() {
    LocalDate startDate = LocalDate.of(2013, 12, 4);
    LocalDate nextNonGood = CalendarBusinessDateUtils.nextNonGoodBusinessDate(startDate, ECB);
    LocalDate firstNonGoodComputed = CalendarBusinessDateUtils.nthNonGoodBusinessDate(startDate, ECB, 1);
    assertEquals("CalendarBusinessDateUtils: nthNonGoodBusinessDate", nextNonGood, firstNonGoodComputed);
    int number = 5;
    LocalDate nthNonGoodExpected = LocalDate.of(2014, 4, 9);
    LocalDate nthNonGoodComputed = CalendarBusinessDateUtils.nthNonGoodBusinessDate(startDate, ECB, number);
    assertEquals("CalendarBusinessDateUtils: nthNonGoodBusinessDate", nthNonGoodExpected, nthNonGoodComputed);
  }

}

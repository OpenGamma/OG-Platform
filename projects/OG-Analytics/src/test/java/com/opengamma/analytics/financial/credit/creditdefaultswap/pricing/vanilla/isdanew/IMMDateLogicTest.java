/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.isIMMDate;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

/**
 * 
 */
public class IMMDateLogicTest {

  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5), Period.ofYears(10) };

  @Test
  void isIMMTest() {
    LocalDate date = LocalDate.of(2013, Month.MARCH, 20);
    assertTrue(isIMMDate(date));

    date = LocalDate.of(2013, Month.APRIL, 20);
    assertFalse(isIMMDate(date));

    date = LocalDate.of(2013, Month.DECEMBER, 23);
    assertFalse(isIMMDate(date));
  }

  @Test
  public void onIMMDateTest() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 20);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = getIMMDateSet(stepin, TENORS);
    assertEquals(dates.length, TENORS.length);
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), dates[0]);
  }

  @Test
  public void immSetTest() {
    final LocalDate date = LocalDate.of(2013, Month.MARCH, 20);
    final int n = 5;
    final LocalDate[] immDates = getIMMDateSet(date, n);
    assertTrue(immDates.length == n);
    assertEquals(date, immDates[0]);
    assertEquals(LocalDate.of(2013, Month.JUNE, 20), immDates[1]);
    assertEquals(LocalDate.of(2013, Month.SEPTEMBER, 20), immDates[2]);
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), immDates[3]);
    assertEquals(LocalDate.of(2014, Month.MARCH, 20), immDates[4]);
  }

  @Test
  public void nonIMMDateTest() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 26);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = getIMMDateSet(stepin, TENORS);
    assertEquals(dates.length, TENORS.length);
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), dates[0]);
  }

  @Test
  public void stepinIMMDateTest() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 19);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = getIMMDateSet(stepin, TENORS);
    assertEquals(dates.length, TENORS.length);
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), dates[0]);
  }

  @Test
  public void stepinIMMDateM1Test() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 18);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = getIMMDateSet(stepin, TENORS);
    assertEquals(dates.length, TENORS.length);
    assertEquals(LocalDate.of(2013, Month.SEPTEMBER, 20), dates[0]);
  }

  @Test
  public void nextIMMTest() {
    LocalDate stepin = LocalDate.of(2011, Month.JUNE, 21);
    LocalDate nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(2011, Month.SEPTEMBER, 20), nextIMM);

    stepin = LocalDate.of(2011, Month.JUNE, 20);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(2011, Month.SEPTEMBER, 20), nextIMM);

    stepin = LocalDate.of(2011, Month.DECEMBER, 20);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(2012, Month.MARCH, 20), nextIMM);

    stepin = LocalDate.of(2011, Month.JUNE, 18);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(2011, Month.JUNE, 20), nextIMM);

    stepin = LocalDate.of(1976, Month.JULY, 30);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(1976, Month.SEPTEMBER, 20), nextIMM);

    stepin = LocalDate.of(1977, Month.FEBRUARY, 13);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(1977, Month.MARCH, 20), nextIMM);

    stepin = LocalDate.of(2013, Month.MARCH, 1);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(2013, Month.MARCH, 20), nextIMM);

    stepin = LocalDate.of(2013, Month.DECEMBER, 25);
    nextIMM = getNextIMMDate(stepin);
    assertEquals(LocalDate.of(2014, Month.MARCH, 20), nextIMM);

  }

  @Test
  public void prevIMMTest() {
    LocalDate stepin = LocalDate.of(2011, Month.JUNE, 21);
    LocalDate prevIMM = getPrevIMMDate(stepin);
    assertEquals(LocalDate.of(2011, Month.JUNE, 20), prevIMM);

    stepin = LocalDate.of(2011, Month.JUNE, 20);
    prevIMM = getPrevIMMDate(stepin);
    assertEquals(LocalDate.of(2011, Month.MARCH, 20), prevIMM);

    prevIMM = getPrevIMMDate(prevIMM);
    assertEquals(LocalDate.of(2010, Month.DECEMBER, 20), prevIMM);

    stepin = LocalDate.of(2011, Month.JUNE, 18);
    prevIMM = getPrevIMMDate(stepin);
    assertEquals(LocalDate.of(2011, Month.MARCH, 20), prevIMM);

    stepin = LocalDate.of(1976, Month.JULY, 30);
    prevIMM = getPrevIMMDate(stepin);
    assertEquals(LocalDate.of(1976, Month.JUNE, 20), prevIMM);

    stepin = LocalDate.of(1977, Month.FEBRUARY, 13);
    prevIMM = getPrevIMMDate(stepin);
    assertEquals(LocalDate.of(1976, Month.DECEMBER, 20), prevIMM);

    stepin = LocalDate.of(2013, Month.MARCH, 1);
    prevIMM = getPrevIMMDate(stepin);
    assertEquals(LocalDate.of(2012, Month.DECEMBER, 20), prevIMM);

  }
}

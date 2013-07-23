/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

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
  public void onIMMDateTest() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 20);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = IMMDateLogic.getIMMDateSet(stepin, TENORS);
    for (final LocalDate date : dates) {
      System.out.println(date);
    }
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), dates[0]);
  }

  @Test
  public void nonIMMDateTest() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 26);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = IMMDateLogic.getIMMDateSet(stepin, TENORS);
    for (final LocalDate date : dates) {
      System.out.println(date);
    }
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), dates[0]);
  }

  @Test
  public void stepinIMMDateTest() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 19);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = IMMDateLogic.getIMMDateSet(stepin, TENORS);
    for (final LocalDate date : dates) {
      System.out.println(date);
    }
    assertEquals(LocalDate.of(2013, Month.DECEMBER, 20), dates[0]);
  }

  @Test
  public void stepinIMMDateM1Test() {
    final LocalDate today = LocalDate.of(2013, Month.MARCH, 18);
    final LocalDate stepin = today.plusDays(1);
    final LocalDate[] dates = IMMDateLogic.getIMMDateSet(stepin, TENORS);
    for (final LocalDate date : dates) {
      System.out.println(date);
    }
    assertEquals(LocalDate.of(2013, Month.SEPTEMBER, 20), dates[0]);
  }

}

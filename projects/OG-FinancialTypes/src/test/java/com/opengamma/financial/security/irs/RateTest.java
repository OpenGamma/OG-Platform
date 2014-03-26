/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.irs;


import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;


@Test(groups = TestGroup.UNIT)
public class RateTest {

  private final static double TOLERACE = 1e-10;

  @SuppressWarnings("deprecation")
  @Test
  public void testConstantRate() throws Exception {
    Rate rate = new Rate(1);
    Assert.assertEquals(1., rate.getInitialRate(), TOLERACE);
    Assert.assertEquals(1., rate.getRate(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(1., rate.getRate(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(1., rate.getRate(LocalDate.now()), TOLERACE);
  }

  @Test
  public void testAmortizingRate() throws Exception {
    LocalDate start = LocalDate.now();
    LocalDate[] dates = new LocalDate[] {start, start.plusYears(1), start.plusYears(2), start.plusYears(3)};
    double[] rates = new double[] {6., 5., 4., 3.};
    Rate.ShiftType[] shiftTypes = new Rate.ShiftType[] {Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT};
    Rate amortizing = Rate.builder().dates(dates).rates(rates).types(shiftTypes).build();
    Assert.assertEquals(6., amortizing.getInitialRate(), TOLERACE);
    Assert.assertEquals(3., amortizing.getRate(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(6., amortizing.getRate(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(6., amortizing.getRate(start), TOLERACE);
    Assert.assertEquals(6., amortizing.getRate(start.plusMonths(11)), TOLERACE);
    Assert.assertEquals(5., amortizing.getRate(start.plusYears(1)), TOLERACE);
    Assert.assertEquals(4., amortizing.getRate(start.plusYears(2)), TOLERACE);
    Assert.assertEquals(3., amortizing.getRate(start.plusYears(3)), TOLERACE);
    Assert.assertEquals(4., amortizing.getRate(start.plusYears(3).minusDays(1)), TOLERACE);
  }

  @Test
  public void testAdditiveShifts() throws Exception {
    LocalDate start = LocalDate.now();
    LocalDate[] dates = new LocalDate[] {start, start.plusYears(1), start.plusYears(2), start.plusYears(3)};
    double[] rates = new double[] {1e6d, -2.5e5d, -2.5e5d, -2.5e5d};
    Rate.ShiftType[] shiftTypes = new Rate.ShiftType[] {Rate.ShiftType.OUTRIGHT, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE};
    Rate amortizing = Rate.builder().dates(dates).rates(rates).types(shiftTypes).build();
    Assert.assertEquals(1e6, amortizing.getInitialRate(), TOLERACE);
    Assert.assertEquals(2.5e5d, amortizing.getRate(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getRate(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getRate(start), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getRate(start.plusMonths(11)), TOLERACE);
    Assert.assertEquals(7.5e5d, amortizing.getRate(start.plusYears(1)), TOLERACE);
    Assert.assertEquals(5e5d, amortizing.getRate(start.plusYears(2)), TOLERACE);
    Assert.assertEquals(2.5e5d, amortizing.getRate(start.plusYears(3)), TOLERACE);
    Assert.assertEquals(5e5d, amortizing.getRate(start.plusYears(3).minusDays(1)), TOLERACE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDeltaInitialNotional() throws Exception {
    LocalDate start = LocalDate.now();
    LocalDate[] dates = new LocalDate[] {start, start.plusYears(1), start.plusYears(2), start.plusYears(3)};
    double[] rates = new double[] {1e6d, -2.5e5d, -2.5e5d, -2.5e5d};
    Rate.ShiftType[] shiftTypes = new Rate.ShiftType[] {Rate.ShiftType.DELTA, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE};
    Rate.builder().dates(dates).rates(rates).types(shiftTypes).build();
  }

}

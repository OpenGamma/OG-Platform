/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.irs;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;


@Test(groups = TestGroup.UNIT)
public class InterestRateSwapNotionalTest {

  private final static double TOLERACE = 1e-10;

  @SuppressWarnings("deprecation")
  @Test
  public void testConstantNotional() throws Exception {
    InterestRateSwapNotional notional = InterestRateSwapNotional.of(Currency.GBP, 1e6);
    Assert.assertEquals(1e6, notional.getInitialAmount(), TOLERACE);
    Assert.assertEquals(1e6, notional.getAmount(), TOLERACE);
    Assert.assertEquals(1e6, notional.getAmount(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(1e6, notional.getAmount(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(1e6, notional.getAmount(LocalDate.now()), TOLERACE);
    Assert.assertEquals(Currency.GBP, notional.getCurrency());
  }

  @Test
  public void testAmortizingNotional() throws Exception {
    LocalDate start = LocalDate.now();
    List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    List<Double> notionals = Lists.newArrayList(1e6, 1e5, 1e4, 1e3);
    List<Rate.ShiftType> shiftTypes = Lists.newArrayList(Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT);
    InterestRateSwapNotional amortizing = InterestRateSwapNotional.of(Currency.GBP, dates, notionals, shiftTypes);
    Assert.assertEquals(1e6, amortizing.getInitialAmount(), TOLERACE);
    Assert.assertEquals(1e3, amortizing.getAmount(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(start), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(start.plusMonths(11)), TOLERACE);
    Assert.assertEquals(1e5, amortizing.getAmount(start.plusYears(1)), TOLERACE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(2)), TOLERACE);
    Assert.assertEquals(1e3, amortizing.getAmount(start.plusYears(3)), TOLERACE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(3).minusDays(1)), TOLERACE);
    Assert.assertEquals(Currency.GBP, amortizing.getCurrency());
  }

  @Test
  public void testAmortizingNotional2() throws Exception {
    LocalDate start = LocalDate.now();
    List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    List<Double> notionals = Lists.newArrayList(1e6, 1e5, 1e4, 1e3);
    InterestRateSwapNotional amortizing = InterestRateSwapNotional.of(Currency.GBP, dates, notionals);
    Assert.assertEquals(1e6, amortizing.getInitialAmount(), TOLERACE);
    Assert.assertEquals(1e3, amortizing.getAmount(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(start), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(start.plusMonths(11)), TOLERACE);
    Assert.assertEquals(1e5, amortizing.getAmount(start.plusYears(1)), TOLERACE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(2)), TOLERACE);
    Assert.assertEquals(1e3, amortizing.getAmount(start.plusYears(3)), TOLERACE);
    Assert.assertEquals(1e4, amortizing.getAmount(start.plusYears(3).minusDays(1)), TOLERACE);
    Assert.assertEquals(Currency.GBP, amortizing.getCurrency());
  }

  @Test
  public void testAdditiveShifts() throws Exception {
    LocalDate start = LocalDate.now();
    List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    List<Double> notionals = Lists.newArrayList(1e6d, -2.5e5d, -2.5e5d, -2.5e5d);
    List<Rate.ShiftType> shiftTypes = Lists.newArrayList(Rate.ShiftType.OUTRIGHT, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE, Rate.ShiftType.ADDITIVE);
    InterestRateSwapNotional amortizing = InterestRateSwapNotional.of(Currency.GBP, dates, notionals, shiftTypes);
    Assert.assertEquals(1e6, amortizing.getInitialAmount(), TOLERACE);
    Assert.assertEquals(2.5e5d, amortizing.getAmount(LocalDate.MAX), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(LocalDate.MIN), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(start), TOLERACE);
    Assert.assertEquals(1e6, amortizing.getAmount(start.plusMonths(11)), TOLERACE);
    Assert.assertEquals(7.5e5d, amortizing.getAmount(start.plusYears(1)), TOLERACE);
    Assert.assertEquals(5e5d, amortizing.getAmount(start.plusYears(2)), TOLERACE);
    Assert.assertEquals(2.5e5d, amortizing.getAmount(start.plusYears(3)), TOLERACE);
    Assert.assertEquals(5e5d, amortizing.getAmount(start.plusYears(3).minusDays(1)), TOLERACE);
    Assert.assertEquals(Currency.GBP, amortizing.getCurrency());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDeltaInitialNotional() throws Exception {
    LocalDate start = LocalDate.now();
    List<LocalDate> dates = Lists.newArrayList(start, start.plusYears(1), start.plusYears(2), start.plusYears(3));
    List<Double> notionals = Lists.newArrayList(1e6, 1e5, 1e4, 1e3);
    List<Rate.ShiftType> shiftTypes = Lists.newArrayList(Rate.ShiftType.DELTA, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT, Rate.ShiftType.OUTRIGHT);
    InterestRateSwapNotional.of(Currency.GBP, dates, notionals, shiftTypes);
  }

}

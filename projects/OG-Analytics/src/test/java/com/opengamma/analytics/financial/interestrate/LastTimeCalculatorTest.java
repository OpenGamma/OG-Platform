/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class LastTimeCalculatorTest {
  private static LastTimeCalculator LDC = LastTimeCalculator.getInstance();
  private static final Currency CUR = Currency.EUR;

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final Cash cash = new Cash(CUR, 1 / 365.0, t, 100, 0.0445, 5.0 / 365, "t");
    assertEquals(t, cash.accept(LDC), 1e-12);
  }

  @Test
  public void testFRA() {
    final double paymentTime = 0.5;
    final double paymentYearFraction = 30. / 360;
    final double fixingTime = paymentTime - 2. / 365;
    final double fixingPeriodStartTime = paymentTime;
    final double fixingPeriodEndTime = 7. / 12;
    final double fixingYearFraction = 31. / 365;
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, "Funding", paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction,
        0.05, "Forward");

    assertEquals(fixingPeriodEndTime, fra.accept(LDC), 1e-12);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final double refrencePrice = 0.0;
    final InterestRateFuture ir = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, refrencePrice, 1, paymentAccrualFactor, 1,
        "S", "Funding", "Forward");
    assertEquals(fixingPeriodEndTime, ir.accept(LDC), 1e-12);
  }

  @Test
  public void testFixedCouponAnnuity() {
    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CUR, new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 1.0, 1.0, "", true);
    assertEquals(10, annuity.accept(LDC), 1e-12);
  }

  @Test
  public void testBond() {
    final double mat = 1.0;
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, mat, 1.0, "a") });
    final AnnuityCouponFixed coupon = new AnnuityCouponFixed(CUR, new double[] {0.5, mat }, 0.03, "a", false);
    final BondFixedSecurity bond = new BondFixedSecurity(nominal, coupon, 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, "a", "b");
    assertEquals(mat, bond.accept(LDC), 1e-12);
  }

  @Test
  public void testDepositZero() {
    final double endTime = 0.03;
    final DepositZero deposit = new DepositZero(Currency.USD, 0, endTime, 100, 100, 0.25, new ContinuousInterestRate(0.03), 2, "FUNDING");
    assertEquals(deposit.accept(LDC), endTime, 0);
  }
}

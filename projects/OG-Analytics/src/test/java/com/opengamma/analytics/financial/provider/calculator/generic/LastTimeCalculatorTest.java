/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LastTimeCalculatorTest {
  private static LastTimeCalculator LDC = LastTimeCalculator.getInstance();
  private static final Currency CUR = Currency.EUR;

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final Cash cash = new Cash(CUR, 1 / 365.0, t, 100, 0.0445, 5.0 / 365);
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
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true, "Ibor");
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR,
                                                              paymentTime,
                                                              paymentYearFraction,
                                                              1,
                                                              index,
                                                              fixingTime,
                                                              fixingPeriodStartTime,
                                                              fixingPeriodEndTime,
                                                              fixingYearFraction,
                                                              0.05);

    assertEquals(fixingPeriodEndTime, fra.accept(LDC), 1e-12);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true, "Ibor");
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final double refrencePrice = 0.0;
    final InterestRateFutureSecurity sec = new InterestRateFutureSecurity(lastTradingTime,
                                                                          iborIndex,
                                                                          fixingPeriodStartTime,
                                                                          fixingPeriodEndTime,
                                                                          fixingPeriodAccrualFactor,
                                                                          1.0,
                                                                          paymentAccrualFactor,
                                                                          "S");
    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(sec, refrencePrice, 1);
    assertEquals(fixingPeriodEndTime, ir.accept(LDC), 1e-12);
  }

  @Test
  public void testFixedCouponAnnuity() {
    final AnnuityCouponFixed annuity = 
        new AnnuityCouponFixed(CUR, new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 1.0, 1.0, true);
    assertEquals(10, annuity.accept(LDC), 1e-12);
  }

  @Test
  public void testBond() {
    final double mat = 1.0;
    final AnnuityPaymentFixed nominal =
        new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, mat, 1.0) });
    final AnnuityCouponFixed coupon =
        new AnnuityCouponFixed(CUR, new double[] {0.5, mat }, 0.03, false);
    final BondFixedSecurity bond =
        new BondFixedSecurity(nominal, coupon, 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, "Issuer");
    assertEquals(mat, bond.accept(LDC), 1e-12);
  }

  @Test
  public void testDepositZero() {
    final double endTime = 0.03;
    final DepositZero deposit =
        new DepositZero(Currency.USD, 0, endTime, 100, 100, 0.25, new ContinuousInterestRate(0.03), 2);
    assertEquals(deposit.accept(LDC), endTime, 0);
  }

  @Test
  public void testForex() {
    final double t = 0.124;
    final Forex fx = new Forex(new PaymentFixed(Currency.AUD, t, -100), new PaymentFixed(Currency.USD, t, 100));
    assertEquals(fx.accept(LDC), t, 0);
  }
}

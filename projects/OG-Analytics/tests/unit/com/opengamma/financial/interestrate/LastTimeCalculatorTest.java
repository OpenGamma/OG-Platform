/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class LastTimeCalculatorTest {
  private static LastTimeCalculator LDC = LastTimeCalculator.getInstance();
  private static final Currency CUR = Currency.USD;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final Cash cash = new Cash(CUR, t, 100, 0.0445, 1 / 365.0, 5.0 / 365, "t");
    assertEquals(t, LDC.visit(cash), 1e-12);
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

    assertEquals(fixingPeriodEndTime, LDC.visit(fra), 1e-12);
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
    final InterestRateFuture ir = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, refrencePrice, 1, paymentAccrualFactor,
        "S", "Funding", "Forward");
    assertEquals(fixingPeriodEndTime, LDC.visit(ir, fixingPeriodStartTime), 1e-12); // passing in fixingDate is just to show that anything can be passed in - it is ignored
  }

  @Test
  public void testFixedCouponAnnuity() {
    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CUR, new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, 1.0, "", true);
    assertEquals(10, LDC.visit(annuity), 1e-12);
  }

  @Test
  public void testForwardLiborAnnuity() {
    final int n = 15;
    final double alpha = 0.245;
    final double yearFrac = 0.25;
    final double spread = 0.01;
    final double[] paymentTimes = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final double[] yearFracs = new double[n];
    final double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * alpha + 0.1;
      paymentTimes[i] = (i + 1) * alpha;
      indexMaturity[i] = paymentTimes[i] + 0.1;
      yearFracs[i] = yearFrac;
      spreads[i] = spread;
    }
    final AnnuityCouponIbor annuity = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, INDEX, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, Math.E, "Bill", "Ben", true);
    assertEquals(n * alpha + 0.1, LDC.visit(annuity), 1e-12);
  }

  @Test
  public void testBond() {
    double mat = 1.0;
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, mat, 1.0, "a")});
    final AnnuityCouponFixed coupon = new AnnuityCouponFixed(CUR, new double[] {0.5, mat}, 0.03, "a", false);
    final BondFixedSecurity bond = new BondFixedSecurity(nominal, coupon, 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, "a", "b");
    assertEquals(mat, LDC.visit(bond), 1e-12);
  }

  @Test
  public void testFixedFloatSwap() {
    final int n = 20;
    final double[] fixedPaymentTimes = new double[n];
    final double[] floatPaymentTimes = new double[2 * n];

    for (int i = 0; i < n * 2; i++) {
      if (i % 2 == 0) {
        fixedPaymentTimes[i / 2] = (i + 2) * 0.25;
      }
      floatPaymentTimes[i] = (i + 1) * 0.25;
    }
    final double swapRate = 0.045;

    final Swap<?, ?> swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, INDEX, swapRate, "", "", true);
    assertEquals(n * 0.5, LDC.visit(swap), 1e-12);
  }

  @Test
  public void testTenorSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] spreads = new double[n];
    final double[] yearFracs = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final double forward = 0.003;
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = forward;
      yearFracs[i] = tau;
    }

    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, INDEX, indexMaturity, yearFracs, 1.0, "", "", true);
    final GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, INDEX, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, "", "", false);

    final Swap<?, ?> swap = new TenorSwap<CouponIbor>(payLeg, receiveLeg);

    assertEquals(n * tau, LDC.visit(swap, swap), 1e-12);// passing in swap twice is just to show that anything can be passed in -second case is it is ignored

  }

}

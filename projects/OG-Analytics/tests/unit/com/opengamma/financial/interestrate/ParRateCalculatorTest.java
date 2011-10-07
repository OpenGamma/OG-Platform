/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

/**
 * 
 */
public class ParRateCalculatorTest {
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.USD;

  static {
    YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.05));
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = new YieldCurve(ConstantDoublesCurve.from(0.0));
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;

    Cash cash = new Cash(CUR, t, 1, 0, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final double rate = PRC.visit(cash, CURVES);
    cash = new Cash(CUR, t, 1, rate, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.visit(cash, CURVES), 1e-12);
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
    ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingYearFraction, 0, FIVE_PC_CURVE_NAME);
    final double rate = PRC.visit(fra, CURVES);
    fra = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, rate,
        FIVE_PC_CURVE_NAME);
    assertEquals(0.0, PVC.visit(fra, CURVES), 1e-12);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/360"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.453;
    final double fixingPeriodStartTime = lastTradingTime;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double notional = 1000000;
    final double paymentAccrualFactor = 0.25;
    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
    final String name = "name";

    final InterestRateFuture ir = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice,
        notional, paymentAccrualFactor, name, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
    final double rate = PRC.visit(ir, CURVES);
    final double price = 1 - rate;
    //final InterestRateFutureTransaction traded = new InterestRateFutureTransaction(ir, 1, price);
    double pvExpected = price * notional * paymentAccrualFactor;
    double pv = PVC.visit(ir, CURVES);
    assertEquals(pvExpected, pv, 1e-12);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;

    final double[] yearFracs = new double[n];
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);
      yearFracs[i] = yearFrac;
    }
    Bond bond = new Bond(CUR, paymentTimes, 0.0, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    final double rate = PRC.visit(bond, CURVES);
    bond = new Bond(CUR, paymentTimes, rate, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    assertEquals(1.0, PVC.visit(bond, CURVES), 1e-12);
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

    Swap<?, ?> swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, 0.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    final double rate = PRC.visit(swap, CURVES);
    swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, rate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    assertEquals(0.0, PVC.visit(swap, CURVES), 1e-12);
  }

  @Test
  public void testBasisSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] yearFracs = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      yearFracs[i] = tau;
    }

    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FIVE_PC_CURVE_NAME, ZERO_PC_CURVE_NAME, true);
    GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, false);

    Swap<?, ?> swap = new TenorSwap<CouponIbor>(payLeg, receiveLeg);
    final double rate = PRC.visit(swap, CURVES);
    final double[] spreads = new double[n];
    for (int i = 0; i < n; i++) {
      spreads[i] = rate;
    }
    receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, false);
    swap = new TenorSwap<CouponIbor>(payLeg, receiveLeg);
    assertEquals(0.0, PVC.visit(swap, CURVES), 1e-12);
  }

}

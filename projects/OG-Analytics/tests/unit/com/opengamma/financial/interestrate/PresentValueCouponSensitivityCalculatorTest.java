/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class PresentValueCouponSensitivityCalculatorTest {
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PVCSC = PresentValueCouponSensitivityCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final double DELTA = 1e-5;
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
    final double r = 0.0456;
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    final Cash cash = new Cash(CUR, t, 1, r, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final Cash cashUp = new Cash(CUR, t, 1, r + DELTA, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final Cash cashDown = new Cash(CUR, t, 1, r - DELTA, tradeTime, yearFrac, FIVE_PC_CURVE_NAME);
    final double pvUp = PVC.visit(cashUp, CURVES);
    final double pvDown = PVC.visit(cashDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    PVCSC.visit(cash, CURVES);

    assertEquals(temp, PVCSC.visit(cash, CURVES), 1e-10);
  }

  @Test
  public void testFRA() {
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double paymentTime = 0.5;
    final double fixingPeriodStartTime = paymentTime - 2. / 365;
    final double fixingPeriodEndTime = 7. / 12;
    final double fixingTime = fixingPeriodStartTime;
    final double fixingYearFraction = 31. / 365;
    final double paymentYearFraction = 30. / 360;
    final double rate = 0.06534;
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingYearFraction, rate, FIVE_PC_CURVE_NAME);
    final ForwardRateAgreement fraUp = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingYearFraction, rate + DELTA, FIVE_PC_CURVE_NAME);
    final ForwardRateAgreement fraDown = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingYearFraction, rate - DELTA, FIVE_PC_CURVE_NAME);
    final double pvUp = PVC.visit(fraUp, CURVES);
    final double pvDown = PVC.visit(fraDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    //TODO accuracy is off compared to old FRA definition
    assertEquals(temp, PVCSC.visit(fra, CURVES), 1e-5);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.468;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final double rate = 0.0356;
    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingPeriodAccrualFactor, 1, paymentAccrualFactor, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME), 1, 1 - rate);
    final InterestRateFutureTransaction irUp = new InterestRateFutureTransaction(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingPeriodAccrualFactor, 1, paymentAccrualFactor, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME), 1, 1 - rate - DELTA);
    final InterestRateFutureTransaction irDown = new InterestRateFutureTransaction(new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingPeriodAccrualFactor, 1, paymentAccrualFactor, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME), 1, 1 - rate + DELTA);
    final double pvUp = PVC.visit(irUp, CURVES);
    final double pvDown = PVC.visit(irDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    assertEquals(temp, PVCSC.visit(ir, CURVES), 1e-10);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.52;
    final double yearFrac = 0.5;

    final double coupon = 0.07;
    final double[] paymentTimes = new double[n];
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = tau * (i + 1);

    }

    final Bond bond = new Bond(CUR, paymentTimes, coupon, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    final Bond bondUp = new Bond(CUR, paymentTimes, coupon + DELTA, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    final Bond bondDown = new Bond(CUR, paymentTimes, coupon - DELTA, yearFrac, 0.0, FIVE_PC_CURVE_NAME);

    final double pvUp = PVC.visit(bondUp, CURVES);
    final double pvDown = PVC.visit(bondDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(bond, CURVES), 1e-10);
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
    final double swapRate = 0.04;

    final FixedFloatSwap swap = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    final FixedFloatSwap swapUp = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate + DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    final FixedFloatSwap swapDown = new FixedFloatSwap(CUR, fixedPaymentTimes, floatPaymentTimes, swapRate - DELTA, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);

    final double pvUp = PVC.visit(swapUp, CURVES);
    final double pvDown = PVC.visit(swapDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(swap, CURVES), 1e-10);
  }

  @Test
  public void testBasisSwap() {
    final int n = 20;
    final double tau = 0.25;
    final double[] paymentTimes = new double[n];
    final double[] spreads = new double[n];
    final double[] spreadsUp = new double[n];
    final double[] spreadsDown = new double[n];
    final double[] yearFracs = new double[n];
    final double[] indexFixing = new double[n];
    final double[] indexMaturity = new double[n];
    final double spread = 0.001;
    for (int i = 0; i < n; i++) {
      indexFixing[i] = i * tau;
      paymentTimes[i] = (i + 1) * tau;
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = spread;
      spreadsUp[i] = spread + DELTA;
      spreadsDown[i] = spread - DELTA;
      yearFracs[i] = tau;
    }

    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexMaturity, yearFracs, 1.0, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME, true);
    final GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, FIVE_PC_CURVE_NAME,
        ZERO_PC_CURVE_NAME, false);
    final GenericAnnuity<CouponIbor> receiveLegUp = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreadsUp, 1.0, FIVE_PC_CURVE_NAME,
        ZERO_PC_CURVE_NAME, false);
    final GenericAnnuity<CouponIbor> receiveLegDown = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreadsDown, 1.0, FIVE_PC_CURVE_NAME,
        ZERO_PC_CURVE_NAME, false);

    final TenorSwap<?> swap = new TenorSwap<CouponIbor>(payLeg, receiveLeg);
    final TenorSwap<?> swapUp = new TenorSwap<CouponIbor>(payLeg, receiveLegUp);
    final TenorSwap<?> swapDown = new TenorSwap<CouponIbor>(payLeg, receiveLegDown);
    final double pvUp = PVC.visit(swapUp, CURVES);
    final double pvDown = PVC.visit(swapDown, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;

    assertEquals(temp, PVCSC.visit(swap, CURVES), 1e-10);
  }

}

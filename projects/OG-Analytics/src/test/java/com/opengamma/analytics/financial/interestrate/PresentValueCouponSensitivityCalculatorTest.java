/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PresentValueCouponSensitivityCalculatorTest {
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCouponSensitivityCalculator PVCSC = PresentValueCouponSensitivityCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final double DELTA = 1e-5;
  private static final Currency CUR = Currency.EUR;

  static {
    YieldAndDiscountCurve curve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = YieldCurve.from(ConstantDoublesCurve.from(0.0));
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final double r = 0.0456;
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    final Cash cash = new Cash(CUR, tradeTime, t, 1, r, yearFrac, FIVE_PC_CURVE_NAME);
    final Cash cashUp = new Cash(CUR, tradeTime, t, 1, r + DELTA, yearFrac, FIVE_PC_CURVE_NAME);
    final Cash cashDown = new Cash(CUR, tradeTime, t, 1, r - DELTA, yearFrac, FIVE_PC_CURVE_NAME);
    final double pvUp = cashUp.accept(PVC, CURVES);
    final double pvDown = cashDown.accept(PVC, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    cash.accept(PVCSC, CURVES);

    assertEquals(temp, cash.accept(PVCSC, CURVES), 1e-10);
  }

  @Test
  public void testFRA() {
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true);
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
    final double pvUp = fraUp.accept(PVC, CURVES);
    final double pvDown = fraDown.accept(PVC, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    //TODO accuracy is off compared to old FRA definition
    assertEquals(temp, fra.accept(PVCSC, CURVES), 1e-5);
  }

  //  @Test
  //  public void testFutures() {
  //    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCounts.ACT_365,
  //        BusinessDayConventions.FOLLOWING, true);
  //    final double lastTradingTime = 1.468;
  //    final double fixingPeriodStartTime = 1.467;
  //    final double fixingPeriodEndTime = 1.75;
  //    final double fixingPeriodAccrualFactor = 0.267;
  //    final double paymentAccrualFactor = 0.25;
  //    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
  //    //  final double rate = 0.0356;
  //    final int quantity = 123;
  //    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice, 1, paymentAccrualFactor,
  //        quantity, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //    final InterestRateFutureTransaction irUp = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice - DELTA, 1,
  //        paymentAccrualFactor, quantity, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //    final InterestRateFutureTransaction irDown = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice + DELTA, 1,
  //        paymentAccrualFactor, quantity, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //    final double pvUp = irUp.accept(PVC, CURVES);
  //    final double pvDown = irDown.accept(PVC, CURVES);
  //    final double temp = (pvUp - pvDown) / 2 / DELTA;
  //    assertEquals(temp, ir.accept(PVCSC, CURVES), 1e-10);
  //  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.52;
    final double yearFrac = 0.5;
    final double coupon = 0.07;
    final CouponFixed[] coupons = new CouponFixed[n];
    final CouponFixed[] couponsUp = new CouponFixed[n];
    final CouponFixed[] couponsDown = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      coupons[i] = new CouponFixed(CUR, tau * (i + 1), FIVE_PC_CURVE_NAME, yearFrac, coupon);
      couponsUp[i] = new CouponFixed(CUR, tau * (i + 1), FIVE_PC_CURVE_NAME, yearFrac, coupon + DELTA);
      couponsDown[i] = new CouponFixed(CUR, tau * (i + 1), FIVE_PC_CURVE_NAME, yearFrac, coupon - DELTA);
    }
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, tau * n, 1, FIVE_PC_CURVE_NAME)});
    final BondFixedSecurity bond = new BondFixedSecurity(nominal, new AnnuityCouponFixed(coupons), 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FIVE_PC_CURVE_NAME, "S");
    final BondFixedSecurity bondUp = new BondFixedSecurity(nominal, new AnnuityCouponFixed(couponsUp), 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FIVE_PC_CURVE_NAME, "S");
    final BondFixedSecurity bondDown = new BondFixedSecurity(nominal, new AnnuityCouponFixed(couponsDown), 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FIVE_PC_CURVE_NAME, "S");
    final double pvUp = bondUp.accept(PVC, CURVES);
    final double pvDown = bondDown.accept(PVC, CURVES);
    final double temp = (pvUp - pvDown) / 2 / DELTA;
    assertEquals(temp, bond.accept(PVCSC, CURVES), 1e-10);
  }

}

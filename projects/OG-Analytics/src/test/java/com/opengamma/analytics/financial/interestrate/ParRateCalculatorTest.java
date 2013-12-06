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
public class ParRateCalculatorTest {
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
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
    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;

    Cash cash = new Cash(CUR, tradeTime, t, 1, 0, yearFrac, FIVE_PC_CURVE_NAME);
    final double rate = cash.accept(PRC, CURVES);
    cash = new Cash(CUR, tradeTime, t, 1, rate, yearFrac, FIVE_PC_CURVE_NAME);
    assertEquals(0.0, cash.accept(PVC, CURVES), 1e-12);
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
        BusinessDayConventions.FOLLOWING, true);
    ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingYearFraction, 0, FIVE_PC_CURVE_NAME);
    final double rate = fra.accept(PRC, CURVES);
    fra = new ForwardRateAgreement(CUR, paymentTime, FIVE_PC_CURVE_NAME, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, rate,
        FIVE_PC_CURVE_NAME);
    assertEquals(0.0, fra.accept(PVC, CURVES), 1e-12);
  }

  //  @Test
  //  public void testFutures() {
  //    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCounts.ACT_360,
  //        BusinessDayConventions.FOLLOWING, true);
  //    final double lastTradingTime = 1.453;
  //    final double fixingPeriodStartTime = lastTradingTime;
  //    final double fixingPeriodEndTime = 1.75;
  //    final double fixingPeriodAccrualFactor = 0.267;
  //    final double notional = 1000000;
  //    final double paymentAccrualFactor = 0.25;
  //    final int quantity = 123;
  //    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
  //    final String name = "name";
  //    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice, notional,
  //        paymentAccrualFactor, quantity, name, FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //    final double rate = ir.accept(PRC, CURVES);
  //    final double price = 1 - rate;
  //    //final InterestRateFutureTransaction traded = new InterestRateFutureTransaction(ir, 1, price);
  //    final double pvExpected = price * notional * paymentAccrualFactor * quantity;
  //    final double pv = ir.accept(PVC, CURVES);
  //    assertEquals(pvExpected, pv, 1e-12);
  //  }

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
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, 11, 1, FIVE_PC_CURVE_NAME)});
    AnnuityCouponFixed coupon = new AnnuityCouponFixed(CUR, new double[] {0.5, 1}, 0.03, FIVE_PC_CURVE_NAME, false);
    BondFixedSecurity bond = new BondFixedSecurity(nominal, coupon, 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FIVE_PC_CURVE_NAME, "S");
    final double rate = bond.accept(PRC, CURVES);
    coupon = new AnnuityCouponFixed(CUR, new double[] {0.5, 1}, rate, FIVE_PC_CURVE_NAME, false);
    bond = new BondFixedSecurity(nominal, coupon, 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FIVE_PC_CURVE_NAME, "S");
    assertEquals(1.0, bond.accept(PVC, CURVES), 1e-12);
  }

}

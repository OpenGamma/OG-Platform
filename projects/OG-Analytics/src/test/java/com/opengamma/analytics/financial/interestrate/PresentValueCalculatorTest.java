/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PresentValueCalculatorTest {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final String FIVE_PC_CURVE_NAME = "5%";
  private static final String FOUR_PC_CURVE_NAME = "4%";
  private static final String ZERO_PC_CURVE_NAME = "0%";
  private static final YieldCurveBundle CURVES;
  private static final Currency CUR = Currency.EUR;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");

  static {
    YieldAndDiscountCurve curve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    CURVES = new YieldCurveBundle();
    CURVES.setCurve(FIVE_PC_CURVE_NAME, curve);
    curve = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    CURVES.setCurve(FOUR_PC_CURVE_NAME, curve);
    curve = YieldCurve.from(ConstantDoublesCurve.from(0.0));
    CURVES.setCurve(ZERO_PC_CURVE_NAME, curve);
  }

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    double r = 1 / t * (1 / curve.getDiscountFactor(t) - 1);
    Cash cash = new Cash(CUR, 0, t, 1, r, t, FIVE_PC_CURVE_NAME);
    double pv = cash.accept(PVC, CURVES);
    assertEquals(0.0, pv, 1e-12);

    final double tradeTime = 2.0 / 365.0;
    final double yearFrac = 5.0 / 360.0;
    r = 1 / yearFrac * (curve.getDiscountFactor(tradeTime) / curve.getDiscountFactor(t) - 1);
    cash = new Cash(CUR, tradeTime, t, 1, r, yearFrac, FIVE_PC_CURVE_NAME);
    pv = cash.accept(PVC, CURVES);
    assertEquals(0.0, pv, 1e-12);
  }

  @Test
  public void testFRA() {
    final double paymentTime = 0.5;
    final double fixingPeriodEnd = 7. / 12.;
    String fundingCurveName = ZERO_PC_CURVE_NAME;
    final String forwardCurveName = FIVE_PC_CURVE_NAME;
    double paymentYearFraction = fixingPeriodEnd - paymentTime;
    final double notional = 1;
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true);
    double fixingTime = paymentTime;
    final double fixingPeriodStart = paymentTime;
    double fixingYearFraction = paymentYearFraction;
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = (forwardCurve.getDiscountFactor(paymentTime) / forwardCurve.getDiscountFactor(fixingPeriodEnd) - 1.0) * 12.0;
    ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, fundingCurveName, paymentYearFraction, notional, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction,
        rate, forwardCurveName);
    double pv = fra.accept(PVC, CURVES);
    assertEquals(0.0, pv, 1e-12);

    fixingTime = paymentTime - 2. / 365.;
    fixingYearFraction = 31. / 365;
    paymentYearFraction = 30. / 360;
    fundingCurveName = FIVE_PC_CURVE_NAME;
    final double forwardRate = (forwardCurve.getDiscountFactor(fixingPeriodStart) / forwardCurve.getDiscountFactor(fixingPeriodEnd) - 1) / fixingYearFraction;
    final double fv = (forwardRate - rate) * paymentYearFraction / (1 + forwardRate * paymentYearFraction);
    final double pv2 = fv * forwardCurve.getDiscountFactor(paymentTime);
    fra = new ForwardRateAgreement(CUR, paymentTime, fundingCurveName, paymentYearFraction, notional, index, fixingTime, fixingPeriodStart, fixingPeriodEnd, fixingYearFraction, rate, forwardCurveName);
    pv = fra.accept(PVC, CURVES);
    assertEquals(pv, pv2, 1e-12);
  }

  //  @Test
  //  public void testFutures() {
  //    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCounts.ACT_365,
  //        BusinessDayConventions.FOLLOWING, true);
  //    final double lastTradingTime = 1.473;
  //    final double fixingPeriodStartTime = 1.467;
  //    final double fixingPeriodEndTime = 1.75;
  //    final double fixingPeriodAccrualFactor = 0.267;
  //    final double paymentAccrualFactor = 0.25;
  //    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
  //    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
  //    final double rate = (curve.getDiscountFactor(fixingPeriodStartTime) / curve.getDiscountFactor(fixingPeriodEndTime) - 1.0) / fixingPeriodAccrualFactor;
  //    final double price = 1 - rate;
  //    final double notional = 1;
  //    final int quantity = 123;
  //    InterestRateFutureTransaction ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, referencePrice, notional, paymentAccrualFactor,
  //        quantity, "A", FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //    double pv = ir.accept(PVC, CURVES);
  //    assertEquals(price * notional * paymentAccrualFactor * quantity, pv, 1e-12);
  //    final double deltaPrice = 0.01;
  //    ir = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, deltaPrice, notional, paymentAccrualFactor, quantity, "A",
  //        FIVE_PC_CURVE_NAME, FIVE_PC_CURVE_NAME);
  //    pv = ir.accept(PVC, CURVES);
  //    assertEquals((price - deltaPrice) * notional * paymentAccrualFactor * quantity, pv, 1e-12);
  //  }

  @Test
  public void testFixedCouponAnnuity() {
    AnnuityCouponFixed annuityReceiver = new AnnuityCouponFixed(CUR, new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1.0, ZERO_PC_CURVE_NAME, false);

    double pv = annuityReceiver.accept(PVC, CURVES);
    assertEquals(10.0, pv, 1e-12);
    final int n = 15;
    final double alpha = 0.49;
    final double yearFrac = 0.51;
    final double[] paymentTimes = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFracs = new double[n];
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double rate = curve.getInterestRate(0.0);
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * alpha;
      coupons[i] = Math.exp((i + 1) * rate * alpha);
      yearFracs[i] = yearFrac;
    }
    annuityReceiver = new AnnuityCouponFixed(CUR, paymentTimes, Math.PI, rate, yearFracs, ZERO_PC_CURVE_NAME, false);
    pv = annuityReceiver.accept(PVC, CURVES);
    assertEquals(n * yearFrac * rate * Math.PI, pv, 1e-12);

    final AnnuityCouponFixed annuityPayer = new AnnuityCouponFixed(CUR, paymentTimes, Math.PI, rate, yearFracs, ZERO_PC_CURVE_NAME, true);
    assertEquals(pv, -annuityPayer.accept(PVC, CURVES), 1e-12);
  }

  @Test
  public void testBond() {
    final int n = 20;
    final double tau = 0.5;
    final double yearFrac = 180 / 365.0;
    final YieldAndDiscountCurve curve = CURVES.getCurve(FIVE_PC_CURVE_NAME);
    final double coupon = (1.0 / curve.getDiscountFactor(tau) - 1.0) / yearFrac;
    final CouponFixed[] coupons = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      coupons[i] = new CouponFixed(CUR, tau * (i + 1), FIVE_PC_CURVE_NAME, yearFrac, coupon);
    }
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, tau * n, 1, FIVE_PC_CURVE_NAME)});
    final BondFixedSecurity bond = new BondFixedSecurity(nominal, new AnnuityCouponFixed(coupons), 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, FIVE_PC_CURVE_NAME, "S");
    final double pv = bond.accept(PVC, CURVES);
    assertEquals(1.0, pv, 1e-12);
  }

  @Test
  public void testGenericAnnuity() {
    final double time = 3.4;
    final double amount = 34.3;
    final double coupon = 0.05;
    final double yearFrac = 0.5;
    final double resetTime = 2.9;
    final double notional = 56;

    final List<Payment> list = new ArrayList<>();
    double expected = 0.0;
    Payment temp = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);
    expected += amount * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    list.add(temp);
    temp = new CouponFixed(CUR, time, FIVE_PC_CURVE_NAME, yearFrac, notional, coupon);
    expected += notional * yearFrac * coupon * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    list.add(temp);
    temp = new CouponIborSpread(CUR, time, ZERO_PC_CURVE_NAME, yearFrac, notional, resetTime, INDEX, resetTime, time, yearFrac, 0.0, FIVE_PC_CURVE_NAME);
    expected += notional * (CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(resetTime) / CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time) - 1);
    list.add(temp);

    final Annuity<Payment> annuity = new Annuity<>(list, Payment.class, true);
    final double pv = annuity.accept(PVC, CURVES);
    assertEquals(expected, pv, 1e-12);
  }

  @Test
  public void testFixedPayment() {
    final double time = 1.23;
    final double amount = 4345.3;
    final PaymentFixed payment = new PaymentFixed(CUR, time, amount, FIVE_PC_CURVE_NAME);
    final double expected = amount * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    final double pv = payment.accept(PVC, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  public void testFixedCouponPayment() {
    final double time = 1.23;
    final double yearFrac = 0.56;
    final double coupon = 0.07;
    final double notional = 1000;

    final CouponFixed payment = new CouponFixed(CUR, time, ZERO_PC_CURVE_NAME, yearFrac, notional, coupon);
    final double expected = notional * yearFrac * coupon;
    final double pv = payment.accept(PVC, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

  @Test
  public void forwardLiborPayment() {
    final double time = 2.45;
    final double resetTime = 2.0;
    final double maturity = 2.5;
    final double paymentYF = 0.48;
    final double forwardYF = 0.5;
    final double spread = 0.04;
    final double notional = 4.53;

    CouponIborSpread payment = new CouponIborSpread(CUR, time, FIVE_PC_CURVE_NAME, paymentYF, notional, resetTime, INDEX, resetTime, maturity, forwardYF, spread, ZERO_PC_CURVE_NAME);
    double expected = notional * paymentYF * spread * CURVES.getCurve(FIVE_PC_CURVE_NAME).getDiscountFactor(time);
    double pv = payment.accept(PVC, CURVES);
    assertEquals(expected, pv, 1e-8);

    payment = new CouponIborSpread(CUR, time, ZERO_PC_CURVE_NAME, paymentYF, 1.0, resetTime, INDEX, resetTime, maturity, forwardYF, spread, FIVE_PC_CURVE_NAME);
    final double forward = (Math.exp(0.05 * (maturity - resetTime)) - 1) / forwardYF;

    expected = paymentYF * (forward + spread);
    pv = payment.accept(PVC, CURVES);
    assertEquals(expected, pv, 1e-8);
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueInflationCalculator;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.market.MarketBundle;
import com.opengamma.analytics.financial.interestrate.market.MarketDataSets;
import com.opengamma.analytics.financial.interestrate.market.PresentValueCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.method.market.SensitivityFiniteDifferenceMarket;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the present value and its sensitivities for zero-coupon with reference index on the first of the month.
 */
public class CouponInflationZeroCouponMonthlyDiscountingMethodTest {
  private static final MarketBundle MARKET = MarketDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[0]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IborIndex[] IBOR_INDEXES = MARKET.getIborIndexes().toArray(new IborIndex[0]);
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Calendar CALENDAR_EUR = EURIBOR3M.getCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_1MAY_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationZeroCouponMonthlyDefinition ZERO_COUPON_NO_DEFINITION = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
      INDEX_1MAY_2008, MONTH_LAG, false);
  private static final CouponInflationZeroCouponMonthly ZERO_COUPON_NO = ZERO_COUPON_NO_DEFINITION.toDerivative(PRICING_DATE, "not used");
  private static final CouponInflationZeroCouponMonthlyDefinition ZERO_COUPON_WITH_DEFINITION = CouponInflationZeroCouponMonthlyDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL, PRICE_INDEX_EUR,
      INDEX_1MAY_2008, MONTH_LAG, true);
  private static final CouponInflationZeroCouponMonthly ZERO_COUPON_WITH = ZERO_COUPON_WITH_DEFINITION.toDerivative(PRICING_DATE, "not used");
  private static final CouponInflationZeroCouponMonthlyDiscountingMethod METHOD = new CouponInflationZeroCouponMonthlyDiscountingMethod();
  private static final PresentValueInflationCalculator PVIC = PresentValueInflationCalculator.getInstance();

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNoNotional() {
    CurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_NO, MARKET);
    double df = MARKET.getCurve(ZERO_COUPON_NO.getCurrency()).getDiscountFactor(ZERO_COUPON_NO.getPaymentTime());
    double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_NO.getReferenceEndTime());
    double pvExpected = (finalIndex / INDEX_1MAY_2008 - 1) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation: Present value", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    CurrencyAmount pvMethod = METHOD.presentValue(ZERO_COUPON_NO, MARKET);
    CurrencyAmount pvCalculator = PVIC.visit(ZERO_COUPON_NO, MARKET);
    assertEquals("Zero-coupon inflation: Present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueWithNotional() {
    CurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_WITH, MARKET);
    double df = MARKET.getCurve(ZERO_COUPON_WITH.getCurrency()).getDiscountFactor(ZERO_COUPON_WITH.getPaymentTime());
    double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_WITH.getReferenceEndTime());
    double pvExpected = (finalIndex / INDEX_1MAY_2008) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation: Present value", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Test the present value curves sensitivity.
   */
  public void presentValueCurveSensitivityNoNotional() {
    final PresentValueCurveSensitivityMarket pvs = METHOD.presentValueCurveSensitivity(ZERO_COUPON_NO, MARKET);
    pvs.clean();
    final double deltaTolerancePrice = 1.0E+1;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 2. Discounting curve sensitivity
    final double[] nodeTimesDisc = new double[] {ZERO_COUPON_NO.getPaymentTime()};
    final double[] sensiDisc = SensitivityFiniteDifferenceMarket
        .curveSensitivity(ZERO_COUPON_NO, MARKET, ZERO_COUPON_NO.getCurrency(), nodeTimesDisc, deltaShift, METHOD, FiniteDifferenceType.CENTRAL);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDisc.length);
    final List<DoublesPair> sensiPvDisc = pvs.getYieldCurveSensitivities().get(MARKET.getCurve(ZERO_COUPON_NO.getCurrency()).getCurve().getName());
    for (int loopnode = 0; loopnode < sensiDisc.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDisc[loopnode], deltaTolerancePrice);
    }
    // 3. Price index curve sensitivity
    final double[] nodeTimesPrice = new double[] {ZERO_COUPON_NO.getReferenceEndTime()};
    final double[] sensiPrice = SensitivityFiniteDifferenceMarket.curveSensitivity(ZERO_COUPON_NO, MARKET, ZERO_COUPON_NO.getPriceIndex(), nodeTimesPrice, deltaShift, METHOD,
        FiniteDifferenceType.CENTRAL);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiPrice.length);
    final List<DoublesPair> sensiPvPrice = pvs.getPriceCurveSensitivities().get(MARKET.getCurve(ZERO_COUPON_NO.getPriceIndex()).getCurve().getName());
    for (int loopnode = 0; loopnode < sensiPrice.length; loopnode++) {
      final DoublesPair pairPv = sensiPvPrice.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesPrice[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiPrice[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Test the present value curves sensitivity.
   */
  public void presentValueCurveSensitivityWithNotional() {
    final PresentValueCurveSensitivityMarket pvs = METHOD.presentValueCurveSensitivity(ZERO_COUPON_WITH, MARKET);
    pvs.clean();
    final double deltaTolerancePrice = 1.0E+1;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 2. Discounting curve sensitivity
    final double[] nodeTimesDisc = new double[] {ZERO_COUPON_WITH.getPaymentTime()};
    final double[] sensiDisc = SensitivityFiniteDifferenceMarket.curveSensitivity(ZERO_COUPON_WITH, MARKET, ZERO_COUPON_WITH.getCurrency(), nodeTimesDisc, deltaShift, METHOD,
        FiniteDifferenceType.CENTRAL);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDisc.length);
    final List<DoublesPair> sensiPvDisc = pvs.getYieldCurveSensitivities().get(MARKET.getCurve(ZERO_COUPON_WITH.getCurrency()).getCurve().getName());
    for (int loopnode = 0; loopnode < sensiDisc.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDisc[loopnode], deltaTolerancePrice);
    }
    // 3. Price index curve sensitivity
    final double[] nodeTimesPrice = new double[] {ZERO_COUPON_WITH.getReferenceEndTime()};
    final double[] sensiPrice = SensitivityFiniteDifferenceMarket.curveSensitivity(ZERO_COUPON_WITH, MARKET, ZERO_COUPON_WITH.getPriceIndex(), nodeTimesPrice, deltaShift, METHOD,
        FiniteDifferenceType.CENTRAL);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiPrice.length);
    final List<DoublesPair> sensiPvPrice = pvs.getPriceCurveSensitivities().get(MARKET.getCurve(ZERO_COUPON_WITH.getPriceIndex()).getCurve().getName());
    for (int loopnode = 0; loopnode < sensiPrice.length; loopnode++) {
      final DoublesPair pairPv = sensiPvPrice.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesPrice[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiPrice[loopnode], deltaTolerancePrice);
    }
  }

}

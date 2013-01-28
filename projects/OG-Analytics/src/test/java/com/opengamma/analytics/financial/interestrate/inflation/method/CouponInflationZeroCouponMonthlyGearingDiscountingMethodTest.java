/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the present value and its sensitivities for zero-coupon with reference index on the first of the month.
 */
public class CouponInflationZeroCouponMonthlyGearingDiscountingMethodTest {
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[0]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final IborIndex[] IBOR_INDEXES = MARKET.getIndexesIbor().toArray(new IborIndex[0]);
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Calendar CALENDAR_EUR = EURIBOR3M.getCalendar();
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final Period COUPON_TENOR = Period.ofYears(10);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, COUPON_TENOR, BUSINESS_DAY, CALENDAR_EUR);
  private static final double NOTIONAL = 98765432;
  private static final int MONTH_LAG = 3;
  private static final double INDEX_1MAY_2008 = 108.23; // 3 m before Aug: May / 1 May index = May index: 108.23
  private static final double FACTOR = 0.50;
  private static final ZonedDateTime PRICING_DATE = DateUtils.getUTCDate(2011, 8, 3);
  private static final CouponInflationZeroCouponMonthlyGearingDefinition ZERO_COUPON_NO_DEFINITION = CouponInflationZeroCouponMonthlyGearingDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR, INDEX_1MAY_2008, MONTH_LAG, false, FACTOR);
  private static final CouponInflationZeroCouponMonthlyGearing ZERO_COUPON_NO = ZERO_COUPON_NO_DEFINITION.toDerivative(PRICING_DATE, "not used");
  private static final CouponInflationZeroCouponMonthlyGearingDefinition ZERO_COUPON_WITH_DEFINITION = CouponInflationZeroCouponMonthlyGearingDefinition.from(START_DATE, PAYMENT_DATE, NOTIONAL,
      PRICE_INDEX_EUR, INDEX_1MAY_2008, MONTH_LAG, true, FACTOR);
  private static final CouponInflationZeroCouponMonthlyGearing ZERO_COUPON_WITH = ZERO_COUPON_WITH_DEFINITION.toDerivative(PRICING_DATE, "not used");
  private static final CouponInflationZeroCouponMonthlyGearingDiscountingMethod METHOD = new CouponInflationZeroCouponMonthlyGearingDiscountingMethod();
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(ZERO_COUPON_NO.getCurrency()).getDiscountFactor(ZERO_COUPON_NO.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_NO.getReferenceEndTime());
    final double pvExpected = FACTOR * (finalIndex / INDEX_1MAY_2008 - 1) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation: Present value", pvExpected, pv.getAmount(ZERO_COUPON_NO.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the net amount.
   */
  public void netAmountNoNotional() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_NO.getReferenceEndTime());
    final double pvExpected = FACTOR * (finalIndex / INDEX_1MAY_2008 - 1) * NOTIONAL;
    assertEquals("Zero-coupon inflation: net amount", pvExpected, pv.getAmount(ZERO_COUPON_NO.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value: Method vs Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyAmount pvCalculator = ZERO_COUPON_NO.accept(PVIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation: Present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the net amount: Method vs Calculator.
   */
  public void netAmountMethodVsCalculator() {
    final MultipleCurrencyAmount naMethod = METHOD.netAmount(ZERO_COUPON_NO, MARKET.getInflationProvider());
    final MultipleCurrencyAmount naCalculator = ZERO_COUPON_NO.accept(NAIC, MARKET.getInflationProvider());
    assertEquals("Zero-coupon inflation: Net amount", naMethod, naCalculator);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueWithNotional() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final double df = MARKET.getCurve(ZERO_COUPON_WITH.getCurrency()).getDiscountFactor(ZERO_COUPON_WITH.getPaymentTime());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_WITH.getReferenceEndTime());
    final double pvExpected = FACTOR * (finalIndex / INDEX_1MAY_2008) * df * NOTIONAL;
    assertEquals("Zero-coupon inflation: Present value", pvExpected, pv.getAmount(ZERO_COUPON_WITH.getCurrency()), 1.0E-2);
  }

  @Test
  /**
   * Tests the net amount.
   */
  public void netAmountWithNotional() {
    final MultipleCurrencyAmount pv = METHOD.netAmount(ZERO_COUPON_WITH, MARKET.getInflationProvider());
    final double finalIndex = MARKET.getCurve(PRICE_INDEX_EUR).getPriceIndex(ZERO_COUPON_WITH.getReferenceEndTime());
    final double pvExpected = FACTOR * (finalIndex / INDEX_1MAY_2008) * NOTIONAL;
    assertEquals("Zero-coupon inflation: net amount", pvExpected, pv.getAmount(ZERO_COUPON_WITH.getCurrency()), 1.0E-2);
  }
  //  @Test // TODO
  //  /**
  //   * Test the present value curves sensitivity.
  //   */
  //  public void presentValueCurveSensitivityNoNotional() {
  //    final CurveSensitivityMarket pvs = METHOD.presentValueCurveSensitivity(ZERO_COUPON_NO, MARKET);
  //    pvs.cleaned();
  //    final double deltaTolerancePrice = 1.0E+1;
  //    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
  //    final double deltaShift = 1.0E-6;
  //    // 2. Discounting curve sensitivity
  //    final double[] nodeTimesDisc = new double[] {ZERO_COUPON_NO.getPaymentTime()};
  //    final double[] sensiDisc = SensitivityFiniteDifferenceMarket
  //        .curveSensitivity(ZERO_COUPON_NO, MARKET, ZERO_COUPON_NO.getCurrency(), nodeTimesDisc, deltaShift, METHOD, FiniteDifferenceType.CENTRAL);
  //    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDisc.length);
  //    final List<DoublesPair> sensiPvDisc = pvs.getYieldDiscountingSensitivities().get(MARKET.getCurve(ZERO_COUPON_NO.getCurrency()).getName());
  //    for (int loopnode = 0; loopnode < sensiDisc.length; loopnode++) {
  //      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
  //      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
  //      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDisc[loopnode], deltaTolerancePrice);
  //    }
  //    // 3. Price index curve sensitivity
  //    final double[] nodeTimesPrice = new double[] {ZERO_COUPON_NO.getReferenceEndTime()};
  //    final double[] sensiPrice = SensitivityFiniteDifferenceMarket.curveSensitivity(ZERO_COUPON_NO, MARKET, ZERO_COUPON_NO.getPriceIndex(), nodeTimesPrice, deltaShift, METHOD,
  //        FiniteDifferenceType.CENTRAL);
  //    assertEquals("Sensitivity finite difference method: number of node", 1, sensiPrice.length);
  //    final List<DoublesPair> sensiPvPrice = pvs.getPriceCurveSensitivities().get(MARKET.getCurve(ZERO_COUPON_NO.getPriceIndex()).getCurve().getName());
  //    for (int loopnode = 0; loopnode < sensiPrice.length; loopnode++) {
  //      final DoublesPair pairPv = sensiPvPrice.get(loopnode);
  //      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesPrice[loopnode], pairPv.getFirst(), 1E-8);
  //      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiPrice[loopnode], deltaTolerancePrice);
  //    }
  //  }
  //
  //  @Test
  //  /**
  //   * Test the present value curves sensitivity.
  //   */
  //  public void presentValueCurveSensitivityWithNotional() {
  //    final CurveSensitivityMarket pvs = METHOD.presentValueCurveSensitivity(ZERO_COUPON_WITH, MARKET);
  //    pvs.cleaned();
  //    final double deltaTolerancePrice = 1.0E+1;
  //    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
  //    final double deltaShift = 1.0E-6;
  //    // 2. Discounting curve sensitivity
  //    final double[] nodeTimesDisc = new double[] {ZERO_COUPON_WITH.getPaymentTime()};
  //    final double[] sensiDisc = SensitivityFiniteDifferenceMarket.curveSensitivity(ZERO_COUPON_WITH, MARKET, ZERO_COUPON_WITH.getCurrency(), nodeTimesDisc, deltaShift, METHOD,
  //        FiniteDifferenceType.CENTRAL);
  //    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDisc.length);
  //    final List<DoublesPair> sensiPvDisc = pvs.getYieldDiscountingSensitivities().get(MARKET.getCurve(ZERO_COUPON_WITH.getCurrency()).getName());
  //    for (int loopnode = 0; loopnode < sensiDisc.length; loopnode++) {
  //      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
  //      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
  //      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDisc[loopnode], deltaTolerancePrice);
  //    }
  //    // 3. Price index curve sensitivity
  //    final double[] nodeTimesPrice = new double[] {ZERO_COUPON_WITH.getReferenceEndTime()};
  //    final double[] sensiPrice = SensitivityFiniteDifferenceMarket.curveSensitivity(ZERO_COUPON_WITH, MARKET, ZERO_COUPON_WITH.getPriceIndex(), nodeTimesPrice, deltaShift, METHOD,
  //        FiniteDifferenceType.CENTRAL);
  //    assertEquals("Sensitivity finite difference method: number of node", 1, sensiPrice.length);
  //    final List<DoublesPair> sensiPvPrice = pvs.getPriceCurveSensitivities().get(MARKET.getCurve(ZERO_COUPON_WITH.getPriceIndex()).getCurve().getName());
  //    for (int loopnode = 0; loopnode < sensiPrice.length; loopnode++) {
  //      final DoublesPair pairPv = sensiPvPrice.get(loopnode);
  //      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesPrice[loopnode], pairPv.getFirst(), 1E-8);
  //      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiPrice[loopnode], deltaTolerancePrice);
  //    }
  //  }

}

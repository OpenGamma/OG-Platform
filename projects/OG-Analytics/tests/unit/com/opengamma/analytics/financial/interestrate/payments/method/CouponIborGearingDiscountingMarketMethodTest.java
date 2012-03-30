/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.interestrate.market.MarketBundle;
import com.opengamma.analytics.financial.interestrate.market.MarketDataSets;
import com.opengamma.analytics.financial.interestrate.market.PresentValueCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.method.market.SensitivityFiniteDifferenceMarket;
import com.opengamma.analytics.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.market.CouponIborGearingDiscountingMarketMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with gearing factor and spread in the discounting method.
 */
public class CouponIborGearingDiscountingMarketMethodTest {
  private static final MarketBundle MARKET = MarketDataSets.createMarket1();
  private static final IborIndex[] IBOR_INDEXES = MARKET.getIborIndexes().toArray(new IborIndex[0]);
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Calendar CALENDAR_EUR = EURIBOR3M.getCalendar();
  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, -EURIBOR3M.getSpotLag(), CALENDAR_EUR);
  private static final CouponIborGearingDefinition COUPON_DEFINITION = new CouponIborGearingDefinition(EURIBOR3M.getCurrency(), ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR,
      NOTIONAL, FIXING_DATE, EURIBOR3M, SPREAD, FACTOR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponIborGearing COUPON = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {"A", "B"});
  private static final CouponIborGearingDiscountingMarketMethod METHOD = new CouponIborGearingDiscountingMarketMethod();

  @Test
  public void presentValue() {
    CurrencyAmount pv = METHOD.presentValue(COUPON, MARKET);
    double df = MARKET.getDiscountingFactor(COUPON.getCurrency(), COUPON.getPaymentTime());
    double forward = MARKET.getForwardRate(EURIBOR3M, COUPON.getFixingPeriodStartTime(), COUPON.getFixingPeriodEndTime(), COUPON.getFixingAccrualFactor());
    double pvExpected = (forward * FACTOR + SPREAD) * COUPON.getPaymentYearFraction() * COUPON.getNotional() * df;
    assertEquals("Coupon Ibor Gearing: Present value by discounting", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Test the present value curves sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final PresentValueCurveSensitivityMarket pvs = METHOD.presentValueCurveSensitivity(COUPON, MARKET);
    pvs.clean();
    final double deltaTolerancePrice = 1.0E+1;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final double[] nodeTimesForward = new double[] {COUPON.getFixingPeriodStartTime(), COUPON.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifferenceMarket.curveSensitivity(COUPON, MARKET, EURIBOR3M, nodeTimesForward, deltaShift, METHOD, FiniteDifferenceType.CENTRAL);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvs.getYieldCurveSensitivities().get(MARKET.getCurve(EURIBOR3M).getCurve().getName());
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final double[] nodeTimesDisc = new double[] {COUPON.getPaymentTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifferenceMarket.curveSensitivity(COUPON, MARKET, COUPON.getCurrency(), nodeTimesDisc, deltaShift, METHOD, FiniteDifferenceType.CENTRAL);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvs.getYieldCurveSensitivities().get(MARKET.getCurve(COUPON.getCurrency()).getCurve().getName());
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

}

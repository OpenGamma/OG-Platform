/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

public class InterestRateFutureTransactionDiscountingMethodTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtil.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, TENOR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  // Time version
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 5, 12);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR,
      NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.985;
  private static final InterestRateFutureTransaction FUTURE_TRANSACTION = new InterestRateFutureTransaction(ERU2, QUANTITY, TRADE_PRICE);
  // Method
  private static final InterestRateFutureTransactionDiscountingMethod METHOD = new InterestRateFutureTransactionDiscountingMethod();
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();

  @Test
  public void presentValueFromPrice() {
    double quotedPrice = 0.98;
    double pv = METHOD.presentValueFromPrice(FUTURE_TRANSACTION, quotedPrice);
    double expectedPv = (quotedPrice - TRADE_PRICE) * FUTURE_FACTOR * NOTIONAL * QUANTITY;
    assertEquals("Present value from quoted price", expectedPv, pv);
  }

  @Test
  /**
   * Test the present value computed from the curves
   */
  public void presentValue() {
    double pv = METHOD.presentValue(FUTURE_TRANSACTION, CURVES);
    YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    double forward = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    double expectedPv = (1 - forward - TRADE_PRICE) * FUTURE_FACTOR * NOTIONAL * QUANTITY;
    assertEquals("Present value from quoted price", expectedPv, pv);
  }

  @Test
  /**
   * Comparison of value from the method and value from the present value calculator.
   */
  public void methodVsCalculator() {
    double pvMethod = METHOD.presentValue(FUTURE_TRANSACTION, CURVES);
    PresentValueCalculator pvc = PresentValueCalculator.getInstance();
    double pvCalculator = pvc.visit(FUTURE_TRANSACTION, CURVES);
    assertEquals("Future discounting: method comparison with present value calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value curves sensitivity computed from the curves
   */
  public void presentValueCurveSensitivity() {
    PresentValueSensitivity pvsFuture = METHOD.presentValueCurveSensitivity(FUTURE_TRANSACTION, CURVES);
    pvsFuture.clean();
    double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    String bumpedCurveName = "Bumped Curve";
    InterestRateFutureSecurity futureSecutiryBumpedForward = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR,
        NAME, DISCOUNTING_CURVE_NAME, bumpedCurveName);
    InterestRateFutureTransaction futureTransactionBumpedForward = new InterestRateFutureTransaction(futureSecutiryBumpedForward, QUANTITY, TRADE_PRICE);
    double[] nodeTimesForward = new double[] {ERU2.getFixingPeriodStartTime(), ERU2.getFixingPeriodEndTime()};
    double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(futureTransactionBumpedForward, CURVES, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    List<DoublesPair> sensiPvForward = pvsFuture.getSensitivity().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity future pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    PresentValueSensitivityCalculator calculator = PresentValueSensitivityCalculator.getInstance();
    Map<String, List<DoublesPair>> sensiCalculator = calculator.visit(FUTURE_TRANSACTION, CURVES);
    PresentValueSensitivity sensiMethod = METHOD.presentValueCurveSensitivity(FUTURE_TRANSACTION, CURVES);
    assertEquals("Future discounting curve sensitivity: method comparison with present value calculator", sensiCalculator, sensiMethod.getSensitivity());
  }

  @Test
  /**
   * Test the rate computed from the method and from the calculator.
   */
  public void parRateMethodVsCalculator() {
    InterestRateFutureSecurityDiscountingMethod methodSecurity = new InterestRateFutureSecurityDiscountingMethod();
    double rateMethod = methodSecurity.parRate(FUTURE_TRANSACTION.getUnderlyingFuture(), CURVES);
    ParRateCalculator calculator = ParRateCalculator.getInstance();
    double rateCalculator = calculator.visit(FUTURE_TRANSACTION, CURVES);
    assertEquals("Future rate from curves", rateMethod, rateCalculator, 1.0E-10);
  }

}

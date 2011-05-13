/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

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
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.future.method.InterestRateFuturesTransactionDiscountingMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
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
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR,
      FORWARD_CURVE_NAME, NAME);
  // Transaction
  private static final int QUANTITY = -123;
  //  private static final ZonedDateTime TRADE_DATE = DateUtil.getUTCDate(2011, 5, 12);
  private static final double TRADE_PRICE = 0.985;
  private static final InterestRateFutureTransaction FUTURE_TRANSACTION = new InterestRateFutureTransaction(ERU2, QUANTITY, TRADE_PRICE);
  // Method
  private static final InterestRateFuturesTransactionDiscountingMethod METHOD = new InterestRateFuturesTransactionDiscountingMethod();

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
  public void presentValueFromCurve() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    double pv = METHOD.presentValueFromCurve(FUTURE_TRANSACTION, curves);
    YieldAndDiscountCurve forwardCurve = curves.getCurve(FORWARD_CURVE_NAME);
    double forward = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    double expectedPv = (1 - forward - TRADE_PRICE) * FUTURE_FACTOR * NOTIONAL * QUANTITY;
    assertEquals("Present value from quoted price", expectedPv, pv);
  }

  @Test
  /**
   * Test the present value curves sensitivity computed from the curves
   */
  public void presentValueCurveSensitivity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    // Par rate sensitivity
    PresentValueSensitivity pvsFuture = METHOD.presentValueCurveSensitivity(FUTURE_TRANSACTION, curves);
    pvsFuture.clean();
    double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-8;
    double pv = METHOD.presentValueFromCurve(FUTURE_TRANSACTION, curves);
    // 1. Forward curve sensitivity
    String bumpedCurveName = "Bumped Curve";
    //    String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    InterestRateFutureSecurity futureSecutiryBumpedForward = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR,
        bumpedCurveName, NAME);
    InterestRateFutureTransaction futureTransactionBumpedForward = new InterestRateFutureTransaction(futureSecutiryBumpedForward, QUANTITY, TRADE_PRICE);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    double[] timeForward = new double[2];
    timeForward[0] = ERU2.getFixingPeriodStartTime();
    timeForward[1] = ERU2.getFixingPeriodEndTime();
    int nbForwardDate = timeForward.length;
    final double[] yieldsForward = new double[nbForwardDate + 1];
    double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForward[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    List<DoublesPair> sensiPvForward = pvsFuture.getSensitivity().get(FORWARD_CURVE_NAME);
    double[] sensiPvForwardFD = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final double bumpedPv = METHOD.presentValueFromCurve(futureTransactionBumpedForward, curvesBumpedForward);
      sensiPvForwardFD[i] = (bumpedPv - pv) / deltaShift;
      final DoublesPair pairPv = sensiPvForward.get(i);
      assertEquals("Sensitivity future pv to forward curve: Node " + i, nodeTimesForward[i + 1], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity future pv to forward curve: Node " + i, sensiPvForwardFD[i], pairPv.getSecond(), deltaTolerancePrice);
    }
  }
}

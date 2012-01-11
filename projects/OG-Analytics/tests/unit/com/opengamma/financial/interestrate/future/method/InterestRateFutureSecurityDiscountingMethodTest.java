/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.calculator.PriceFromCurvesDiscountingCalculator;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * Tests for the methods related to interest rate securities pricing without convexity adjustment.
 */
public class InterestRateFutureSecurityDiscountingMethodTest {
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
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
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
  //  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final InterestRateFuture ERU2 = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL,
      FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
  private static final InterestRateFutureDiscountingMethod METHOD = InterestRateFutureDiscountingMethod.getInstance();
  private static final PriceFromCurvesDiscountingCalculator PRICE_CALCULATOR = PriceFromCurvesDiscountingCalculator.getInstance();
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();

  @Test
  /**
   * Test the price computed from the curves
   */
  public void price() {
    final double price = METHOD.price(ERU2, CURVES);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    final double forward = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    final double expectedPrice = 1.0 - forward;
    assertEquals("Future price from curves", expectedPrice, price);
  }

  @Test
  /**
   * Tests the method versus the calculator for the price.
   */
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD.price(ERU2, CURVES);
    final double priceCalculator = PRICE_CALCULATOR.visit(ERU2, CURVES);
    assertEquals("Bond future security Discounting: Method vs calculator", priceMethod, priceCalculator, 1.0E-10);
  }

  @Test
  /**
   * Test the rate computed from the curves
   */
  public void parRate() {
    final double rate = METHOD.parRate(ERU2, CURVES);
    final YieldAndDiscountCurve forwardCurve = CURVES.getCurve(FORWARD_CURVE_NAME);
    final double expectedRate = (forwardCurve.getDiscountFactor(FIXING_START_TIME) / forwardCurve.getDiscountFactor(FIXING_END_TIME) - 1) / FIXING_ACCRUAL;
    assertEquals("Future price from curves", expectedRate, rate, 1.0E-10);
  }

  @Test
  /**
   * Test the rate computed from the method and from the calculator.
   */
  public void parRateMethodVsCalculator() {
    final double rateMethod = METHOD.parRate(ERU2, CURVES);
    final ParRateCalculator calculator = ParRateCalculator.getInstance();
    final double rateCalculator = calculator.visit(ERU2, CURVES);
    assertEquals("Future price from curves", rateMethod, rateCalculator, 1.0E-10);
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.calculator.discounting.MarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the methods related to interest rate securities pricing without convexity adjustment.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureSecurityDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];

  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  // Time version
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 5, 12);
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.of(0, 0)), ZoneOffset.UTC);
  private static final double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR,
      NAME);

  private static final InterestRateFutureSecurityDiscountingMethod METHOD_IRFUT_SEC_DSC = InterestRateFutureSecurityDiscountingMethod.getInstance();

  private static final MarketQuoteDiscountingCalculator MQDC = MarketQuoteDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-10;

  @Test
  /**
   * Test the price computed from the curves
   */
  public void price() {
    final double price = METHOD_IRFUT_SEC_DSC.price(ERU2, MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL);
    final double expectedPrice = 1.0 - forward;
    assertEquals("Future price from curves", expectedPrice, price, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the method versus the calculator for the price.
   */
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD_IRFUT_SEC_DSC.price(ERU2, MULTICURVES);
    final double priceCalculator = ERU2.accept(MQDC, MULTICURVES);
    assertEquals("Bond future security Discounting: Method vs calculator", priceMethod, priceCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the rate computed from the curves
   */
  public void parRate() {
    final double rate = METHOD_IRFUT_SEC_DSC.parRate(ERU2, MULTICURVES);
    final double expectedRate = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL);
    assertEquals("Future price from curves", expectedRate, rate, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the rate computed from the method and from the calculator.
   */
  public void parRateMethodVsCalculator() {
    final double rateMethod = METHOD_IRFUT_SEC_DSC.parRate(ERU2, MULTICURVES);
    final double rateCalculator = ERU2.accept(PRDC, MULTICURVES);
    assertEquals("Future price from curves", rateMethod, rateCalculator, TOLERANCE_PRICE);
  }

}

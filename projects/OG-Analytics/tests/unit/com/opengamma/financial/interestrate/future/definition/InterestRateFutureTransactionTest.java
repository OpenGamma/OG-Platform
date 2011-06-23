/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the construction of interest rate future transactions.
 */
public class InterestRateFutureTransactionTest {
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureTransaction(null, QUANTITY, TRADE_PRICE);
  }

  @Test
  public void getter() {
    assertEquals(ERU2, FUTURE_TRANSACTION.getUnderlyingFuture());
    assertEquals(QUANTITY, FUTURE_TRANSACTION.getQuantity());
    assertEquals(TRADE_PRICE, FUTURE_TRANSACTION.getReferencePrice());
  }

  @Test
  public void equalHash() {
    assertTrue(FUTURE_TRANSACTION.equals(FUTURE_TRANSACTION));
    InterestRateFutureTransaction other = new InterestRateFutureTransaction(ERU2, QUANTITY, TRADE_PRICE);
    assertTrue(FUTURE_TRANSACTION.equals(other));
    assertTrue(FUTURE_TRANSACTION.hashCode() == other.hashCode());
    assertEquals(FUTURE_TRANSACTION.toString(), other.toString());
    InterestRateFutureTransaction modifiedFuture;
    modifiedFuture = new InterestRateFutureTransaction(ERU2, QUANTITY + 1, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransaction(ERU2, QUANTITY, TRADE_PRICE + 0.01);
    assertFalse(FUTURE_TRANSACTION.equals(modifiedFuture));
    InterestRateFutureSecurity otherSecurity = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL * 2, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    modifiedFuture = new InterestRateFutureTransaction(otherSecurity, QUANTITY, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION.equals(modifiedFuture));
    assertFalse(FUTURE_TRANSACTION.equals(TRADE_PRICE));
    assertFalse(FUTURE_TRANSACTION.equals(null));
  }

}

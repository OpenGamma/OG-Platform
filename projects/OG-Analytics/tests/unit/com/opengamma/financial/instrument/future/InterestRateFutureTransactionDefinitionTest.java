/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
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
 * Tests the interest rate future transaction construction.
 */
public class InterestRateFutureTransactionDefinitionTest { //EURIBOR 3M Index
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
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final InterestRateFutureSecurityDefinition ERU2 = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
  // Transaction
  private static final int QUANTITY = -123;
  private static final ZonedDateTime TRADE_DATE = DateUtil.getUTCDate(2011, 5, 12);
  private static final double TRADE_PRICE = 0.985;
  private static final InterestRateFutureTransactionDefinition FUTURE_TRANSACTION = new InterestRateFutureTransactionDefinition(ERU2, QUANTITY, TRADE_DATE, TRADE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullunderlying() {
    new InterestRateFutureTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTradeDate() {
    new InterestRateFutureTransactionDefinition(ERU2, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  public void getter() {
    assertEquals(ERU2, FUTURE_TRANSACTION.getUnderlyingFuture());
    assertEquals(QUANTITY, FUTURE_TRANSACTION.getQuantity());
    assertEquals(TRADE_DATE, FUTURE_TRANSACTION.getTradeDate());
    assertEquals(TRADE_PRICE, FUTURE_TRANSACTION.getTradePrice());
  }
}

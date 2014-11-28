/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to bond futures security Definition construction.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureDefinitionTest {
  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency CUR = Currency.EUR;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5),
      Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31),
      DateUtils.getUTCDate(2011, 1, 31), DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30),
      DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket],
          SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 9, 29);
  private static final double NOTIONAL = 100000;
  private static final double REF_PRICE = 0.0;
  private static final BondFutureDefinition FUTURE_DEFINITION = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL,
      BASKET_DEFINITION, CONVERSION_FACTOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLastTrading() {
    new BondFutureDefinition(null, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstNotice() {
    new BondFutureDefinition(LAST_TRADING_DATE, null, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLastNotice() {
    new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, null, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBasket() {
    new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, null, CONVERSION_FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConversion() {
    new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroBasket() {
    new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, new BondFixedSecurityDefinition[0], CONVERSION_FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sizeConversionFactor() {
    final double[] incorrectConversionFactor = new double[NB_BOND - 1];
    new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, incorrectConversionFactor);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Bond future security definition: last trading date", LAST_TRADING_DATE, FUTURE_DEFINITION.getTradingLastDate());
    assertEquals("Bond future security definition: first notice date", FIRST_NOTICE_DATE, FUTURE_DEFINITION.getNoticeFirstDate());
    assertEquals("Bond future security definition: last notice date", LAST_NOTICE_DATE, FUTURE_DEFINITION.getNoticeLastDate());
    assertEquals("Bond future security definition: first delivery date", ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR),
        FUTURE_DEFINITION.getDeliveryFirstDate());
    assertEquals("Bond future security definition: last delivery date", ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR),
        FUTURE_DEFINITION.getDeliveryLastDate());
    assertEquals("Bond future security definition: notional", NOTIONAL, FUTURE_DEFINITION.getNotional());
    assertEquals("Bond future security definition: delivery basket", BASKET_DEFINITION, FUTURE_DEFINITION.getDeliveryBasket());
    assertEquals("Bond future security definition: conversion factors", CONVERSION_FACTOR, FUTURE_DEFINITION.getConversionFactor());
    assertEquals("Bond future security definition: settlement days", SETTLEMENT_DAYS, FUTURE_DEFINITION.getSettlementDays());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_DEFINITION.equals(FUTURE_DEFINITION));
    final BondFutureDefinition other = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
    assertTrue(FUTURE_DEFINITION.equals(other));
    assertTrue(FUTURE_DEFINITION.hashCode() == other.hashCode());
    BondFutureDefinition modifiedFuture;
    modifiedFuture = new BondFutureDefinition(FIRST_NOTICE_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
    assertFalse(FUTURE_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new BondFutureDefinition(LAST_TRADING_DATE, LAST_TRADING_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
    assertFalse(FUTURE_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, FIRST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
    assertFalse(FUTURE_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL + 100000, BASKET_DEFINITION, CONVERSION_FACTOR);
    assertFalse(FUTURE_DEFINITION.equals(modifiedFuture));
    final double[] otherConversionFactor = new double[] {.9000, .8565, .8493, .8516, .8540, .8417, .8292 };
    modifiedFuture = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, otherConversionFactor);
    assertFalse(FUTURE_DEFINITION.equals(modifiedFuture));
    final BondFixedSecurityDefinition[] otherBasket = new BondFixedSecurityDefinition[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      otherBasket[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, 2 * RATE[loopbasket],
          SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
    }
    modifiedFuture = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, otherBasket, CONVERSION_FACTOR);
    assertFalse(FUTURE_DEFINITION.equals(modifiedFuture));
    assertFalse(FUTURE_DEFINITION.equals(LAST_TRADING_DATE));
    assertFalse(FUTURE_DEFINITION.equals(null));
  }

  /**
   * Tests the toDerivative method.
   */
  @Test
  public void toDerivative() {
    final ZonedDateTime firstDeliveryDate = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final ZonedDateTime lastDeliveryDate = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 17);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double lastTradingTime = actAct.getDayCountFraction(referenceDate, LAST_TRADING_DATE);
    final double firstNoticeTime = actAct.getDayCountFraction(referenceDate, FIRST_NOTICE_DATE);
    final double lastNoticeTime = actAct.getDayCountFraction(referenceDate, LAST_NOTICE_DATE);
    final double firstDeliveryTime = actAct.getDayCountFraction(referenceDate, firstDeliveryDate);
    final double lastDeliveryTime = actAct.getDayCountFraction(referenceDate, lastDeliveryDate);
    final BondFixedSecurity[] basket = new BondFixedSecurity[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      basket[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(referenceDate, lastDeliveryDate);
    }
    final BondFuture futureConverted = FUTURE_DEFINITION.toDerivative(referenceDate, REF_PRICE);
    final BondFuture futureExpected = new BondFuture(lastTradingTime, firstNoticeTime, lastNoticeTime, firstDeliveryTime, lastDeliveryTime, NOTIONAL, basket,
        CONVERSION_FACTOR, REF_PRICE);
    assertEquals("Bond future security definition: future conversion", futureExpected, futureConverted);
  }
}

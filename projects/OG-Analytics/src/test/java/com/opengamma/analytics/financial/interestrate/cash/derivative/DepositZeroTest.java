/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of DepositZero.
 */
@Test(groups = TestGroup.UNIT)
public class DepositZeroTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE_FIGURE = 0.0250;
  private static final InterestRate RATE = new ContinuousInterestRate(RATE_FIGURE);
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final DayCount DAY_COUNT = DayCounts.ACT_365;
  private static final double START_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, SPOT_DATE);
  private static final double END_TIME = TimeCalculator.getTimeBetween(TRADE_DATE, END_DATE);
  private static final double DEPOSIT_AF = DAY_COUNT.getDayCountFraction(SPOT_DATE, END_DATE);
  private static final double INTEREST_AMOUNT = 1.0 / RATE.getDiscountFactor(DEPOSIT_AF) * NOTIONAL;

  private static final DepositZero DEPOSIT = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new DepositZero(null, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullRate() {
    new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, null, INTEREST_AMOUNT);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetCurveName() {
    DEPOSIT.getDiscountingCurveName();
  }

  @Test
  /**
   * Tests the getters
   */
  public void getter() {
    assertEquals("DepositZero: getter", START_TIME, DEPOSIT.getStartTime());
    assertEquals("DepositZero: getter", END_TIME, DEPOSIT.getEndTime());
    assertEquals("DepositZero: getter", NOTIONAL, DEPOSIT.getInitialAmount());
    assertEquals("DepositZero: getter", NOTIONAL, DEPOSIT.getNotional());
    assertEquals("DepositZero: getter", RATE, DEPOSIT.getRate());
    assertEquals("DepositZero: getter", EUR, DEPOSIT.getCurrency());
    assertEquals("DepositZero: getter", DEPOSIT_AF, DEPOSIT.getPaymentAccrualFactor());
    assertEquals("DepositZero: getter", INTEREST_AMOUNT, DEPOSIT.getInterestAmount());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertEquals("DepositZero: equal-hash code", DEPOSIT, DEPOSIT);
    final DepositZero other = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
    assertEquals("DepositZero: equal-hash code", other, DEPOSIT);
    assertEquals("DepositZero: equal-hash code", other.hashCode(), DEPOSIT.hashCode());
    DepositZero modified;
    modified = new DepositZero(Currency.USD, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME + 0.01, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME, END_TIME + 0.01, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL + 10, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL + 10, DEPOSIT_AF, RATE, INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF + 0.01, RATE, INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, new PeriodicInterestRate(RATE_FIGURE, 1), INTEREST_AMOUNT);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
    modified = new DepositZero(EUR, START_TIME, END_TIME, NOTIONAL, NOTIONAL, DEPOSIT_AF, RATE, INTEREST_AMOUNT + 10);
    assertFalse("DepositZero: equal-hash code", DEPOSIT.equals(modified));
  }

}

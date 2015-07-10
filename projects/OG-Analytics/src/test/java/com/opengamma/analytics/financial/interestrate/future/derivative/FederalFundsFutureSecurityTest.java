/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Federal Fund future.
 */
@Test(groups = TestGroup.UNIT)
public class FederalFundsFutureSecurityTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final Currency USD = INDEX_FEDFUND.getCurrency();
  private static final BusinessDayConvention BUSINESS_DAY_PRECEDING = BusinessDayConventions.PRECEDING;
  private static final BusinessDayConvention BUSINESS_DAY_FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final ZonedDateTime APRIL_1 = DateUtils.getUTCDate(2012, 4, 1);
  private static final ZonedDateTime LAST_TRADING_DATE = BUSINESS_DAY_PRECEDING.adjustDate(NYC, APRIL_1);
  private static final ZonedDateTime PERIOD_FIRST_DATE = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, MARCH_1);
  private static final ZonedDateTime PERIOD_LAST_DATE = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, APRIL_1.minusDays(1));
  private static final List<ZonedDateTime> FIXING_LIST = new ArrayList<>();
  private static final ZonedDateTime[] FIXING_DATE;
  static {
    ZonedDateTime date = PERIOD_FIRST_DATE;
    while (!date.isAfter(PERIOD_LAST_DATE)) {
      FIXING_LIST.add(date);
      date = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, date.plusDays(1));
    }
    FIXING_DATE = FIXING_LIST.toArray(new ZonedDateTime[FIXING_LIST.size()]);
  }
  private static final double[] FIXING_ACCURAL_FACTOR = new double[FIXING_DATE.length - 1];
  private static final double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double[] FIXING_TIME = new double[FIXING_DATE.length];
  private static double FIXING_TOTAL_ACCURAL_FACTOR = 0.0;
  static {
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 1; loopfix++) {
      FIXING_ACCURAL_FACTOR[loopfix] = INDEX_FEDFUND.getDayCount().getDayCountFraction(FIXING_DATE[loopfix], FIXING_DATE[loopfix + 1]);
      FIXING_TOTAL_ACCURAL_FACTOR += FIXING_ACCURAL_FACTOR[loopfix];
    }
    for (int loopfix = 0; loopfix < FIXING_DATE.length; loopfix++) {
      FIXING_TIME[loopfix] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE[loopfix]);
    }
  }
  private static final double NOTIONAL = 5000000;
  private static final double PAYMENT_ACCURAL_FACTOR = 1.0 / 12.0;
  private static final String NAME = "FFH2";
  private static final double ACCRUED_INTERESTS = 0;

  private static final FederalFundsFutureSecurity FUTURE_FEDFUND = new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME,
      FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLastTrading() {
    new FederalFundsFutureSecurity(null, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingTime() {
    new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, null, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingAccrual() {
    new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, null, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixingLength() {
    new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, new double[3], LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Fed fund future security", INDEX_FEDFUND, FUTURE_FEDFUND.getIndex());
    assertEquals("Fed fund future security", ACCRUED_INTERESTS, FUTURE_FEDFUND.getAccruedInterest());
    assertEquals("Fed fund future security", FIXING_TIME, FUTURE_FEDFUND.getFixingPeriodTime());
    assertEquals("Fed fund future security", FIXING_ACCURAL_FACTOR, FUTURE_FEDFUND.getFixingPeriodAccrualFactor());
    assertEquals("Fed fund future security", NOTIONAL, FUTURE_FEDFUND.getNotional());
    assertEquals("Fed fund future security", PAYMENT_ACCURAL_FACTOR, FUTURE_FEDFUND.getPaymentAccrualFactor());
    assertEquals("Fed fund future security", NAME, FUTURE_FEDFUND.getName());
    assertEquals("Fed fund future security", USD, FUTURE_FEDFUND.getCurrency());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_FEDFUND.equals(FUTURE_FEDFUND));
    final FederalFundsFutureSecurity other = new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR,
        NOTIONAL,
        PAYMENT_ACCURAL_FACTOR, NAME);
    assertTrue(FUTURE_FEDFUND.equals(other));
    assertTrue(FUTURE_FEDFUND.hashCode() == other.hashCode());
    FederalFundsFutureSecurity modifiedFuture;
    modifiedFuture = new FederalFundsFutureSecurity(IndexONMaster.getInstance().getIndex("EONIA"), ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR,
        FIXING_TOTAL_ACCURAL_FACTOR,
        NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS + 0.1, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL,
        PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL + 10.0,
        PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL, 0.25, NAME);
    assertFalse(FUTURE_FEDFUND.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurity(INDEX_FEDFUND, ACCRUED_INTERESTS, FIXING_TIME, LAST_TRADING_TIME, FIXING_ACCURAL_FACTOR, FIXING_TOTAL_ACCURAL_FACTOR, NOTIONAL,
        PAYMENT_ACCURAL_FACTOR, "Wrong");
    assertFalse(FUTURE_FEDFUND.equals(modifiedFuture));
    assertFalse(FUTURE_FEDFUND.equals(INDEX_FEDFUND));
    assertFalse(FUTURE_FEDFUND.equals(null));
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

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
import com.opengamma.util.time.DateUtils;

/**
 * Tests the ForwardRateAgreement construction.
 */
public class ForwardRateAgreementTest {

  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Dates : The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 1, 7);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double ACCRUAL_FACTOR_FIXING = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // To derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double FIXING_PERIOD_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_START_DATE);
  private static final double FIXING_PERIOD_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  //  private static final String[] CURVES = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final ForwardRateAgreement FRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME,
      FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new ForwardRateAgreement(null, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING,
        FRA_RATE, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    new ForwardRateAgreement(CUR, PAYMENT_TIME, null, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING, FRA_RATE,
        FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, null, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING,
        FRA_RATE, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING,
        FRA_RATE, null);
  }

  @Test
  public void getter() {
    assertEquals(FRA.getFixingPeriodStartTime(), FIXING_PERIOD_START_TIME);
    assertEquals(FRA.getFixingPeriodEndTime(), FIXING_PERIOD_END_TIME);
    assertEquals(FRA.getFixingYearFraction(), ACCRUAL_FACTOR_FIXING);
    assertEquals(FRA.getIndex(), INDEX);
    assertEquals(FRA.getRate(), FRA_RATE);
    assertEquals(FRA.getForwardCurveName(), FORWARD_CURVE_NAME);
  }

  @Test
  public void equalHash() {
    assertEquals(FRA, FRA);
    final ForwardRateAgreement newFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME,
        FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(newFRA.equals(FRA), true);
    assertEquals(newFRA.hashCode() == FRA.hashCode(), true);
    ForwardRateAgreement modifiedFRA;
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME + 1.0, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FORWARD_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT + 1.0, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME - 0.01, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME - 0.001, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME + 0.01,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING + 1.0, FRA_RATE, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE + 1.0, FORWARD_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FUNDING_CURVE_NAME);
    assertEquals(modifiedFRA.equals(FRA), false);
    final IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, FUNDING_CURVE_NAME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, otherIndex, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE, FORWARD_CURVE_NAME);
    assertFalse(modifiedFRA.equals(FRA));
    assertFalse(FRA.equals(CUR));
    assertFalse(FRA.equals(null));
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.fra.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the ForwardRateAgreement construction.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementTest {

  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Dates : The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 1, 7);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double ACCRUAL_FACTOR_FIXING = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // To derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double FIXING_PERIOD_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_START_DATE);
  private static final double FIXING_PERIOD_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final ForwardRateAgreement FRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME,
      FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING, FRA_RATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new ForwardRateAgreement(null, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING,
        FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, null, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING,
        FRA_RATE);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetFundingCurveName() {
    FRA.getFundingCurveName();
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetDiscountingCurveName() {
    FRA.getForwardCurveName();
  }

  @Test
  public void getter() {
    assertEquals(FRA.getFixingPeriodStartTime(), FIXING_PERIOD_START_TIME);
    assertEquals(FRA.getFixingPeriodEndTime(), FIXING_PERIOD_END_TIME);
    assertEquals(FRA.getFixingYearFraction(), ACCRUAL_FACTOR_FIXING);
    assertEquals(FRA.getIndex(), INDEX);
    assertEquals(FRA.getRate(), FRA_RATE);
  }

  @Test
  public void equalHash() {
    assertEquals(FRA, FRA);
    final ForwardRateAgreement newFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME,
        FIXING_PERIOD_END_TIME, ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(newFRA.equals(FRA), true);
    assertEquals(newFRA.hashCode() == FRA.hashCode(), true);
    ForwardRateAgreement modifiedFRA;
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME + 1.0, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT + 1.0, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME - 0.01, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME - 0.001, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME + 0.01,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING + 1.0, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA), false);
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE + 1.0);
    assertEquals(modifiedFRA.equals(FRA), false);
    final IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor1");
    modifiedFRA = new ForwardRateAgreement(CUR, PAYMENT_TIME, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, otherIndex, FIXING_TIME, FIXING_PERIOD_START_TIME, FIXING_PERIOD_END_TIME,
        ACCRUAL_FACTOR_FIXING, FRA_RATE);
    assertFalse(modifiedFRA.equals(FRA));
    assertFalse(FRA.equals(CUR));
    assertFalse(FRA.equals(null));
  }
}

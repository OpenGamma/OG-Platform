/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.fra;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the ForwardRateAgreementDefinition construction.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementDefinitionTest {

  // Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Calendar PAY_CALENDAR = new CalendarGBP("B");
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
  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ForwardRateAgreementDefinition FRA_DEFINITION_1 = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL,
      FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  private static final ForwardRateAgreementDefinition FRA_DEFINITION_2 = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL,
      FIXING_DATE, FIXING_START_DATE.plusDays(1), FIXING_END_DATE.plusDays(1), INDEX, FRA_RATE, CALENDAR);
  private static final ForwardRateAgreementDefinition FRA_DEFINITION_3 = ForwardRateAgreementDefinition.from(FRA_DEFINITION_1, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  private static final ForwardRateAgreementDefinition FRA_DEFINITION_4 = ForwardRateAgreementDefinition.from(ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, INDEX, FRA_RATE, CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new ForwardRateAgreementDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new ForwardRateAgreementDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStart() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEnd() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, null, INDEX, FRA_RATE, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, null, FRA_RATE, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncorrectCurrency() {
    final Currency EUR = Currency.EUR;
    new ForwardRateAgreementDefinition(EUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, null, FRA_RATE, CALENDAR);
  }

  @Test
  public void getter() {
    assertEquals(FRA_DEFINITION_1.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(FRA_DEFINITION_1.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(FRA_DEFINITION_1.getIndex(), INDEX);
    assertEquals(FRA_DEFINITION_1.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    assertEquals(FRA_DEFINITION_1.getRate(), FRA_RATE);
    assertEquals(FRA_DEFINITION_1.getFixingPeriodStartDate(), FIXING_START_DATE);
    assertEquals(FRA_DEFINITION_1.getFixingPeriodEndDate(), FIXING_END_DATE);
    assertEquals(FRA_DEFINITION_2.getFixingPeriodStartDate(), FIXING_START_DATE.plusDays(1));
    assertEquals(FRA_DEFINITION_2.getFixingPeriodEndDate(), FIXING_END_DATE.plusDays(1));
    assertEquals(FRA_DEFINITION_3.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(FRA_DEFINITION_3.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(FRA_DEFINITION_3.getIndex(), INDEX);
    assertEquals(FRA_DEFINITION_3.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    assertEquals(FRA_DEFINITION_3.getRate(), FRA_RATE);
    assertEquals(FRA_DEFINITION_3.getFixingPeriodStartDate(), FIXING_START_DATE);
    assertEquals(FRA_DEFINITION_3.getFixingPeriodEndDate(), FIXING_END_DATE);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getAccrualEndDate(), ACCRUAL_END_DATE);
    final double accrualFactorPay = DAY_COUNT_INDEX.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getPaymentYearFraction(), accrualFactorPay);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getPaymentDate(), ACCRUAL_START_DATE);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, -SETTLEMENT_DAYS, CALENDAR);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getFixingDate(), fixingDate);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getIndex(), INDEX);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getRate(), FRA_RATE);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getFixingPeriodStartDate(), ACCRUAL_START_DATE);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, INDEX, CALENDAR);
    assertEquals("ForwardRateAgreementDefinition: from", FRA_DEFINITION_4.getFixingPeriodEndDate(), fixingPeriodEndDate);
  }

  @Test
  public void fromTrade() {
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2011, 1, 3);
    final Period startPeriod = Period.ofMonths(6);
    final ForwardRateAgreementDefinition fraFromTrade = ForwardRateAgreementDefinition.fromTrade(tradeDate, startPeriod, NOTIONAL, INDEX, FRA_RATE, CALENDAR);
    final Period endPeriod = Period.ofMonths(9);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(tradeDate, SETTLEMENT_DAYS, CALENDAR);
    final ZonedDateTime accrualStartDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, INDEX, CALENDAR);
    final ZonedDateTime accrualEndDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, INDEX, CALENDAR);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -SETTLEMENT_DAYS, CALENDAR);
    final double accrualFactor = DAY_COUNT_INDEX.getDayCountFraction(accrualStartDate, accrualEndDate);
    final ForwardRateAgreementDefinition fraExpected = new ForwardRateAgreementDefinition(CUR, accrualStartDate, accrualStartDate, accrualEndDate, accrualFactor, NOTIONAL, fixingDate, INDEX,
        FRA_RATE, CALENDAR);
    assertEquals("FRA builder", fraExpected, fraFromTrade);
  }

  @Test
  public void equalHash() {
    assertTrue(FRA_DEFINITION_1.equals(FRA_DEFINITION_1));
    final ForwardRateAgreementDefinition newFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX,
        FRA_RATE, CALENDAR);
    assertEquals(newFRA.equals(FRA_DEFINITION_1), true);
    assertEquals(newFRA.hashCode() == FRA_DEFINITION_1.hashCode(), true);
    ForwardRateAgreementDefinition modifiedFRA;
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, ACCRUAL_START_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, PAYMENT_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, FIXING_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT + 0.10, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, FIXING_DATE, INDEX, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, PAYMENT_DATE, INDEX, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE + 0.10, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    final IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor");
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, otherIndex, FRA_RATE, CALENDAR);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    assertFalse(FRA_DEFINITION_1.equals(CUR));
    assertFalse(FRA_DEFINITION_1.equals(null));
  }

  @Test
  public void toDerivativeNotFixed() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, FRA_DEFINITION_1.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, FRA_DEFINITION_1.getFixingPeriodEndDate());
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        FRA_DEFINITION_1.getFixingPeriodAccrualFactor(), FRA_RATE);
    final ForwardRateAgreement convertedFra = (ForwardRateAgreement) FRA_DEFINITION_1.toDerivative(REFERENCE_DATE);
    assertEquals(convertedFra, fra);
    assertEquals(fra, convertedFra);
    final double shift = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(FIXING_DATE, FRA_RATE + shift);
    final ForwardRateAgreement convertedFra2 = (ForwardRateAgreement) FRA_DEFINITION_3.toDerivative(REFERENCE_DATE, fixingTS);
    assertEquals(fra, convertedFra2);
  }

  @Test
  public void toDerivativeFixed() {
    final ZonedDateTime referenceFixed = DateUtils.getUTCDate(2011, 1, 4);
    final ForwardRateAgreementDefinition fraFixed = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX,
        FRA_RATE, CALENDAR);
    final double shift = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(FIXING_DATE, FRA_RATE + shift);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(referenceFixed.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final CouponFixed fra = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, (FRA_RATE + shift) - FRA_RATE);
    final Payment convertedFra = fraFixed.toDerivative(referenceFixed, fixingTS);
    assertEquals(convertedFra.equals(fra), true);
  }

  @Test
  public void testPaymentCalendar() {
    // Set payment date to Good Friday, also following Monday is Easter Monday - should adjust to following Tuesday
    ForwardRateAgreementDefinition def = ForwardRateAgreementDefinition.from(DateUtils.getUTCDate(2014, 4, 18),
        DateUtils.getUTCDate(2014, 10, 18), NOTIONAL, INDEX, FRA_RATE, CALENDAR, PAY_CALENDAR);
    assertTrue(CALENDAR.isWorkingDay(DateUtils.getUTCDate(2014, 4, 18).toLocalDate()));
    assertTrue(PAY_CALENDAR.isWorkingDay(def.getPaymentDate().toLocalDate()));
    assertEquals(DateUtils.getUTCDate(2014, 4, 22), def.getPaymentDate());
  }
}

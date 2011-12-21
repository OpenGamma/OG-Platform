/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.fra;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the ForwardRateAgreementDefinition construction.
 */
public class ForwardRateAgreementDefinitionTest {

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
  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ForwardRateAgreementDefinition FRA_DEFINITION_1 = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT,
      NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  private static final ForwardRateAgreementDefinition FRA_DEFINITION_2 = ForwardRateAgreementDefinition.from(FRA_DEFINITION_1, FIXING_DATE, INDEX, FRA_RATE);
  //private static final ForwardRateAgreementDefinition FRA_DEFINITION_3 = ForwardRateAgreementDefinition.from(ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, INDEX, FRA_RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new ForwardRateAgreementDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new ForwardRateAgreementDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStart() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEnd() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, null, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, null, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncorrectCurrency() {
    final Currency EUR = Currency.EUR;
    new ForwardRateAgreementDefinition(EUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, null, FRA_RATE);
  }

  @Test
  public void getter() {
    assertEquals(FRA_DEFINITION_1.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(FRA_DEFINITION_1.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(FRA_DEFINITION_1.getIndex(), INDEX);
    assertEquals(FRA_DEFINITION_1.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    assertEquals(FRA_DEFINITION_1.getRate(), FRA_RATE);
    assertEquals(FRA_DEFINITION_1.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(FRA_DEFINITION_1.getFixindPeriodEndDate(), FIXING_END_DATE);
    assertEquals(FRA_DEFINITION_2.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(FRA_DEFINITION_2.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(FRA_DEFINITION_2.getIndex(), INDEX);
    assertEquals(FRA_DEFINITION_2.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    assertEquals(FRA_DEFINITION_2.getRate(), FRA_RATE);
    assertEquals(FRA_DEFINITION_2.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(FRA_DEFINITION_2.getFixindPeriodEndDate(), FIXING_END_DATE);
    //TODO
    //    assertEquals(FRA_DEFINITION_3.getAccrualStartDate(), ACCRUAL_START_DATE);
    //    assertEquals(FRA_DEFINITION_3.getAccrualEndDate(), ACCRUAL_END_DATE);
    //    assertEquals(FRA_DEFINITION_3.getIndex(), INDEX);
    //    assertEquals(FRA_DEFINITION_3.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    //    assertEquals(FRA_DEFINITION_3.getRate(), FRA_RATE);
    //    assertEquals(FRA_DEFINITION_3.getFixindPeriodStartDate(), FIXING_START_DATE);
    //    assertEquals(FRA_DEFINITION_3.getFixindPeriodEndDate(), FIXING_END_DATE);
  }

  @Test
  public void equalHash() {
    assertTrue(FRA_DEFINITION_1.equals(FRA_DEFINITION_1));
    final ForwardRateAgreementDefinition newFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE,
        INDEX, FRA_RATE);
    assertEquals(newFRA.equals(FRA_DEFINITION_1), true);
    assertEquals(newFRA.hashCode() == FRA_DEFINITION_1.hashCode(), true);
    ForwardRateAgreementDefinition modifiedFRA;
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, ACCRUAL_START_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, PAYMENT_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, FIXING_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT + 0.10, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, PAYMENT_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE + 0.10);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    final IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM);
    modifiedFRA = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, otherIndex, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION_1), false);
    assertFalse(FRA_DEFINITION_1.equals(CUR));
    assertFalse(FRA_DEFINITION_1.equals(null));
  }

  @Test
  public void toDerivativeNotFixed() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, FRA_DEFINITION_1.getFixindPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, FRA_DEFINITION_1.getFixindPeriodEndDate());
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        FRA_DEFINITION_1.getFixingPeriodAccrualFactor(), FRA_RATE, forwardCurve);
    final ForwardRateAgreement convertedFra = (ForwardRateAgreement) FRA_DEFINITION_1.toDerivative(REFERENCE_DATE, curves);
    assertEquals(convertedFra, fra);
    assertEquals(fra, convertedFra);
    final double shift = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FRA_RATE + shift});
    final ForwardRateAgreement convertedFra2 = (ForwardRateAgreement) FRA_DEFINITION_2.toDerivative(REFERENCE_DATE, fixingTS, curves);
    assertEquals(fra, convertedFra2);
  }

  @Test
  public void toDerivativeFixed() {
    final ZonedDateTime referenceFixed = DateUtils.getUTCDate(2011, 1, 4);
    final ForwardRateAgreementDefinition fraFixed = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX,
        FRA_RATE);
    final double shift = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FRA_RATE + shift});
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(referenceFixed), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final CouponFixed fra = new CouponFixed(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, (FRA_RATE + shift) - FRA_RATE);
    final Payment convertedFra = fraFixed.toDerivative(referenceFixed, fixingTS, curves);
    assertEquals(convertedFra.equals(fra), true);
  }
}

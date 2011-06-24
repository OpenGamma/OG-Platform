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
import com.opengamma.financial.interestrate.fra.definition.ZZZForwardRateAgreement;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests the ForwardRateAgreementDefinition construction.
 */
public class ZZZForwardRateAgreementDefinitionTest {

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
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 1, 7);
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, TENOR);
  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ZZZForwardRateAgreementDefinition FRA_DEFINITION = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT,
      NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  private static final ZZZForwardRateAgreementDefinition FRA_DEFINITION_2 = ZZZForwardRateAgreementDefinition.from(FRA_DEFINITION, FIXING_DATE, INDEX, FRA_RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new ZZZForwardRateAgreementDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new ZZZForwardRateAgreementDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStart() {
    new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEnd() {
    new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, null, INDEX, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, null, FRA_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIncorrectCurrency() {
    final Currency EUR = Currency.EUR;
    new ZZZForwardRateAgreementDefinition(EUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, null, FRA_RATE);
  }

  @Test
  public void getter() {
    assertEquals(FRA_DEFINITION.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(FRA_DEFINITION.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(FRA_DEFINITION.getIndex(), INDEX);
    assertEquals(FRA_DEFINITION.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    assertEquals(FRA_DEFINITION.getRate(), FRA_RATE);
    assertEquals(FRA_DEFINITION.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(FRA_DEFINITION.getFixindPeriodEndDate(), FIXING_END_DATE);
    assertEquals(FRA_DEFINITION_2.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(FRA_DEFINITION_2.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(FRA_DEFINITION_2.getIndex(), INDEX);
    assertEquals(FRA_DEFINITION_2.getPaymentYearFraction(), ACCRUAL_FACTOR_PAYMENT);
    assertEquals(FRA_DEFINITION_2.getRate(), FRA_RATE);
  }

  @Test
  public void equalHash() {
    assertTrue(FRA_DEFINITION.equals(FRA_DEFINITION));
    final ZZZForwardRateAgreementDefinition newFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE,
        INDEX, FRA_RATE);
    assertEquals(newFRA.equals(FRA_DEFINITION), true);
    assertEquals(newFRA.hashCode() == FRA_DEFINITION.hashCode(), true);
    ZZZForwardRateAgreementDefinition modifiedFRA;
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, ACCRUAL_START_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, PAYMENT_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, FIXING_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT + 0.10, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL + 1.0, FIXING_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, PAYMENT_DATE, INDEX, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, INDEX, FRA_RATE + 0.10);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM);
    modifiedFRA = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE, otherIndex, FRA_RATE);
    assertEquals(modifiedFRA.equals(FRA_DEFINITION), false);
    assertFalse(FRA_DEFINITION.equals(CUR));
    assertFalse(FRA_DEFINITION.equals(null));
  }

  @Test
  public void toDerivativeNotFixed() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, FRA_DEFINITION.getFixindPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, FRA_DEFINITION.getFixindPeriodEndDate());
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final ZZZForwardRateAgreement fra = new ZZZForwardRateAgreement(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, INDEX, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        FRA_DEFINITION.getFixingPeriodAccrualFactor(), FRA_RATE, forwardCurve);
    final ZZZForwardRateAgreement convertedFra = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, curves);
    assertEquals(fra, convertedFra);
    final double shift = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FRA_RATE + shift});
    final ZZZForwardRateAgreement convertedFra2 = (ZZZForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE, fixingTS, curves);
    assertEquals(fra, convertedFra2);
  }

  @Test
  public void toDerivativeFixed() {
    final ZonedDateTime referenceFixed = DateUtil.getUTCDate(2011, 1, 4);
    final ZZZForwardRateAgreementDefinition fraFixed = new ZZZForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE,
        INDEX, FRA_RATE);
    final double shift = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FRA_RATE + shift});
    //fraFixed.fixingProcess(FRA_RATE + shift);
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

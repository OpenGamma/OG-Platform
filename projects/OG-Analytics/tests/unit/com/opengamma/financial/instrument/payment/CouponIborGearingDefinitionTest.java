/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

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
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests related to the construction of Ibor coupon with gearing factor and spread and its conversion to derivative.
 */
public class CouponIborGearingDefinitionTest {
  // The index: Libor 3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, TENOR, BUSINESS_DAY, CALENDAR);
  private static final double FIXING_ACCRUAL_FACTOR = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final CouponIborGearingDefinition COUPON_DEFINITION = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      FIXING_DATE, INDEX, SPREAD, FACTOR);

  private static final double FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FIXING_RATE});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CouponIborGearingDefinition(null, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new CouponIborGearingDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStart() {
    new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEnd() {
    new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, INDEX, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, SPREAD, FACTOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurrency() {
    final Currency otherCurrency = Currency.EUR;
    new CouponIborGearingDefinition(otherCurrency, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, SPREAD, FACTOR);
  }

  @Test
  /**
   * Tests the getters.
   */
  public void getter() {
    assertEquals(CUR, COUPON_DEFINITION.getCurrency());
    assertEquals(ACCRUAL_START_DATE, COUPON_DEFINITION.getAccrualStartDate());
    assertEquals(ACCRUAL_END_DATE, COUPON_DEFINITION.getAccrualEndDate());
    assertEquals(ACCRUAL_END_DATE, COUPON_DEFINITION.getPaymentDate());
    assertEquals(ACCRUAL_FACTOR, COUPON_DEFINITION.getPaymentYearFraction());
    assertEquals(FIXING_DATE, COUPON_DEFINITION.getFixingDate());
    assertEquals(FIXING_START_DATE, COUPON_DEFINITION.getFixingPeriodStartDate());
    assertEquals(FIXING_END_DATE, COUPON_DEFINITION.getFixingPeriodEndDate());
    assertEquals(FIXING_ACCRUAL_FACTOR, COUPON_DEFINITION.getFixingPeriodAccrualFactor());
    assertEquals(INDEX, COUPON_DEFINITION.getIndex());
    assertEquals(SPREAD, COUPON_DEFINITION.getSpread());
    assertEquals(FACTOR, COUPON_DEFINITION.getFactor());
    assertEquals(SPREAD * ACCRUAL_FACTOR * NOTIONAL, COUPON_DEFINITION.getSpreadAmount());
  }

  @Test
  /**
   * Tests the equal and hash code.
   */
  public void testEqualHash() {
    final CouponIborGearingDefinition newCoupon = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD,
        FACTOR);
    assertEquals(newCoupon, COUPON_DEFINITION);
    assertEquals(newCoupon.hashCode(), COUPON_DEFINITION.hashCode());
    CouponIborGearingDefinition other;
    other = new CouponIborGearingDefinition(CUR, ACCRUAL_START_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR);
    assertFalse(COUPON_DEFINITION.equals(other));
    other = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR * 1.1, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR);
    assertFalse(COUPON_DEFINITION.equals(other));
    other = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL + 1.0, FIXING_DATE, INDEX, SPREAD, FACTOR);
    assertFalse(COUPON_DEFINITION.equals(other));
    other = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_START_DATE, INDEX, SPREAD, FACTOR);
    assertFalse(COUPON_DEFINITION.equals(other));
    other = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD * 0.0001, FACTOR);
    assertFalse(COUPON_DEFINITION.equals(other));
    other = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, SPREAD, FACTOR * 2.0);
    assertFalse(COUPON_DEFINITION.equals(other));
  }

  @Test
  /**
   * Tests the builder from an Ibor coupon.
   */
  public void fromCouponIbor() {
    final CouponIborDefinition couponIbor = new CouponIborDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
    final CouponIborGearingDefinition couponFrom = CouponIborGearingDefinition.from(couponIbor, SPREAD, FACTOR);
    assertEquals(COUPON_DEFINITION, couponFrom);
  }

  @Test
  /**
   * Tests the builder from the standard financial details.
   */
  public void fromFinancial() {
    final CouponIborGearingDefinition couponFrom = CouponIborGearingDefinition.from(ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, INDEX, SPREAD, FACTOR);
    assertEquals(COUPON_DEFINITION, couponFrom);
  }

  @Test
  /**
   * Tests the toDerivative method where the fixing date after the current date.
   */
  public void testToDerivativeBeforeFixing() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2010, 12, 27);
    final double paymentTime = actAct.getDayCountFraction(referenceDate, ACCRUAL_END_DATE);
    final double fixingTime = actAct.getDayCountFraction(referenceDate, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(referenceDate, FIXING_START_DATE);
    final double fixingPeriodEndTime = actAct.getDayCountFraction(referenceDate, FIXING_END_DATE);
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final CouponIborGearing coupon = new CouponIborGearing(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, forwardCurve);
    Payment couponConverted = COUPON_DEFINITION.toDerivative(referenceDate, curves);
    assertEquals(coupon, couponConverted);
    couponConverted = COUPON_DEFINITION.toDerivative(referenceDate, FIXING_TS, curves);
    assertEquals(coupon, couponConverted);
  }

  @Test
  /**
   * Tests the toDerivative method where the fixing date before the current date.
   */
  public void testToDerivativeAfterFixing() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 23);
    final double paymentTime = actAct.getDayCountFraction(referenceDate, ACCRUAL_END_DATE);
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final CouponFixed coupon = new CouponFixed(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, FIXING_RATE * FACTOR + SPREAD);
    final Payment couponConverted = COUPON_DEFINITION.toDerivative(referenceDate, FIXING_TS, curves);
    assertEquals(coupon, couponConverted);
  }

  @SuppressWarnings("unused")
  @Test
  /**
   * Tests the toDerivative method where the fixing date is equal to the current date.
   */
  public void testToDerivativeOnFixing() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 19);
    final double paymentTime = actAct.getDayCountFraction(referenceDate, ACCRUAL_END_DATE);
    final double fixingTime = actAct.getDayCountFraction(referenceDate, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(referenceDate, FIXING_START_DATE);
    final double fixingPeriodEndTime = actAct.getDayCountFraction(referenceDate, FIXING_END_DATE);
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    // The fixing is known
    final CouponFixed coupon = new CouponFixed(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, FIXING_RATE * FACTOR + SPREAD);
    final Payment couponConverted = COUPON_DEFINITION.toDerivative(referenceDate, FIXING_TS, curves);
    assertEquals(coupon, couponConverted);
    // The fixing is not known
    final DoubleTimeSeries<ZonedDateTime> fixingTS2 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {ScheduleCalculator.getAdjustedDate(FIXING_DATE, -1, CALENDAR)},
        new double[] {FIXING_RATE});
    final CouponIborGearing coupon2 = new CouponIborGearing(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, fixingTime, INDEX, fixingPeriodStartTime, fixingPeriodEndTime,
        FIXING_ACCRUAL_FACTOR, SPREAD, FACTOR, forwardCurve);
    final Payment couponConverted2 = COUPON_DEFINITION.toDerivative(referenceDate, fixingTS2, curves);
    //assertEquals(coupon2, couponConverted2);
    final Payment couponConverted3 = COUPON_DEFINITION.toDerivative(referenceDate, curves);
    //assertEquals(coupon2, couponConverted3);
  }

}

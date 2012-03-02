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
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Test related to the construction and the conversion of Ibor coupon with spread.
 */
public class CouponIborSpreadDefinitionTest {

  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double SPREAD = -0.001; // -10 bps
  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON_DEFINITION = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);
  private static final CouponIborSpreadDefinition IBOR_COUPON_SPREAD_DEFINITION = CouponIborSpreadDefinition.from(IBOR_COUPON_DEFINITION, SPREAD);
  private static final double FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FIXING_RATE});
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    CouponIborSpreadDefinition.from(null, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionAfterFixingNoData() {
    IBOR_COUPON_SPREAD_DEFINITION.toDerivative(FIXING_DATE.plusDays(3), new String[] {"A", "B"});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullTS() {
    IBOR_COUPON_SPREAD_DEFINITION.toDerivative(FIXING_DATE, null, new String[] {"A", "B"});
  }

  @Test
  public void testGetter() {
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getSpread(), SPREAD, 1E-10);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getSpreadAmount(), SPREAD * NOTIONAL * IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), 1E-2);
  }

  @Test
  public void testObject() {
    CouponIborSpreadDefinition other = CouponIborSpreadDefinition.from(IBOR_COUPON_DEFINITION, SPREAD);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION, other);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.hashCode(), other.hashCode());
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION, other);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.hashCode(), other.hashCode());
    other = new CouponIborSpreadDefinition(Currency.AUD, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, new IborIndex(Currency.AUD, TENOR, SETTLEMENT_DAYS, CALENDAR,
            DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM), SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate().plusDays(1), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate().plusDays(1),
        IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate()
        .plusDays(1), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction() + 0.01, NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL + 100, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE.plusDays(1), INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, new IborIndex(Currency.USD, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM), SPREAD);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD + 0.01);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final double paymentTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, IBOR_COUPON_DEFINITION.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, IBOR_COUPON_DEFINITION.getFixingPeriodEndDate());
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final CouponIbor couponIbor = new CouponIbor(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, fixingTime, INDEX, fixingPeriodStartTime,
        fixingPeriodEndTime, IBOR_COUPON_SPREAD_DEFINITION.getFixingPeriodAccrualFactor(), SPREAD, forwardCurve);
    CouponIbor convertedDefinition = (CouponIbor) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, curves);
    assertEquals(couponIbor, convertedDefinition);
    convertedDefinition = (CouponIbor) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS, curves);
    assertEquals(couponIbor, convertedDefinition);
  }

  @Test
  public void testToDerivativeAfterFixing() {
    final ZonedDateTime date = FIXING_DATE.plusDays(2);
    double paymentTime = TimeCalculator.getTimeBetween(date, PAYMENT_DATE);
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    CouponFixed convertedDefinition = (CouponFixed) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(date, FIXING_TS, curves);
    assertEquals(couponFixed, convertedDefinition);
    paymentTime = TimeCalculator.getTimeBetween(FIXING_DATE, PAYMENT_DATE);
    couponFixed = new CouponFixed(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    convertedDefinition = (CouponFixed) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(FIXING_DATE, FIXING_TS, curves);
    assertEquals(couponFixed, convertedDefinition);
  }

  @Test
  /**
   * Tests the toDerivative method where the fixing date is equal to the current date.
   */
  public void testToDerivativeOnFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 1, 3, 12, 5);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    final double fixingTime = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(referenceDate, IBOR_COUPON_DEFINITION.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(referenceDate, IBOR_COUPON_DEFINITION.getFixingPeriodEndDate());
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    // The fixing is known
    final CouponFixed coupon = new CouponFixed(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    final Payment couponConverted = IBOR_COUPON_SPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS, curves);
    assertEquals(coupon, couponConverted);
    // The fixing is not known
    final DoubleTimeSeries<ZonedDateTime> fixingTS2 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {ScheduleCalculator.getAdjustedDate(FIXING_DATE, -1, CALENDAR)},
        new double[] {FIXING_RATE});
    final CouponIbor coupon2 = new CouponIbor(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, fixingTime, INDEX, fixingPeriodStartTime,
        fixingPeriodEndTime, IBOR_COUPON_SPREAD_DEFINITION.getFixingPeriodAccrualFactor(), SPREAD, forwardCurve);
    final Payment couponConverted2 = IBOR_COUPON_SPREAD_DEFINITION.toDerivative(referenceDate, fixingTS2, curves);
    assertEquals("CouponIborGearingDefinition: toDerivative", coupon2, couponConverted2);
    final Payment couponConverted3 = IBOR_COUPON_SPREAD_DEFINITION.toDerivative(referenceDate, curves);
    assertEquals("CouponIborGearingDefinition: toDerivative", coupon2, couponConverted3);
  }

}

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
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * 
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

  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double SPREAD = -0.001; // -10 bps
  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);
  private static final CouponIborSpreadDefinition IBOR_COUPON_SPREAD = CouponIborSpreadDefinition.from(IBOR_COUPON, SPREAD);
  private static final double FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {FIXING_DATE}, new double[] {FIXING_RATE});
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    CouponIborSpreadDefinition.from(null, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionAfterFixingNoData() {
    IBOR_COUPON_SPREAD.toDerivative(FIXING_DATE.plusDays(3), new String[] {"A", "B"});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullTS() {
    IBOR_COUPON_SPREAD.toDerivative(FIXING_DATE, null, new String[] {"A", "B"});
  }

  @Test
  public void testGetter() {
    assertEquals(IBOR_COUPON_SPREAD.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_COUPON_SPREAD.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_COUPON_SPREAD.getSpread(), SPREAD, 1E-10);
    assertEquals(IBOR_COUPON_SPREAD.getSpreadAmount(), SPREAD * NOTIONAL * IBOR_COUPON_SPREAD.getPaymentYearFraction(), 1E-2);
  }

  @Test
  public void testObject() {
    CouponIborSpreadDefinition other = CouponIborSpreadDefinition.from(IBOR_COUPON, SPREAD);
    assertEquals(IBOR_COUPON_SPREAD, other);
    assertEquals(IBOR_COUPON_SPREAD.hashCode(), other.hashCode());
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertEquals(IBOR_COUPON_SPREAD, other);
    assertEquals(IBOR_COUPON_SPREAD.hashCode(), other.hashCode());
    other = new CouponIborSpreadDefinition(Currency.AUD, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, new IborIndex(Currency.AUD, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM), SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate().plusDays(1), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate().plusDays(1), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate().plusDays(1),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction() + 0.01, NOTIONAL, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL + 100, FIXING_DATE, INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE.plusDays(1), INDEX, SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, new IborIndex(Currency.USD, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM), SPREAD);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD.getPaymentDate(), IBOR_COUPON_SPREAD.getAccrualStartDate(), IBOR_COUPON_SPREAD.getAccrualEndDate(),
        IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD + 0.01);
    assertFalse(IBOR_COUPON_SPREAD.equals(other));
  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final double paymentTime = actAct.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = actAct.getDayCountFraction(REFERENCE_DATE, FIXING_DATE);
    final double fixingPeriodStartTime = actAct.getDayCountFraction(REFERENCE_DATE, IBOR_COUPON.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(REFERENCE_DATE, IBOR_COUPON.getFixingPeriodEndDate());
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    final CouponIbor couponIbor = new CouponIbor(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        IBOR_COUPON_SPREAD.getFixingPeriodAccrualFactor(), SPREAD, forwardCurve);
    CouponIbor convertedDefinition = (CouponIbor) IBOR_COUPON_SPREAD.toDerivative(REFERENCE_DATE, curves);
    assertEquals(couponIbor, convertedDefinition);
    convertedDefinition = (CouponIbor) IBOR_COUPON_SPREAD.toDerivative(REFERENCE_DATE, FIXING_TS, curves);
    assertEquals(couponIbor, convertedDefinition);
  }

  @Test
  public void testToDerivativeAfterFixing() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime date = FIXING_DATE.plusDays(2);
    double paymentTime = actAct.getDayCountFraction(date, PAYMENT_DATE);
    final String fundingCurve = "Funding";
    final String forwardCurve = "Forward";
    final String[] curves = {fundingCurve, forwardCurve};
    CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    CouponFixed convertedDefinition = (CouponFixed) IBOR_COUPON_SPREAD.toDerivative(date, FIXING_TS, curves);
    assertEquals(couponFixed, convertedDefinition);
    paymentTime = actAct.getDayCountFraction(FIXING_DATE, PAYMENT_DATE);
    couponFixed = new CouponFixed(CUR, paymentTime, fundingCurve, IBOR_COUPON_SPREAD.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    convertedDefinition = (CouponFixed) IBOR_COUPON_SPREAD.toDerivative(FIXING_DATE, FIXING_TS, curves);
    assertEquals(couponFixed, convertedDefinition);
  }
}

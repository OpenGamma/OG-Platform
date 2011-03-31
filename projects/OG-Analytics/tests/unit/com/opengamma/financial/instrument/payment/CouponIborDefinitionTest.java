/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class CouponIborDefinitionTest {
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(FIXING_START_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, TENOR);

  private static final DayCount DAY_COUNT_PAYMENT = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double ACCRUAL_FACTOR_FIXING = DAY_COUNT_INDEX.getDayCountFraction(FIXING_START_DATE, FIXING_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  // Coupon with specific payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON = CouponIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON_2 = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CouponIborDefinition.from(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    CouponIborDefinition.from(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    CouponIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    CouponIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    CouponIborDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullFixingDate() {
    CouponIborDefinition.from(NOTIONAL, null, INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullIndex() {
    CouponIborDefinition.from(NOTIONAL, FIXING_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingAfterPayment() {
    CouponIborDefinition.from(FIXING_DATE.minusDays(1), ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
  }

  @Test
  public void test() {
    assertEquals(IBOR_COUPON.getPaymentDate(), PAYMENT_DATE);
    assertEquals(IBOR_COUPON.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(IBOR_COUPON.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(IBOR_COUPON.getPaymentYearFraction(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(IBOR_COUPON.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_COUPON.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_COUPON.isFixed(), false);
    assertEquals(IBOR_COUPON.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_COUPON.getFixindPeriodEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_COUPON.getFixingPeriodAccrualFactor(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertEquals(IBOR_COUPON_2.getPaymentDate(), FIXING_END_DATE);
    assertEquals(IBOR_COUPON_2.getAccrualStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_COUPON_2.getAccrualEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_COUPON_2.getPaymentYearFraction(), ACCRUAL_FACTOR_FIXING, 1E-10);
    assertEquals(IBOR_COUPON_2.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_COUPON_2.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_COUPON_2.isFixed(), false);
    assertEquals(IBOR_COUPON_2.getFixindPeriodStartDate(), FIXING_START_DATE);
    assertEquals(IBOR_COUPON_2.getFixindPeriodEndDate(), FIXING_END_DATE);
    assertEquals(IBOR_COUPON_2.getFixingPeriodAccrualFactor(), ACCRUAL_FACTOR_FIXING, 1E-10);
  }

  @Test
  public void testEqualHash() {
    //TODO
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    double fixingPeriodStartTime = actAct.getDayCountFraction(zonedDate, IBOR_COUPON.getFixindPeriodStartDate());
    double fixingPeriodEndTime = actAct.getDayCountFraction(zonedDate, IBOR_COUPON.getFixindPeriodEndDate());
    String fundingCurve = "Funding";
    String forwardCurve = "Forward";
    String[] curves = {fundingCurve, forwardCurve};
    CouponIbor couponIbor = new CouponIbor(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, ACCRUAL_FACTOR_FIXING, forwardCurve);
    CouponIbor convertedDefinition = (CouponIbor) IBOR_COUPON.toDerivative(REFERENCE_DATE, curves);
    assertEquals(couponIbor, convertedDefinition);
  }
}

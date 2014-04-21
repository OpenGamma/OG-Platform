/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.generator.USDDeposit;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
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
 * Tests the constructors and equal/hash for CouponFixedDefinition.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedDefinitionTest {
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double RATE = 0.04;
  private static final ZonedDateTime FAKE_DATE = DateUtils.getUTCDate(0, 1, 1);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BD_CONVENTION = BusinessDayConventions.FOLLOWING;
  private static final IborIndex INDEX = new IborIndex(CUR, Period.ofMonths(6), 0, DAY_COUNT, BD_CONVENTION, false ,"Ibor");
  private static final CouponFloatingDefinition COUPON = new CouponIborDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE, INDEX, CALENDAR);
  private static final CouponFixedDefinition FIXED_COUPON = new CouponFixedDefinition(COUPON, RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test
  public void test() {
    assertEquals(FIXED_COUPON.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(FIXED_COUPON.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(FIXED_COUPON.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(FIXED_COUPON.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(FIXED_COUPON.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(FIXED_COUPON.getRate(), RATE, 1E-10);
    assertEquals(FIXED_COUPON.getAmount(), RATE * NOTIONAL * ACCRUAL_FACTOR, 1E-10);
  }

  @Test
  public void fromGeneratorDeposit() {
    final GeneratorDeposit generator = new USDDeposit(CALENDAR);
    final Period tenor = Period.ofMonths(3);
    final CouponFixedDefinition cpnFixed = CouponFixedDefinition.from(ACCRUAL_START_DATE, tenor, generator, NOTIONAL, RATE);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, tenor, generator.getBusinessDayConvention(), CALENDAR, generator.isEndOfMonth());
    final double accrual = generator.getDayCount().getDayCountFraction(ACCRUAL_START_DATE, endDate);
    final CouponFixedDefinition cpnExpected = new CouponFixedDefinition(generator.getCurrency(), endDate, ACCRUAL_START_DATE, endDate, accrual, NOTIONAL, RATE);
    assertEquals("CouponFixedDefinition: from deposit generator", cpnExpected, cpnFixed);
  }

  @Test
  public void testEqualHash() {
    final CouponFixedDefinition comparedCoupon = new CouponFixedDefinition(COUPON, RATE);
    assertEquals(comparedCoupon, FIXED_COUPON);
    assertEquals(comparedCoupon.hashCode(), FIXED_COUPON.hashCode());
    final CouponFixedDefinition modifiedCoupon = new CouponFixedDefinition(COUPON, RATE + 0.01);
    assertFalse(FIXED_COUPON.equals(modifiedCoupon));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testToDerivativeDeprecated() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double paymentTime = actAct.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
    final String fundingCurve = "Funding";
    final CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, RATE, FIXED_COUPON.getAccrualStartDate(), FIXED_COUPON.getAccrualEndDate());
    final CouponFixed convertedDefinition = FIXED_COUPON.toDerivative(REFERENCE_DATE, fundingCurve);
    assertEquals(couponFixed, convertedDefinition);
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double paymentTime = actAct.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
    final CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, RATE, FIXED_COUPON.getAccrualStartDate(), FIXED_COUPON.getAccrualEndDate());
    final CouponFixed convertedDefinition = FIXED_COUPON.toDerivative(REFERENCE_DATE);
    assertEquals(couponFixed, convertedDefinition);
  }
}

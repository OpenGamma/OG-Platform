/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
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
 * Tests of the CMS coupons.
 */
@Test(groups = TestGroup.UNIT)
public class CouponCMSTest {
  //Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2014, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2014, 6, 17); // Prefixed
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE;
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponCMS CMS_COUPON_RECEIVER = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);

  @Test
  public void testGetter() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    assertEquals(fixingTime, CMS_COUPON_RECEIVER.getFixingTime(), 1E-10);
    assertEquals(SWAP, CMS_COUPON_RECEIVER.getUnderlyingSwap());
    assertEquals(NOTIONAL, CMS_COUPON_RECEIVER.getNotional(), 1E-10);
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 10000;
    final CouponCMS coupon = new CouponCMS(CUR, 0.25, 0.25, NOTIONAL, 0.25, SWAP, 0.25);
    final CouponCMS expected = new CouponCMS(CUR, 0.25, 0.25, notional, 0.25, SWAP, 0.25);
    assertEquals(expected, coupon.withNotional(notional));
  }

  @Test
  public void testHashCodeEquals() {
    final double paymentTime = 1.5;
    final double paymentYearFraction = 0.25;
    final double fixingTime = 0.245;
    final double settlementTime = 1.51;
    final CouponCMS coupon = new CouponCMS(CUR, paymentTime, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    CouponCMS other = new CouponCMS(CUR, paymentTime, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertEquals(coupon, other);
    assertEquals(coupon.hashCode(), other.hashCode());
    other = new CouponCMS(Currency.AUD, paymentTime, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime + 1e-8, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, paymentYearFraction + 0.1, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, paymentYearFraction, NOTIONAL + 10000, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, paymentYearFraction, NOTIONAL, fixingTime + 1e-8, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, paymentYearFraction, NOTIONAL, fixingTime, SWAP.withNotional(NOTIONAL + 1000), settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime + 1e-8);
    assertFalse(other.equals(coupon));
  }

}

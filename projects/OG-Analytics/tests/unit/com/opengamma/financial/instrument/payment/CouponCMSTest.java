/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.swap.FixedFloatSwapDefinition;
import com.opengamma.financial.instrument.swap.FixedSwapLegDefinition;
import com.opengamma.financial.instrument.swap.FloatingSwapLegDefinition;
import com.opengamma.financial.instrument.swap.SwapConvention;
import com.opengamma.util.time.DateUtil;

/**
 * Test related to CouponCMSDefinition construction.
 */
public class CouponCMSTest {
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_SWAP = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final boolean IS_EOM = true;
  private static final String NAME = "CONVENTION";
  private static final SwapConvention CONVENTION = new SwapConvention(SETTLEMENT_DAYS, DAY_COUNT_SWAP, BUSINESS_DAY, CALENDAR, IS_EOM, NAME);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime[] NOMINAL_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 3), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 3), DateUtil.getUTCDate(2015, 7, 3)};
  private static final ZonedDateTime[] SETTLEMENT_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 3), DateUtil.getUTCDate(2011, 7, 4), DateUtil.getUTCDate(2012, 1, 3),
      DateUtil.getUTCDate(2012, 7, 3), DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3),
      DateUtil.getUTCDate(2015, 1, 5), DateUtil.getUTCDate(2015, 7, 3)};
  private static final ZonedDateTime[] RESET_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 1), DateUtil.getUTCDate(2011, 7, 1), DateUtil.getUTCDate(2012, 1, 2),
      DateUtil.getUTCDate(2012, 7, 2), DateUtil.getUTCDate(2013, 1, 1), DateUtil.getUTCDate(2013, 7, 1), DateUtil.getUTCDate(2014, 1, 1), DateUtil.getUTCDate(2014, 7, 1),
      DateUtil.getUTCDate(2015, 1, 1), DateUtil.getUTCDate(2015, 7, 1)};
  private static final ZonedDateTime[] MATURITY_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 7, 4), DateUtil.getUTCDate(2012, 1, 3), DateUtil.getUTCDate(2012, 7, 3),
      DateUtil.getUTCDate(2013, 1, 3), DateUtil.getUTCDate(2013, 7, 3), DateUtil.getUTCDate(2014, 1, 3), DateUtil.getUTCDate(2014, 7, 3), DateUtil.getUTCDate(2015, 1, 5),
      DateUtil.getUTCDate(2015, 7, 3), DateUtil.getUTCDate(2016, 1, 4)};
  private static final double NOTIONAL_SWAP = 1;
  private static final double RATE = 0.0001; // 0 is not authorized
  private static final FixedSwapLegDefinition FIXED_DEFINITION = new FixedSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, NOTIONAL_SWAP, RATE, CONVENTION);
  private static final double INITIAL_RATE = 0.00;
  private static final double SPREAD = 0.00;
  private static final FloatingSwapLegDefinition FLOAT_DEFINITION = new FloatingSwapLegDefinition(EFFECTIVE_DATE, NOMINAL_DATES, SETTLEMENT_DATES, RESET_DATES, MATURITY_DATES, NOTIONAL_SWAP,
      INITIAL_RATE, SPREAD, CONVENTION);
  private static final FixedFloatSwapDefinition SWAP = new FixedFloatSwapDefinition(FIXED_DEFINITION, FLOAT_DEFINITION);

  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  private static final ZonedDateTime FAKE_DATE = DateUtil.getUTCDate(0, 1, 1);
  private static final CouponFloatingDefinition COUPON = new CouponFloatingDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE);
  private static final CouponFloatingDefinition FLOAT_COUPON = CouponFloatingDefinition.from(COUPON, FIXING_DATE);

  private static final CouponCMSDefinition CMS_COUPON = new CouponCMSDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  private static final CouponCMSDefinition CMS_COUPON_2 = CouponCMSDefinition.from(FLOAT_COUPON, SWAP);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new CouponCMSDefinition(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    new CouponCMSDefinition(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    new CouponCMSDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new CouponCMSDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    new CouponCMSDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromNullCoupon() {
    CouponCMSDefinition.from(null, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromNullSwap() {
    FixedFloatSwapDefinition NullSwap = null; // To remove .from ambiguity
    CouponCMSDefinition.from(FLOAT_COUPON, NullSwap);
  }

  @Test
  public void test() {
    assertEquals(CMS_COUPON.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(CMS_COUPON.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(CMS_COUPON.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(CMS_COUPON.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(CMS_COUPON.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(CMS_COUPON.getFixingDate(), FIXING_DATE);
    assertEquals(CMS_COUPON.isFixed(), false);
    assertEquals(CMS_COUPON.getUnderlyingSwap(), SWAP);
    assertEquals(CMS_COUPON_2.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(CMS_COUPON_2.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(CMS_COUPON_2.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(CMS_COUPON_2.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(CMS_COUPON_2.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(CMS_COUPON_2.getFixingDate(), FIXING_DATE);
    assertEquals(CMS_COUPON_2.isFixed(), false);
    assertEquals(CMS_COUPON_2.getUnderlyingSwap(), SWAP);
  }

  @Test
  public void testFixingProcess() {
    CouponFloatingDefinition CouponWithReset = CouponCMSDefinition.from(FLOAT_COUPON, SWAP);
    double RESET_RATE = 0.04;
    assertEquals(CouponWithReset.isFixed(), false);
    CouponWithReset.fixingProcess(RESET_RATE);
    assertEquals(CouponWithReset.isFixed(), true);
    assertEquals(CouponWithReset.getFixedRate(), RESET_RATE, 1E-10);

  }

}

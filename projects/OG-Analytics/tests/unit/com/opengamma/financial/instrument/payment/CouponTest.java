/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class CouponTest {

  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  private static final CouponDefinition COUPON = new CouponDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL);
  private static final CouponDefinition COUPON_2 = new CouponDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_FACTOR, NOTIONAL);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new CouponDefinition(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentDateConstructor2() {
    new CouponDefinition(null, ACCRUAL_START_DATE, ACCRUAL_FACTOR, NOTIONAL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    new CouponDefinition(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccrualStartDateConstructor2() {
    new CouponDefinition(PAYMENT_DATE, null, ACCRUAL_FACTOR, NOTIONAL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    new CouponDefinition(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL);
  }

  @Test
  public void test() {
    assertEquals(COUPON.getPaymentDate(), PAYMENT_DATE);
    assertEquals(COUPON.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(COUPON.getAccrualEndDate(), ACCRUAL_END_DATE);
    assertEquals(COUPON.getAccrualFactor(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(COUPON.getNotional(), NOTIONAL, 1E-2);

    assertEquals(COUPON_2.getPaymentDate(), PAYMENT_DATE);
    assertEquals(COUPON_2.getAccrualStartDate(), ACCRUAL_START_DATE);
    assertEquals(COUPON_2.getAccrualEndDate(), PAYMENT_DATE);
    assertEquals(COUPON_2.getAccrualFactor(), ACCRUAL_FACTOR, 1E-10);
    assertEquals(COUPON_2.getNotional(), NOTIONAL, 1E-2);
  }

}

/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class CouponFixedDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double RATE = 0.04;

  private static final ZonedDateTime FAKE_DATE = DateUtil.getUTCDate(0, 1, 1);

  private static final CouponFloatingDefinition COUPON = new CouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE);
  private static final CouponFixedDefinition FIXED_COUPON = new CouponFixedDefinition(COUPON, RATE);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative

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
  public void testEqualHash() {
    CouponFixedDefinition comparedCoupon = new CouponFixedDefinition(COUPON, RATE);
    assertEquals(comparedCoupon, FIXED_COUPON);
    assertEquals(comparedCoupon.hashCode(), FIXED_COUPON.hashCode());
    CouponFixedDefinition modifiedCoupon = new CouponFixedDefinition(COUPON, RATE + 0.01);
    assertFalse(FIXED_COUPON.equals(modifiedCoupon));
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    String fundingCurve = "Funding";
    CouponFixed couponFixed = new CouponFixed(paymentTime, fundingCurve, ACCRUAL_FACTOR, NOTIONAL, RATE);
    CouponFixed convertedDefinition = FIXED_COUPON.toDerivative(REFERENCE_DATE, fundingCurve);
    assertEquals(couponFixed, convertedDefinition);
  }

}

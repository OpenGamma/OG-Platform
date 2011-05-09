package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

public class CouponFloatingTest {
  private static final Currency CCY = Currency.AUD;
  private static final double PAYMENT_TIME = 0.5;
  private static final String NAME = "D";
  private static final double ACCRUAL_TIME = 0.51;
  private static final double NOTIONAL = 100;
  private static final double FIXING_TIME = 0.1;
  private static final CouponFloating COUPON = new CouponFloating(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL, FIXING_TIME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeFixingTime() {
    new CouponFloating(CCY, PAYMENT_TIME, NAME, PAYMENT_TIME, NOTIONAL, -FIXING_TIME);
  }

  @Test
  public void testObject() {
    assertEquals(CCY, COUPON.getCurrency());
    assertEquals(FIXING_TIME, COUPON.getFixingTime(), 0);
    assertEquals(NAME, COUPON.getFundingCurveName());
    assertEquals(NOTIONAL, COUPON.getNotional(), 0);
    assertEquals(PAYMENT_TIME, COUPON.getPaymentTime(), 0);
    assertEquals(ACCRUAL_TIME, COUPON.getPaymentYearFraction(), 0);
    assertEquals(NOTIONAL, COUPON.getReferenceAmount(), 0);
    CouponFloating other = new CouponFloating(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL, FIXING_TIME);
    assertEquals(COUPON, other);
    assertEquals(COUPON.hashCode(), other.hashCode());
    assertEquals(COUPON.toString(), "\n Currency=AUD, Payment time=0.5, Funding curve=D, year fraction = 0.51, notional = 100.0, fixing time = 0.1");
    other = new CouponFloating(Currency.CAD, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL, FIXING_TIME);
    assertFalse(other.equals(COUPON));
    other = new CouponFloating(CCY, PAYMENT_TIME + 1, NAME, ACCRUAL_TIME, NOTIONAL, FIXING_TIME);
    assertFalse(other.equals(COUPON));
    other = new CouponFloating(CCY, PAYMENT_TIME, NAME + "_", ACCRUAL_TIME, NOTIONAL, FIXING_TIME);
    assertFalse(other.equals(COUPON));
    other = new CouponFloating(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME + 1, NOTIONAL, FIXING_TIME);
    assertFalse(other.equals(COUPON));
    other = new CouponFloating(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL + 1, FIXING_TIME);
    assertFalse(other.equals(COUPON));
    other = new CouponFloating(CCY, PAYMENT_TIME, NAME, ACCRUAL_TIME, NOTIONAL, FIXING_TIME + 1);
    assertFalse(other.equals(COUPON));
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.interestrate.payments.CouponFixed;

/**
 * 
 */
public class BondForwardTest {
  private static final double COUPON = 0.01;
  private static final double[] PAYMENT_TIMES = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final String YIELD_CURVE_NAME = "A";
  private static final Bond BOND = new Bond(PAYMENT_TIMES, COUPON, YIELD_CURVE_NAME);
  private static final double FORWARD_DATE = 0.12;
  private static final double ACCRUED_INTEREST = 0.2;
  private static final double ACCRUED_INTEREST_AT_DELIVERY = 0.3;
  private static final CouponFixed[] PAYMENTS = new CouponFixed[] {new CouponFixed(0.5, YIELD_CURVE_NAME, 0.5, 0.03)};
  private static final BondForward FORWARD = new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBond() {
    new BondForward(null, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeForwardDate() {
    new BondForward(BOND, -FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiredCoupons() {
    new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiredCoupon() {
    new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, new CouponFixed[] {null});
  }

  @Test
  public void test() {
    assertEquals(BOND, FORWARD.getBond());
    assertEquals(FORWARD_DATE, FORWARD.getForwardTime(), 0);
    assertEquals(ACCRUED_INTEREST, FORWARD.getAccruedInterest(), 0);
    assertArrayEquals(PAYMENTS, FORWARD.getTimeBetweenExpiredCoupons());
    BondForward other = new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);
    assertEquals(other, FORWARD);
    assertEquals(other.hashCode(), FORWARD.hashCode());
    other = new BondForward(new Bond(new double[] {1, 2, 3, 4}, COUPON, YIELD_CURVE_NAME), FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);
    assertFalse(other.equals(FORWARD));
    other = new BondForward(BOND, FORWARD_DATE + 1, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);
    assertFalse(other.equals(FORWARD));
    other = new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST + 0.1, ACCRUED_INTEREST_AT_DELIVERY, PAYMENTS);
    assertFalse(other.equals(FORWARD));
    other = new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY + 0.1, PAYMENTS);
    assertFalse(other.equals(FORWARD));
    other = new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, new CouponFixed[0]);
    assertFalse(other.equals(FORWARD));
    final BondForward forward = new BondForward(BOND, FORWARD_DATE, ACCRUED_INTEREST, ACCRUED_INTEREST_AT_DELIVERY, new CouponFixed[0]);
    assertEquals(other, forward);
    assertEquals(forward.hashCode(), forward.hashCode());
  }
}

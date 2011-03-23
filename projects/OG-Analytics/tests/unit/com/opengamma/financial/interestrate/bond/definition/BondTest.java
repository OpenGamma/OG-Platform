/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import java.util.Arrays;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * 
 */
public class BondTest {
  private static final String BOND_CURVE = "A curve";
  private static final double COUPON = 0.2;
  private static final double YEAR_FRACTION = 1.0;
  private static final double[] COUPONS;
  private static final double[] TIMES = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] YEAR_FRACTIONS;
  private static final double[] PAYMENTS;
  private static final Bond BOND = new Bond(TIMES, COUPON, BOND_CURVE);
  private static final GenericAnnuity<PaymentFixed> ANNUITY;
  private static final GenericAnnuity<CouponFixed> COUPON_ANNUITY;
  private static GenericAnnuity<CouponFixed> UNIT_ANNUITY;

  static {
    final int n = TIMES.length;
    PAYMENTS = new double[n];
    COUPONS = new double[n];
    YEAR_FRACTIONS = new double[n];
    Arrays.fill(COUPONS, COUPON);
    Arrays.fill(YEAR_FRACTIONS, YEAR_FRACTION);
    final PaymentFixed[] fixedPayments = new PaymentFixed[n + 1];
    final CouponFixed[] couponPayments = new CouponFixed[n];
    final CouponFixed[] unit = new CouponFixed[n];
    CouponFixed payment;
    for (int i = 0; i < n; i++) {
      PAYMENTS[i] = COUPON * YEAR_FRACTIONS[i] + (i == (n - 1) ? 1.0 : 0.0);
      payment = new CouponFixed(TIMES[i], BOND_CURVE, YEAR_FRACTIONS[i], COUPON);
      fixedPayments[i] = payment;
      couponPayments[i] = payment;
      unit[i] = new CouponFixed(TIMES[i], BOND_CURVE, YEAR_FRACTIONS[i], 1);
    }
    fixedPayments[n] = new PaymentFixed(TIMES[n - 1], 1, BOND_CURVE);
    ANNUITY = new GenericAnnuity<PaymentFixed>(fixedPayments);
    COUPON_ANNUITY = new GenericAnnuity<CouponFixed>(couponPayments);
    UNIT_ANNUITY = new GenericAnnuity<CouponFixed>(unit);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes1() {
    new Bond(null, COUPON, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes2() {
    new Bond(null, COUPON, YEAR_FRACTION, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes3() {
    new Bond(null, COUPONS, YEAR_FRACTIONS, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTimes1() {
    new Bond(new double[0], COUPON, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTimes2() {
    new Bond(new double[0], COUPON, YEAR_FRACTION, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTimes3() {
    new Bond(new double[0], COUPONS, YEAR_FRACTIONS, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupons1() {
    new Bond(TIMES, null, YEAR_FRACTIONS, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCoupons1() {
    new Bond(TIMES, new double[0], YEAR_FRACTIONS, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYearFraction1() {
    new Bond(TIMES, COUPONS, null, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyYearFraction1() {
    new Bond(TIMES, COUPONS, new double[0], 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    new Bond(TIMES, COUPON, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    new Bond(TIMES, COUPON, YEAR_FRACTION, 0.0, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName3() {
    new Bond(TIMES, COUPONS, YEAR_FRACTIONS, 0.0, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCoupons1() {
    new Bond(TIMES, new double[] {1, 2, 3, 4, 5}, YEAR_FRACTIONS, 0.0, BOND_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongYearFraction1() {
    new Bond(TIMES, COUPONS, new double[] {1, 2, 3, 4, 5}, 0.0, BOND_CURVE);
  }

  @Test
  public void testGetters() {
    assertEquals(BOND.getPrinciplePayment().getFundingCurveName(), BOND_CURVE);
    assertEquals(0.0, BOND.getAccruedInterest(), 0.0);
    assertEquals(BOND.getPrinciplePayment().getPaymentTime(), TIMES[TIMES.length - 1], 0.0);
    final Bond other = new Bond(TIMES, COUPONS, YEAR_FRACTIONS, 0.1, BOND_CURVE);
    assertEquals(0.1, other.getAccruedInterest(), 0.0);
    assertEquals(BOND.getAnnuity(), ANNUITY);
    assertEquals(BOND.getCouponAnnuity(), COUPON_ANNUITY);
    assertEquals(BOND.getUnitCouponAnnuity(), UNIT_ANNUITY);
  }

  @Test
  public void testHashCodeAndEquals() {
    Bond other = new Bond(TIMES, COUPON, BOND_CURVE);
    assertEquals(other, BOND);
    assertEquals(other.hashCode(), BOND.hashCode());
    other = new Bond(new double[] {1, 2, 3, 4, 5, 6, 7, 8.1, 9, 10}, COUPON, BOND_CURVE);
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, TIMES, YEAR_FRACTIONS, 0.0, BOND_CURVE);
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, COUPON, "sfdfsdfs");
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, COUPONS, YEAR_FRACTIONS, 0.1, BOND_CURVE);
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, COUPONS, YEAR_FRACTIONS, 0.0, BOND_CURVE);
    assertEquals(other, BOND);
  }

}

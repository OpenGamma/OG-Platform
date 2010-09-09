/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;

/**
 * 
 */
public class FixedFloatSwapTest {
  public static final String FUNDING_CURVE_NAME = "funding";
  public static final String LIBOR_CURVE_NAME = "Libor";
  private static final double[] FIXED_PAYMENTS = new double[] {1.5, 2, 3, 4, 5, 6};
  private static final double COUPON_RATE = 0.04;
  private static final double[] FLOAT_PAYMENTS = new double[] {1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
  private static final double[] INDEX_FIXING = new double[] {0.0, 1.5, 2., 3., 4., 5., 6., 7., 8., 9., 10., 11.};
  private static final double[] INDEX_MATURITY = new double[] {1.5, 2, 3., 4., 5., 6., 7., 8., 9, 10., 11., 12.};
  private static final double[] YEAR_FRACS = new double[] {1.5, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

  private static final FixedFloatSwap SWAP = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE,
      FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixedPayments() {
    new FixedFloatSwap(null, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFloatPayments() {
    new FixedFloatSwap(FIXED_PAYMENTS, null, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurveName() {
    new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, null, FUNDING_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborCurveName() {
    new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyFixedPayments() {
    new FixedFloatSwap(new double[0], FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyFloatPayments() {
    new FixedFloatSwap(FIXED_PAYMENTS, new double[0], COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
  }

  @Test
  public void testHashCodeAndEquals() {
    FixedFloatSwap other = new FixedFloatSwap(Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length), Arrays.copyOf(
        FLOAT_PAYMENTS, FLOAT_PAYMENTS.length), COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
    other = new FixedFloatSwap(new double[] {1, 2, 3, 4, 5, 6}, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME,
        LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, new double[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, COUPON_RATE,
        FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, 0.03, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, LIBOR_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
    assertFalse(other.equals(SWAP));

    ConstantCouponAnnuity fixed = new ConstantCouponAnnuity(FIXED_PAYMENTS, 1.0, COUPON_RATE, FUNDING_CURVE_NAME);
    VariableAnnuity floating = new VariableAnnuity(FLOAT_PAYMENTS, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACS, 1.0,
        FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    other = new FixedFloatSwap(fixed, floating);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
  }

  @Test
  public void testGetters() {
    assertArrayEquals(FIXED_PAYMENTS, SWAP.getFixedLeg().getPaymentTimes(), 0);
    assertArrayEquals(FLOAT_PAYMENTS, SWAP.getFloatingLeg().getPaymentTimes(), 0);
    assertArrayEquals(INDEX_FIXING, SWAP.getFloatingLeg().getIndexFixingTimes(), 0);
    assertArrayEquals(INDEX_MATURITY, SWAP.getFloatingLeg().getIndexMaturityTimes(), 0);
    assertEquals(FIXED_PAYMENTS.length, SWAP.getFixedLeg().getNumberOfPayments());
    assertEquals(FLOAT_PAYMENTS.length, SWAP.getFloatingLeg().getNumberOfPayments());

    assertArrayEquals(new double[] {1.5, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, SWAP.getFloatingLeg().getYearFractions(),
        0);
  }
  // @Test
  // public void testUnsortedInputs() {
  // double[] unsorted = new double[] {2, 6, 3, 1.5, 4, 5};
  // Swap swap = new Swap(unsorted, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  // assertEquals(swap, SWAP);
  // assertArrayEquals(swap.getFixedLeg().getPaymentTimes(), SWAP.getFixedLeg().getPaymentTimes(), 0);
  // unsorted = new double[] {12, 6, 7, 9, 10, 2, 4, 3, 1.5, 5, 8, 11};
  // swap = new Swap(FIXED_PAYMENTS, unsorted, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  // assertEquals(swap, SWAP);
  // assertArrayEquals(swap.getFloatingLeg().getPaymentTimes(), SWAP.getFloatingLeg().getPaymentTimes(), 0);
  // }

}

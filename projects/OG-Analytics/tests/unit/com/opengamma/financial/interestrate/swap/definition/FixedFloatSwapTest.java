/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;

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

  private static final FixedFloatSwap SWAP = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);

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
    FixedFloatSwap other = new FixedFloatSwap(Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length), Arrays.copyOf(FLOAT_PAYMENTS, FLOAT_PAYMENTS.length), COUPON_RATE, FUNDING_CURVE_NAME,
        LIBOR_CURVE_NAME);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
    other = new FixedFloatSwap(new double[] {1, 2, 3, 4, 5, 6}, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, new double[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, COUPON_RATE, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, 0.03, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, LIBOR_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new FixedFloatSwap(FIXED_PAYMENTS, FLOAT_PAYMENTS, COUPON_RATE, FUNDING_CURVE_NAME, FUNDING_CURVE_NAME);
    assertFalse(other.equals(SWAP));

    AnnuityCouponFixed fixed = new AnnuityCouponFixed(FIXED_PAYMENTS, 1.0, COUPON_RATE, FUNDING_CURVE_NAME);
    AnnuityCouponIbor floating = new AnnuityCouponIbor(FLOAT_PAYMENTS, INDEX_FIXING, INDEX_MATURITY, YEAR_FRACS, 1.0, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    other = new FixedFloatSwap(fixed, floating);
    assertEquals(other.getFixedLeg(), SWAP.getFixedLeg());
    assertEquals(other.getFloatingLeg().getNthPayment(0), SWAP.getFloatingLeg().getNthPayment(0));
    assertEquals(other.getFloatingLeg(), SWAP.getFloatingLeg());
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
  }

  @Test
  public void testGetters() {
    GenericAnnuity<CouponFixed> fixedLeg = SWAP.getFixedLeg();
    assertEquals(fixedLeg.getNumberOfPayments(), FIXED_PAYMENTS.length, 0);
    for (int i = 0; i < FIXED_PAYMENTS.length; i++) {
      assertEquals(fixedLeg.getNthPayment(i).getPaymentTime(), FIXED_PAYMENTS[i], 0);
      assertEquals(fixedLeg.getNthPayment(i).getFixedRate(), COUPON_RATE, 0);
    }

    GenericAnnuity<CouponIbor> floatLeg = SWAP.getFloatingLeg();
    assertEquals(floatLeg.getNumberOfPayments(), FLOAT_PAYMENTS.length, 0);
    for (int i = 0; i < FLOAT_PAYMENTS.length; i++) {
      assertEquals(floatLeg.getNthPayment(i).getPaymentTime(), FLOAT_PAYMENTS[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getFixingTime(), INDEX_FIXING[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getFixingPeriodEndTime(), INDEX_MATURITY[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getPaymentYearFraction(), YEAR_FRACS[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getFixingYearFraction(), YEAR_FRACS[i], 0);
      assertEquals(floatLeg.getNthPayment(i).getSpread(), 0.0, 0);
    }

  }

}

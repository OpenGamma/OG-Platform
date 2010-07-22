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

/**
 * 
 */
public class SwapTest {
  public static final String CURVE_NAME = "test";
  private static final double[] FIXED_PAYMENTS = new double[] {1.5, 2, 3, 4, 5, 6};
  private static final double[] FLOAT_PAYMENTS = new double[] {1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
  private static final double[] FORWARD_START_OFFSETS = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
  private static final double[] FORWARD_END_OFFSETS = new double[] {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2};
  private static final Swap SWAP = new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixedPayments() {
    new Swap(null, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFloatPayments() {
    new Swap(FIXED_PAYMENTS, null, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartOffsets() {
    new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, null, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEndOffsets() {
    new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, null, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyFixedPayments() {
    new Swap(new double[0], FLOAT_PAYMENTS, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyFloatPayments() {
    new Swap(FIXED_PAYMENTS, new double[0], FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyStartOffsets() {
    new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, new double[0], FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyEndOffsets() {
    new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, null, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongStartOffsets() {
    new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, new double[] {0.1, 0.1, 0.1}, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongEndOffsets() {
    new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, new double[] {0.1, 0.1}, CURVE_NAME, CURVE_NAME);
  }

  @Test
  public void testHashCodeAndEquals() {
    Swap other = new Swap(Arrays.copyOf(FIXED_PAYMENTS, FIXED_PAYMENTS.length), Arrays.copyOf(FLOAT_PAYMENTS, FLOAT_PAYMENTS.length), Arrays
        .copyOf(FORWARD_START_OFFSETS, FORWARD_START_OFFSETS.length), Arrays.copyOf(FORWARD_END_OFFSETS, FORWARD_END_OFFSETS.length), CURVE_NAME, CURVE_NAME);
    assertEquals(other, SWAP);
    assertEquals(other.hashCode(), SWAP.hashCode());
    other = new Swap(new double[] {1, 2, 3, 4, 5, 6, 7}, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new Swap(FIXED_PAYMENTS, new double[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, FORWARD_START_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, FORWARD_END_OFFSETS, FORWARD_END_OFFSETS, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(SWAP));
    other = new Swap(FIXED_PAYMENTS, FLOAT_PAYMENTS, FORWARD_START_OFFSETS, FORWARD_START_OFFSETS, CURVE_NAME, CURVE_NAME);
    assertFalse(other.equals(SWAP));
  }

  @Test
  public void testGetters() {
    assertArrayEquals(FIXED_PAYMENTS, SWAP.getFixedLeg().getPaymentTimes(), 0);
    assertArrayEquals(FLOAT_PAYMENTS, SWAP.getFloatingLeg().getPaymentTimes(), 0);
    assertArrayEquals(FORWARD_START_OFFSETS, SWAP.getFloatingLeg().getDeltaStart(), 0);
    assertArrayEquals(FORWARD_END_OFFSETS, SWAP.getFloatingLeg().getDeltaEnd(), 0);
    assertEquals(FIXED_PAYMENTS.length, SWAP.getFixedLeg().getNumberOfPayments());
    assertEquals(FLOAT_PAYMENTS.length, SWAP.getFloatingLeg().getNumberOfPayments());

    assertArrayEquals(new double[] {1.5, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, SWAP.getFloatingLeg().getYearFractions(), 0);
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

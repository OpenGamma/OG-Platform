/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * 
 */
public class GenericAnnuityTest {
  private static final FixedCouponPayment[] PAYMENTS;
  private static final List<FixedCouponPayment> LIST_PAYMENTS;
  private static final FixedPayment[] MIXED_PAYMENTS;
  private static final List<FixedPayment> LIST_MIXED_PAYMENTS;

  static {
    final int n = 5;
    final double tau = 0.25;
    final double coupon = 0.06;
    PAYMENTS = new FixedCouponPayment[n];
    LIST_PAYMENTS = new ArrayList<FixedCouponPayment>();
    MIXED_PAYMENTS = new FixedPayment[n];
    LIST_MIXED_PAYMENTS = new ArrayList<FixedPayment>();
    for (int i = 0; i < n; i++) {
      final FixedCouponPayment temp = new FixedCouponPayment((i + 1) * tau, tau, coupon, "fg");
      PAYMENTS[i] = temp;
      LIST_PAYMENTS.add(temp);
      if (i % 2 == 0) {
        final FixedPayment temp2 = new FixedPayment((i + 1) * tau, 23.2, "fg");
        MIXED_PAYMENTS[i] = temp2;
        LIST_MIXED_PAYMENTS.add(temp2);
      } else {
        MIXED_PAYMENTS[i] = temp;
        LIST_MIXED_PAYMENTS.add(temp);
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    new GenericAnnuity<FixedCouponPayment>(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    new GenericAnnuity<FixedCouponPayment>(new FixedCouponPayment[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayment() {
    new GenericAnnuity<FixedCouponPayment>(new FixedCouponPayment[] {null});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullList() {
    new GenericAnnuity<FixedCouponPayment>(null, FixedCouponPayment.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullClass() {
    new GenericAnnuity<FixedCouponPayment>(LIST_PAYMENTS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyList() {
    new GenericAnnuity<FixedCouponPayment>(new ArrayList<FixedCouponPayment>(), FixedCouponPayment.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentInList() {
    final List<FixedCouponPayment> list = new ArrayList<FixedCouponPayment>();
    list.add(null);
    new GenericAnnuity<FixedCouponPayment>(list, FixedCouponPayment.class);
  }

  @Test
  public void testArrayConstruction() {
    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(PAYMENTS);
    final GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(PAYMENTS);
    final GenericAnnuity<FixedCouponPayment> temp3 = new GenericAnnuity<FixedCouponPayment>(PAYMENTS);
    assertTrue(Arrays.equals(PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp2.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp3.getPayments()));
    assertTrue(temp1.equals(temp2));
  }

  @Test
  public void testListConstruction() {
    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(LIST_PAYMENTS, Payment.class);
    final GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(LIST_PAYMENTS, FixedPayment.class);
    final GenericAnnuity<FixedCouponPayment> temp3 = new GenericAnnuity<FixedCouponPayment>(LIST_PAYMENTS, FixedCouponPayment.class);
    assertTrue(Arrays.equals(PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp2.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp3.getPayments()));
  }

  @Test
  public void testMixedArrayConstruction() {
    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(MIXED_PAYMENTS);
    final GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(MIXED_PAYMENTS);
    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp2.getPayments()));
  }

  @Test
  public void testMixedListConstruction() {
    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(LIST_MIXED_PAYMENTS, Payment.class);
    final GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(LIST_MIXED_PAYMENTS, FixedPayment.class);
    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp2.getPayments()));
  }

  @Test
  public void test() {
    final GenericAnnuity<FixedCouponPayment> annuity = new GenericAnnuity<FixedCouponPayment>(PAYMENTS);
    GenericAnnuity<FixedCouponPayment> other = new GenericAnnuity<FixedCouponPayment>(PAYMENTS);
    assertEquals(annuity, other);
    assertEquals(annuity.hashCode(), other.hashCode());
    assertEquals(annuity.getNumberOfPayments(), PAYMENTS.length);
    assertArrayEquals(annuity.getPayments(), PAYMENTS);
    for (int i = 0; i < PAYMENTS.length; i++) {
      assertEquals(annuity.getNthPayment(i), PAYMENTS[i]);
    }
    other = new GenericAnnuity<FixedCouponPayment>(new FixedCouponPayment[] {PAYMENTS[0], PAYMENTS[1]});
    assertFalse(annuity.equals(other));
  }
}

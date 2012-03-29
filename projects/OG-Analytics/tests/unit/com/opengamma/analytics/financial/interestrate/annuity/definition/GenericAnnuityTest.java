/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class GenericAnnuityTest {
  private static final CouponFixed[] PAYMENTS;
  private static final List<CouponFixed> LIST_PAYMENTS;
  private static final Payment[] MIXED_PAYMENTS;
  private static final List<Payment> LIST_MIXED_PAYMENTS;
  private static final Currency CUR = Currency.USD;

  static {
    final int n = 5;
    final double tau = 0.25;
    final double coupon = 0.06;
    PAYMENTS = new CouponFixed[n];
    LIST_PAYMENTS = new ArrayList<CouponFixed>();
    MIXED_PAYMENTS = new Payment[n];
    LIST_MIXED_PAYMENTS = new ArrayList<Payment>();
    for (int i = 0; i < n; i++) {
      final CouponFixed temp = new CouponFixed(CUR, (i + 1) * tau, "fg", tau, coupon);
      PAYMENTS[i] = temp;
      LIST_PAYMENTS.add(temp);
      if (i % 2 == 0) {
        final PaymentFixed temp2 = new PaymentFixed(CUR, (i + 1) * tau, 23.2, "fg");
        MIXED_PAYMENTS[i] = temp2;
        LIST_MIXED_PAYMENTS.add(temp2);
      } else {
        MIXED_PAYMENTS[i] = temp;
        LIST_MIXED_PAYMENTS.add(temp);
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    new GenericAnnuity<CouponFixed>(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    new GenericAnnuity<CouponFixed>(new CouponFixed[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment() {
    new GenericAnnuity<CouponFixed>(new CouponFixed[] {null});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullList() {
    new GenericAnnuity<CouponFixed>(null, CouponFixed.class, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullClass() {
    new GenericAnnuity<CouponFixed>(LIST_PAYMENTS, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyList() {
    new GenericAnnuity<CouponFixed>(new ArrayList<CouponFixed>(), CouponFixed.class, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentInList() {
    final List<CouponFixed> list = new ArrayList<CouponFixed>();
    list.add(null);
    new GenericAnnuity<CouponFixed>(list, CouponFixed.class, true);
  }

  @Test
  public void testArrayConstruction() {
    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(PAYMENTS);
    //    final GenericAnnuity<PaymentFixed> temp2 = new GenericAnnuity<PaymentFixed>(PAYMENTS);
    final GenericAnnuity<CouponFixed> temp3 = new GenericAnnuity<CouponFixed>(PAYMENTS);
    assertTrue(Arrays.equals(PAYMENTS, temp1.getPayments()));
    //    assertTrue(Arrays.equals(PAYMENTS, temp2.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp3.getPayments()));
    //    assertTrue(temp1.equals(temp2));
  }

  @Test
  public void testListConstruction() {
    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(LIST_PAYMENTS, Payment.class, true);
    //    final GenericAnnuity<PaymentFixed> temp2 = new GenericAnnuity<PaymentFixed>(LIST_PAYMENTS, PaymentFixed.class);
    final GenericAnnuity<CouponFixed> temp3 = new GenericAnnuity<CouponFixed>(LIST_PAYMENTS, CouponFixed.class, true);
    assertTrue(Arrays.equals(PAYMENTS, temp1.getPayments()));
    //    assertTrue(Arrays.equals(PAYMENTS, temp2.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp3.getPayments()));
  }

  //  @Test
  //  public void testMixedArrayConstruction() {
  //    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(MIXED_PAYMENTS);
  //    final GenericAnnuity<PaymentFixed> temp2 = new GenericAnnuity<PaymentFixed>(MIXED_PAYMENTS);
  //    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp1.getPayments()));
  //    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp2.getPayments()));
  //  }

  //  @Test
  //  public void testMixedListConstruction() {
  //    final GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(LIST_MIXED_PAYMENTS, Payment.class);
  //    final GenericAnnuity<PaymentFixed> temp2 = new GenericAnnuity<PaymentFixed>(LIST_MIXED_PAYMENTS, PaymentFixed.class);
  //    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp1.getPayments()));
  //    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp2.getPayments()));
  //  }

  @Test
  public void test() {
    final GenericAnnuity<CouponFixed> annuity = new GenericAnnuity<CouponFixed>(PAYMENTS);
    GenericAnnuity<CouponFixed> other = new GenericAnnuity<CouponFixed>(PAYMENTS);
    assertEquals(annuity, other);
    assertEquals(annuity.hashCode(), other.hashCode());
    assertEquals(annuity.getNumberOfPayments(), PAYMENTS.length);
    assertArrayEquals(annuity.getPayments(), PAYMENTS);
    for (int i = 0; i < PAYMENTS.length; i++) {
      assertEquals(annuity.getNthPayment(i), PAYMENTS[i]);
    }
    other = new GenericAnnuity<CouponFixed>(new CouponFixed[] {PAYMENTS[0], PAYMENTS[1]});
    assertFalse(annuity.equals(other));
  }
}

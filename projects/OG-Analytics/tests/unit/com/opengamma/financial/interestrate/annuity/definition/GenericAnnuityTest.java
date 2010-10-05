/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

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
    int n = 5;
    double tau = 0.25;
    double coupon = 0.06;
    PAYMENTS = new FixedCouponPayment[n];
    LIST_PAYMENTS = new ArrayList<FixedCouponPayment>();
    MIXED_PAYMENTS = new FixedPayment[n];
    LIST_MIXED_PAYMENTS = new ArrayList<FixedPayment>();
    for (int i = 0; i < n; i++) {
      FixedCouponPayment temp = new FixedCouponPayment((i + 1) * tau, tau, coupon, "fg");
      PAYMENTS[i] = temp;
      LIST_PAYMENTS.add(temp);
      if (n % 2 == 0) {
        FixedPayment temp2 = new FixedPayment((i + 1) * tau, 23.2, "fg");
        MIXED_PAYMENTS[i] = temp2;
        LIST_MIXED_PAYMENTS.add(temp2);
      } else {
        MIXED_PAYMENTS[i] = temp;
        LIST_MIXED_PAYMENTS.add(temp);
      }
    }
  }

  @Test
  public void TestArrayConsturction() {
    GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(PAYMENTS);
    GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(PAYMENTS);
    GenericAnnuity<FixedCouponPayment> temp3 = new GenericAnnuity<FixedCouponPayment>(PAYMENTS);
    assertTrue(Arrays.equals(PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp2.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp3.getPayments()));
    assertTrue(temp1.equals(temp2));
  }

  @Test
  public void TestListConsturction() {
    GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(LIST_PAYMENTS, Payment.class);
    GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(LIST_PAYMENTS, FixedPayment.class);
    GenericAnnuity<FixedCouponPayment> temp3 = new GenericAnnuity<FixedCouponPayment>(LIST_PAYMENTS, FixedCouponPayment.class);
    assertTrue(Arrays.equals(PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp2.getPayments()));
    assertTrue(Arrays.equals(PAYMENTS, temp3.getPayments()));
  }

  @Test
  public void TestMixedArrayConsturction() {
    GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(MIXED_PAYMENTS);
    GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(MIXED_PAYMENTS);

    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp2.getPayments()));
  }

  @Test
  public void TestMixedListConsturction() {
    GenericAnnuity<Payment> temp1 = new GenericAnnuity<Payment>(LIST_MIXED_PAYMENTS, Payment.class);
    GenericAnnuity<FixedPayment> temp2 = new GenericAnnuity<FixedPayment>(LIST_MIXED_PAYMENTS, FixedPayment.class);

    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp1.getPayments()));
    assertTrue(Arrays.equals(MIXED_PAYMENTS, temp2.getPayments()));

  }

}

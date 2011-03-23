/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;

/**
 * 
 */
public class YieldSensitivityCalculatorTest {
  private static YieldSensitivityCalculator YSC = YieldSensitivityCalculator.getInstance();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity1() {
    YSC.calculateYield(null, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity2() {
    YSC.calculatePriceForYield(null, 0.05);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity3() {
    YSC.calculateNthOrderSensitivity(null, 1.0, 3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity4() {
    YSC.calculateNthOrderSensitivityFromYield(null, 0.04, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOrder() {
    YSC.calculateNthOrderSensitivity(new GenericAnnuity<CouponFixed>(new CouponFixed[] {new CouponFixed(2, "A", 2, 0.4)}), 1, -1);
  }

  @Test
  public void testSinglePaymentYield() {
    final int n = 10;
    final double pv = 0.875;
    final CouponFixed[] payments = new CouponFixed[n];
    //    final PaymentFixed[] payments = new PaymentFixed[n];
    final double tau = 0.5;
    for (int i = 0; i < n - 1; i++) {
      payments[i] = new CouponFixed((i + 1) * tau, "", tau, 0.0);
    }
    payments[n - 1] = new CouponFixed(n * tau, "", tau, 2.0);

    final GenericAnnuity<CouponFixed> annuity = new GenericAnnuity<CouponFixed>(payments);
    final double yield = YieldSensitivityCalculator.getInstance().calculateYield(annuity, pv);
    assertEquals(Math.log(2.0 * tau / pv) / 10.0 / tau, yield, 1e-8);
  }

  @Test
  public void testSinglePaymentSensitivity() {
    final int n = 10;
    final double pv = 0.78945;
    final CouponFixed[] payments = new CouponFixed[n];
    final double tau = 0.5;
    for (int i = 0; i < n - 1; i++) {
      payments[i] = new CouponFixed((i + 1) * tau, "", tau, 0.0);
    }
    payments[n - 1] = new CouponFixed(n * tau, "", tau, 2.0);

    final GenericAnnuity<CouponFixed> annuity = new GenericAnnuity<CouponFixed>(payments);

    for (int order = 1; order < 5; order++) {
      final double sense = YieldSensitivityCalculator.getInstance().calculateNthOrderSensitivity(annuity, pv, order);
      assertEquals(Math.pow(tau * n, order) * pv, sense, 1e-8);
    }
  }

  @Test
  public void testSinglePaymentSensitivityFromYield() {
    final int n = 10;
    final double yield = 0.06;
    final CouponFixed[] payments = new CouponFixed[n];
    final double tau = 0.5;
    for (int i = 0; i < n - 1; i++) {
      payments[i] = new CouponFixed((i + 1) * tau, "", tau, 0.0);
    }
    payments[n - 1] = new CouponFixed(n * tau, "", tau, 2.0);

    final GenericAnnuity<CouponFixed> annuity = new GenericAnnuity<CouponFixed>(payments);

    final double pv = YieldSensitivityCalculator.getInstance().calculatePriceForYield(annuity, yield);
    for (int order = 1; order < 5; order++) {
      final double sense = YieldSensitivityCalculator.getInstance().calculateNthOrderSensitivityFromYield(annuity, yield, order);
      assertEquals(Math.pow(tau * n, order) * pv, sense, 1e-8);
    }
  }

}

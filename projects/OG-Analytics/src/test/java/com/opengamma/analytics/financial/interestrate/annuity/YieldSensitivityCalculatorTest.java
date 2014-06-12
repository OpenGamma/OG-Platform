/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldSensitivityCalculatorTest {
  private static YieldSensitivityCalculator YSC = YieldSensitivityCalculator.getInstance();
  private static final Currency CUR = Currency.EUR;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity1() {
    YSC.calculateYield((AnnuityCouponFixed) null, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity2() {
    YSC.calculatePriceForYield((AnnuityCouponFixed) null, 0.05);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity3() {
    YSC.calculateNthOrderSensitivity((AnnuityCouponFixed) null, 1.0, 3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnuity4() {
    YSC.calculateNthOrderSensitivityFromYield((AnnuityCouponFixed) null, 0.04, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOrder() {
    YSC.calculateNthOrderSensitivity(new AnnuityCouponFixed(new CouponFixed[] {new CouponFixed(CUR, 2, 2, 0.4)}), 1, -1);
  }

  @Test
  public void testSinglePaymentYield() {
    final int n = 10;
    final double pv = 0.875;
    final CouponFixed[] payments = new CouponFixed[n];
    //    final PaymentFixed[] payments = new PaymentFixed[n];
    final double tau = 0.5;
    for (int i = 0; i < n - 1; i++) {
      payments[i] = new CouponFixed(CUR, (i + 1) * tau, tau, 0.0);
    }
    payments[n - 1] = new CouponFixed(CUR, n * tau, tau, 2.0);

    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(payments);
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
      payments[i] = new CouponFixed(CUR, (i + 1) * tau, tau, 0.0);
    }
    payments[n - 1] = new CouponFixed(CUR, n * tau, tau, 2.0);

    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(payments);

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
      payments[i] = new CouponFixed(CUR, (i + 1) * tau, tau, 0.0);
    }
    payments[n - 1] = new CouponFixed(CUR, n * tau, tau, 2.0);

    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(payments);

    final double pv = YieldSensitivityCalculator.getInstance().calculatePriceForYield(annuity, yield);
    for (int order = 1; order < 5; order++) {
      final double sense = YieldSensitivityCalculator.getInstance().calculateNthOrderSensitivityFromYield(annuity, yield, order);
      assertEquals(Math.pow(tau * n, order) * pv, sense, 1e-8);
    }
  }

}

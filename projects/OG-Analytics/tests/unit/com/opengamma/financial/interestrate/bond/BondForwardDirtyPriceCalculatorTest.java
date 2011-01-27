/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class BondForwardDirtyPriceCalculatorTest {
  private static final BondForwardDirtyPriceCalculator CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final double DIRTY_PRICE = 103;
  private static final double CLEAN_PRICE = 100;
  private static final double FUNDING_RATE = 0.03;
  private static final String CURVE_NAME = "A";
  private static final BondForward FORWARD1 = new BondForward(new Bond(new double[] {1, 2, 3, 4, 5, 6}, 0.05, CURVE_NAME), 0.5, DIRTY_PRICE - CLEAN_PRICE, 0, new FixedCouponPayment[0]);
  private static final BondForward FORWARD2 = new BondForward(new Bond(new double[] {1, 2, 3, 4, 5, 6}, 0.05, CURVE_NAME), 3.5, DIRTY_PRICE - CLEAN_PRICE, 0, new FixedCouponPayment[] {
      new FixedCouponPayment(2.5, 1, 0.05, CURVE_NAME), new FixedCouponPayment(1.5, 1, 0.05, CURVE_NAME), new FixedCouponPayment(0.5, 1, 0.05, CURVE_NAME)});
  private static final double EPS = 1e-10;

  @Test(expected = IllegalArgumentException.class)
  public void testNullForward() {
    CALCULATOR.calculate(null, DIRTY_PRICE, FUNDING_RATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDirtyPrice() {
    CALCULATOR.calculate(FORWARD1, -DIRTY_PRICE, FUNDING_RATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeFundingRate() {
    CALCULATOR.calculate(FORWARD1, DIRTY_PRICE, -FUNDING_RATE);
  }

  @Test
  public void test() {
    final double yield = BondYieldCalculator.getInstance().calculate(FORWARD1.getBond(), DIRTY_PRICE);
    final YieldCurveBundle curves = new YieldCurveBundle(new String[] {CURVE_NAME}, new YieldCurve[] {new YieldCurve(ConstantDoublesCurve.from(yield))});
    double valueWithoutExpiredCoupons = DIRTY_PRICE * (1 + FUNDING_RATE * FORWARD1.getForwardTime());
    assertEquals(CALCULATOR.calculate(FORWARD1, CLEAN_PRICE, FUNDING_RATE), valueWithoutExpiredCoupons, EPS);
    assertEquals(CALCULATOR.calculate(FORWARD1, curves, FUNDING_RATE), valueWithoutExpiredCoupons, EPS);
    valueWithoutExpiredCoupons = DIRTY_PRICE * (1 + FUNDING_RATE * FORWARD2.getForwardTime());
    final double expiredCouponValue = 0.05 * (3 + FUNDING_RATE * (0.5 + 1.5 + 2.5));
    assertEquals(CALCULATOR.calculate(FORWARD2, CLEAN_PRICE, FUNDING_RATE), valueWithoutExpiredCoupons - expiredCouponValue, EPS);
    assertEquals(CALCULATOR.calculate(FORWARD2, curves, FUNDING_RATE), valueWithoutExpiredCoupons - expiredCouponValue, EPS);
  }
}

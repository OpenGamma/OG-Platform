/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class BondDirtyPriceCalculatorTest {
  private static final BondDirtyPriceCalculator CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final YieldCurveBundle CURVES = new YieldCurveBundle(new String[] {"A", "B"}, new YieldCurve[] {new YieldCurve(ConstantDoublesCurve.from(0.1)),
      new YieldCurve(ConstantDoublesCurve.from(0.11))});
  private static final double ACCRUED_INTEREST = 0.03;
  private static final double CLEAN_PRICE = 1.03;
  private static final Bond BOND = new Bond(new double[] {1, 2, 3}, new double[] {0.02, 0.02, 0.02}, new double[] {1, 1, 1}, ACCRUED_INTEREST, "A");

  @Test(expected = IllegalArgumentException.class)
  public void testNullBond1() {
    CALCULATOR.calculate(null, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBond2() {
    CALCULATOR.calculate(null, CURVES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurves() {
    CALCULATOR.calculate(BOND, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.calculate(BOND, CLEAN_PRICE), CLEAN_PRICE + ACCRUED_INTEREST, 0);
    final double pv = 0.02 * (Math.exp(-0.1) + Math.exp(-0.2)) + 1.02 * Math.exp(-0.3);
    assertEquals(pv, CALCULATOR.calculate(BOND, CURVES), 1e-12);
    final double dirtyPrice = 101.1;
    assertEquals(CALCULATOR.calculate(BOND, BondCleanPriceCalculator.getInstance().calculate(BOND, dirtyPrice)), dirtyPrice, 1e-15);
  }
}

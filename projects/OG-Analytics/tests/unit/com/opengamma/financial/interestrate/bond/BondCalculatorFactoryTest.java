/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;

/**
 * 
 */
public class BondCalculatorFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadName1() {
    BondCalculatorFactory.getBondCalculator("A");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadName2() {
    BondCalculatorFactory.getBondForwardCalculator("F");
  }

  @Test
  public void testUnknownCalculator() {
    final BondCalculator bc = new BondCalculator() {

      @Override
      public Double calculate(final Bond bond, final double price) {
        return null;
      }

      @Override
      public Double calculate(final Bond bond, final YieldCurveBundle curves) {
        return null;
      }
    };
    assertNull(BondCalculatorFactory.getBondCalculatorName(bc));
    final BondForwardCalculator bfc = new BondForwardCalculator() {

      @Override
      public Double calculate(final BondForward bondForward, final double price, final double fundingRate) {
        return null;
      }

      @Override
      public Double calculate(final BondForward bondForward, final YieldCurveBundle curves, final double fundingRate) {
        return null;
      }
    };
    assertNull(BondCalculatorFactory.getBondForwardCalculatorName(bfc));
  }

  @Test
  public void testNullCalculators() {
    assertNull(BondCalculatorFactory.getBondCalculatorName(null));
    assertNull(BondCalculatorFactory.getBondForwardCalculatorName(null));
  }

  @Test
  public void test() {
    assertEquals(BondCalculatorFactory.BOND_CLEAN_PRICE, BondCalculatorFactory.getBondCalculatorName(BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_CLEAN_PRICE)));
    assertEquals(BondCalculatorFactory.BOND_DIRTY_PRICE, BondCalculatorFactory.getBondCalculatorName(BondCalculatorFactory.getBondCalculator(BondCalculatorFactory.BOND_DIRTY_PRICE)));
    assertEquals(BondCalculatorFactory.BOND_FORWARD_DIRTY_PRICE,
        BondCalculatorFactory.getBondForwardCalculatorName(BondCalculatorFactory.getBondForwardCalculator(BondCalculatorFactory.BOND_FORWARD_DIRTY_PRICE)));
  }
}

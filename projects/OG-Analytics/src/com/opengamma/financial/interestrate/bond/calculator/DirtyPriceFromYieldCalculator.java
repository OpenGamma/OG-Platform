/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * 
 */
public final class DirtyPriceFromYieldCalculator extends AbstractInstrumentDerivativeVisitor<Double, Double> {
  private static final DirtyPriceFromYieldCalculator INSTANCE = new DirtyPriceFromYieldCalculator();

  public static DirtyPriceFromYieldCalculator getInstance() {
    return INSTANCE;
  }

  private DirtyPriceFromYieldCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    Validate.notNull(bond, "bond");
    Validate.notNull(yield, "yield");
    return BondSecurityDiscountingMethod.getInstance().dirtyPriceFromYield(bond, yield);
  }
}

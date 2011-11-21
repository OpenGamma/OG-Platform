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
 * Calculate dirty price for bonds.
 */
public final class MacaulayDurationFromYieldCalculator extends AbstractInstrumentDerivativeVisitor<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final MacaulayDurationFromYieldCalculator s_instance = new MacaulayDurationFromYieldCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static MacaulayDurationFromYieldCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private MacaulayDurationFromYieldCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    Validate.notNull(bond);
    Validate.notNull(yield);
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.macaulayDurationFromYield(bond, yield);
  }
}

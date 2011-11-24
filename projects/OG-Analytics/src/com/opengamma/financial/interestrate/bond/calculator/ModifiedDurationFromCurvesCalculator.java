/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * Calculate modified duration for bonds.
 */
public final class ModifiedDurationFromCurvesCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The calculator instance.
   */
  private static final ModifiedDurationFromCurvesCalculator s_instance = new ModifiedDurationFromCurvesCalculator();
  /**
   * The fixed coupon bond method.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND = BondSecurityDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ModifiedDurationFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private ModifiedDurationFromCurvesCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    return METHOD_BOND.modifiedDurationFromCurves(bond, curves);
  }

}

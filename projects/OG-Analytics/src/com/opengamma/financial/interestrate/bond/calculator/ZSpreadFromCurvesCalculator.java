/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * 
 */
public final class ZSpreadFromCurvesCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Double> {
  private static final ZSpreadFromCurvesCalculator s_instance = new ZSpreadFromCurvesCalculator();
  private static final BondSecurityDiscountingMethod METHOD = BondSecurityDiscountingMethod.getInstance();
  
  public static ZSpreadFromCurvesCalculator getInstance() {
    return s_instance;
  }
  
  private ZSpreadFromCurvesCalculator() {
  }
  
  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    double pv = METHOD.presentValue(bond, curves);
    return METHOD.zSpreadFromCurvesAndPV(bond, curves, pv);
  }
}

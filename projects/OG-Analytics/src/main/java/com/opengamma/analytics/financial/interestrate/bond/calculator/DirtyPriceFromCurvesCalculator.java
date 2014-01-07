/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * Calculate dirty price for bonds.
 * @deprecated Use {@link com.opengamma.analytics.financial.provider.calculator.issuer.DirtyPriceFromCurvesCalculator}
 */
@Deprecated
public final class DirtyPriceFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The calculator instance.
   */
  private static final DirtyPriceFromCurvesCalculator s_instance = new DirtyPriceFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static DirtyPriceFromCurvesCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private DirtyPriceFromCurvesCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(bond);
    final BondSecurityDiscountingMethod method = BondSecurityDiscountingMethod.getInstance();
    return method.dirtyPriceFromCurves(bond, curves);
  }

}

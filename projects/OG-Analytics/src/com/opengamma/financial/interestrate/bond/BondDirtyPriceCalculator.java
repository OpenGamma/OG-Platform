/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.FixedIncomeCalculatorFactory;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class BondDirtyPriceCalculator extends BondCalculator {
  @SuppressWarnings("unchecked")
  private static final InterestRateDerivativeVisitor<YieldCurveBundle, Double> PV_CALCULATOR = (InterestRateDerivativeVisitor<YieldCurveBundle, Double>) FixedIncomeCalculatorFactory
      .getCalculator(FixedIncomeCalculatorFactory.PRESENT_VALUE);

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    return PV_CALCULATOR.visitBond(bond, curves);
  }

  @Override
  public Double calculate(final Bond bond, final double cleanPrice) {
    return cleanPrice + bond.getAccruedInterest();
  }

}

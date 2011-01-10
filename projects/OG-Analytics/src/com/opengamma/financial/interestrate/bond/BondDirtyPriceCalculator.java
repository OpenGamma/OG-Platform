/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.FixedIncomeCalculatorFactory;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public final class BondDirtyPriceCalculator extends BondCalculator {
  @SuppressWarnings("unchecked")
  private static final InterestRateDerivativeVisitor<YieldCurveBundle, Double> PV_CALCULATOR = (InterestRateDerivativeVisitor<YieldCurveBundle, Double>) FixedIncomeCalculatorFactory
      .getCalculator(FixedIncomeCalculatorFactory.PRESENT_VALUE);
  private static final BondDirtyPriceCalculator INSTANCE = new BondDirtyPriceCalculator();

  private BondDirtyPriceCalculator() {
  }

  public static BondDirtyPriceCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    return PV_CALCULATOR.visitBond(bond, curves);
  }

  @Override
  public Double calculate(final Bond bond, final double cleanPrice) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(cleanPrice > 0, "clean price is positive");
    return cleanPrice + bond.getAccruedInterest();
  }

}

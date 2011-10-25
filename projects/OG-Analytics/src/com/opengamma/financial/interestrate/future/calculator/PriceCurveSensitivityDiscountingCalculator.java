/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.method.BondFutureDiscountingMethod;

/**
 * Calculate security prices curve sensitivity for futures (bond and interest rate).
 */
public final class PriceCurveSensitivityDiscountingCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, PresentValueSensitivity> {

  /**
   * The calculator instance.
   */
  private static final PriceCurveSensitivityDiscountingCalculator s_instance = new PriceCurveSensitivityDiscountingCalculator();
  /**
   * The method to compute bond future prices.
   */
  private static final BondFutureDiscountingMethod METHOD_BOND_FUTURE = BondFutureDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static PriceCurveSensitivityDiscountingCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PriceCurveSensitivityDiscountingCalculator() {
  }

  @Override
  public PresentValueSensitivity visitBondFuture(final BondFuture future, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(future);
    return METHOD_BOND_FUTURE.priceCurveSensitivity(future, curves);
  }

}

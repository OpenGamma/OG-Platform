/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.method.BondFutureSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;

/**
 * Calculate security prices for futures (bond and interest rate).
 */
public final class PriceFromCurvesDiscountingCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The calculator instance.
   */
  private static final PriceFromCurvesDiscountingCalculator s_instance = new PriceFromCurvesDiscountingCalculator();
  /**
   * The method to compute bond future prices.
   */
  private static final BondFutureSecurityDiscountingMethod METHOD_BOND_FUTURE = BondFutureSecurityDiscountingMethod.getInstance();
  /**
   * The method to compute interest rate future prices.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_RATE_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static PriceFromCurvesDiscountingCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PriceFromCurvesDiscountingCalculator() {
  }

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(future);
    return METHOD_RATE_FUTURE.priceFromCurves(future, curves);
  }

  @Override
  public Double visitBondFutureSecurity(final BondFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(future);
    return METHOD_BOND_FUTURE.priceFromCurves(future, curves);
  }

}

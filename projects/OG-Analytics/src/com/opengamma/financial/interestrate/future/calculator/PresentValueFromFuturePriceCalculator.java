/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.calculator;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.method.BondFutureDiscountingMethod;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;

import org.apache.commons.lang.Validate;

/**
 * Calculate present value for futures from the quoted price.
 */
public final class PresentValueFromFuturePriceCalculator extends AbstractInterestRateDerivativeVisitor<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final PresentValueFromFuturePriceCalculator s_instance = new PresentValueFromFuturePriceCalculator();
  /**
   * The method to compute bond future prices.
   */
  private static final BondFutureDiscountingMethod METHOD_BOND_FUTURE = BondFutureDiscountingMethod.getInstance();
  /**
   * The method to compute interest rate future prices.
   */
  private static final InterestRateFutureDiscountingMethod METHOD_RATE_FUTURE = InterestRateFutureDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static PresentValueFromFuturePriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueFromFuturePriceCalculator() {
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final Double futurePrice) {
    Validate.notNull(future);
    return METHOD_RATE_FUTURE.presentValueFromPrice(future, futurePrice);
  }

  @Override
  public Double visitBondFuture(final BondFuture future, final Double futurePrice) {
    Validate.notNull(future);
    return METHOD_BOND_FUTURE.presentValueFromPrice(future, futurePrice);
  }

}

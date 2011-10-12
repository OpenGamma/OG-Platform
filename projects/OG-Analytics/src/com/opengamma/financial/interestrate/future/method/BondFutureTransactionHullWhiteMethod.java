/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value and its sensitivities for an bond future with discounting (using the cheapest-to-deliver). 
 * The delivery option is not taken into account.
 */
public final class BondFutureTransactionHullWhiteMethod extends BondFutureTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureTransactionHullWhiteMethod INSTANCE = new BondFutureTransactionHullWhiteMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFutureTransactionHullWhiteMethod() {
  }

  /**
   * The bond future security method.
   */
  private static final BondFutureSecurityHullWhiteMethod METHOD_SECURITY = BondFutureSecurityHullWhiteMethod.getInstance();

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated to the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFutureTransaction future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    final double futurePrice = METHOD_SECURITY.price(future.getUnderlyingFuture(), curves);
    final double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(future.getUnderlyingFuture().getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BondFutureTransaction, "Bond future transaction");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((BondFutureTransaction) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated. 
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final BondFutureTransaction future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateCurveSensitivity priceSensitivity = METHOD_SECURITY.priceCurveSensitivity(future.getUnderlyingFuture(), curves);
    final InterestRateCurveSensitivity transactionSensitivity = priceSensitivity.multiply(future.getQuantity() * future.getUnderlyingFuture().getNotional());
    return transactionSensitivity;
  }

}

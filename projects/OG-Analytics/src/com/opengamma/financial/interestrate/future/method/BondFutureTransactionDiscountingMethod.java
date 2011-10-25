/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.util.money.CurrencyAmount;

import org.apache.commons.lang.Validate;

/**
 * Method to compute the present value and its sensitivities for an bond future with discounting (using the cheapest-to-deliver). 
 * The delivery option is not taken into account.
 */
public final class BondFutureTransactionDiscountingMethod extends BondFutureMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFutureTransactionDiscountingMethod INSTANCE = new BondFutureTransactionDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFutureTransactionDiscountingMethod() {
  }

  /**
   * The bond future security method.
   */
  private static final BondFutureDiscountingMethod METHOD_SECURITY = BondFutureDiscountingMethod.getInstance();

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param txn The transaction on the future.
   * @param curves The yield curves. Should contain the credit and repo curves associated to the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFutureTransaction txn, final YieldCurveBundle curves) {
    Validate.notNull(txn, "BondFutureTransaction");
    final BondFuture future = txn.getUnderlyingFuture();
    final double futurePrice = METHOD_SECURITY.price(future, curves);
    final double pv = METHOD_SECURITY.presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BondFutureTransaction, "Bond future transaction");
    return presentValue((BondFutureTransaction) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated. 
   * @return The present value rate sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final BondFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    final PresentValueSensitivity priceSensitivity = METHOD_SECURITY.priceCurveSensitivity(future.getUnderlyingFuture(), curves);
    final PresentValueSensitivity transactionSensitivity = priceSensitivity.multiply(future.getQuantity() * future.getUnderlyingFuture().getNotional());
    return transactionSensitivity;
  }

}

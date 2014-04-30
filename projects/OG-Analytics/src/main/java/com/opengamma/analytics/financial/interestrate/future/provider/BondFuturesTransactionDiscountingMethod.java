/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the bond futures transaction results with the price computed as the cheapest forward.
 */
public final class BondFuturesTransactionDiscountingMethod extends FuturesTransactionIssuerMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesTransactionDiscountingMethod INSTANCE = new BondFuturesTransactionDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesTransactionDiscountingMethod() {
  }

  /**
   * The method to compute bond security figures.
   */
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURES_SEC = BondFuturesSecurityDiscountingMethod.getInstance();

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   * @param futures The future.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param netBasis The net basis associated to the future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromNetBasis(final BondFuturesTransaction futures, final IssuerProviderInterface issuerMulticurves, final double netBasis) {
    return presentValueFromPrice(futures, METHOD_FUTURES_SEC.priceFromNetBasis(futures.getUnderlyingSecurity(), issuerMulticurves, netBasis));
  }

}

/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface to generic futures security pricing method.
 */
public final class FuturesTransactionIssuerMethod extends FuturesTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FuturesTransactionIssuerMethod INSTANCE = new FuturesTransactionIssuerMethod();

  /**
   * Constructor.
   */
  private FuturesTransactionIssuerMethod() {
    super(FuturesSecurityIssuerMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FuturesTransactionIssuerMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Gets the securityMethod.
   * @return the securityMethod
   */
  @Override
  public FuturesSecurityIssuerMethod getSecurityMethod() {
    return (FuturesSecurityIssuerMethod) super.getSecurityMethod();
  }

  /**
   * Compute the present value of a future transaction from a curve provider.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FuturesTransaction<?> futures, final ParameterIssuerProviderInterface multicurve) {
    double price = getSecurityMethod().price(futures.getUnderlyingFuture(), multicurve);
    return presentValueFromPrice(futures, price);
  }

}

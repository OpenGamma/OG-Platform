/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface to generic futures security pricing method.
 */
public abstract class FuturesTransactionMethod {

  /**
   * The method used to price the underlying futures security.
   */
  private final FuturesSecurityMethod _securityMethod;

  /**
   * Constructor.
   * @param securityMethod The method used to price the underlying futures security.
   */
  public FuturesTransactionMethod(FuturesSecurityMethod securityMethod) {
    super();
    _securityMethod = securityMethod;
  }

  /**
   * Gets the securityMethod.
   * @return the securityMethod
   */
  public FuturesSecurityMethod getSecurityMethod() {
    return _securityMethod;
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param futures The futures.
   * @param quotedPrice The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final FuturesTransaction<?> futures, final double quotedPrice) {
    double priceIndex = _securityMethod.marginIndex(futures.getUnderlyingSecurity(), quotedPrice);
    double referenceIndex = _securityMethod.marginIndex(futures.getUnderlyingSecurity(), futures.getReferencePrice());
    double pv = (priceIndex - referenceIndex) * futures.getQuantity();
    return MultipleCurrencyAmount.of(futures.getUnderlyingSecurity().getCurrency(), pv);
  }

}

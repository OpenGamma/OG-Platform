/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
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
   * Compute the present value of a future transaction from a quoted price.
   * @param futures The futures.
   * @param quotedPrice The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final FuturesTransaction<?> futures, final double quotedPrice) {
    double priceIndex = _securityMethod.marginIndex(futures.getUnderlyingFuture(), quotedPrice);
    double referenceIndex = _securityMethod.marginIndex(futures.getUnderlyingFuture(), futures.getReferencePrice());
    double pv = (priceIndex - referenceIndex) * futures.getQuantity();
    return MultipleCurrencyAmount.of(futures.getUnderlyingFuture().getCurrency(), pv);
  }

  /**
   * Compute the present value of a future transaction from a curve provider.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FuturesTransaction<?> futures, final ParameterProviderInterface multicurve) {
    double price = _securityMethod.price(futures.getUnderlyingFuture(), multicurve);
    return presentValueFromPrice(futures, price);
  }

}

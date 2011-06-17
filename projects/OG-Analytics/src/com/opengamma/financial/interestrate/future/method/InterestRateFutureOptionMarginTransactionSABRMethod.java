/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date, 
 * i.e. 0 for normal options and x for x-year mid-curve options.
 */
public class InterestRateFutureOptionMarginTransactionSABRMethod extends InterestRateFutureOptionMarginTransactionMethod {

  /**
   * The method used to compute the underlying security price.
   */
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SECURITY = new InterestRateFutureOptionMarginSecuritySABRMethod();

  /**
   * Computes the present value of a transaction from the future price and SABR data.
   * @param transaction The future option transaction.
   * @param sabrData The SABR data bundle. 
   * @param priceFuture The price of the underlying future.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionMarginTransaction transaction, final SABRInterestRateDataBundle sabrData, final double priceFuture) {
    double priceSecurity = METHOD_SECURITY.optionPriceFromFuturePrice(transaction.getUnderlyingOption(), sabrData, priceFuture);
    CurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value of a transaction from yield curves and SABR data. The future price is computed as ???
   * @param transaction The future option transaction.
   * @param sabrData The SABR data bundle. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InterestRateFutureOptionMarginTransaction transaction, final SABRInterestRateDataBundle sabrData) {
    double priceSecurity = METHOD_SECURITY.optionPrice(transaction.getUnderlyingOption(), sabrData);
    CurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureOptionMarginTransaction, "Interest rate future option transaction");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "Interest rate future option transaction");
    return presentValue((InterestRateFutureOptionMarginTransaction) instrument, (SABRInterestRateDataBundle) curves);
  }

}

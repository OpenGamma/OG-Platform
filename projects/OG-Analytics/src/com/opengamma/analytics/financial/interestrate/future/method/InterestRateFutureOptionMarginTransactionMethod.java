/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Methods for the pricing of interest rate futures option with premium generic to all models.
 */
public abstract class InterestRateFutureOptionMarginTransactionMethod implements PricingMethod {

  /**
   * The method used to price the underlying security.
   */
  private final InterestRateFutureOptionMarginSecurityMethod _securityMethod;

  /**
   * Constructor.
   * @param securityMethod The method to price the underlying security.
   */
  public InterestRateFutureOptionMarginTransactionMethod(InterestRateFutureOptionMarginSecurityMethod securityMethod) {
    _securityMethod = securityMethod;
  }

  /**
   * Gets the method to price the underlying security.
   * @return The method.
   */
  public InterestRateFutureOptionMarginSecurityMethod getSecurityMethod() {
    return _securityMethod;
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param option The future option.
   * @param price The quoted price.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final InterestRateFutureOptionMarginTransaction option, final double price) {
    double pv = (price - option.getReferencePrice()) * option.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor() * option.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * option.getQuantity();
    return CurrencyAmount.of(option.getUnderlyingOption().getCurrency(), pv);
  }

  /**
   * Computes the present value of a transaction from the future price and curve/volatility data.
   * @param transaction The future option transaction.
   * @param curves The yield curve bundle.
   * @param priceFuture The price of the underlying future.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves, final double priceFuture) {
    double priceSecurity = _securityMethod.optionPriceFromFuturePrice(transaction.getUnderlyingOption(), curves, priceFuture);
    CurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof InterestRateFutureOptionMarginTransaction, "The instrument should be a InterestRateFutureOptionMarginTransaction");
    final InterestRateFutureOptionMarginTransaction transaction = (InterestRateFutureOptionMarginTransaction) instrument;
    double priceSecurity = _securityMethod.optionPrice(transaction.getUnderlyingOption(), curves);
    CurrencyAmount pvTransaction = presentValueFromPrice(transaction, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param curves The yield curve bundle.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    InterestRateCurveSensitivity securitySensitivity = _securityMethod.priceCurveSensitivity(transaction.getUnderlyingOption(), curves);
    return securitySensitivity.multiply(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
  }

}

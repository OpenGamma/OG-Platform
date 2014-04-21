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
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class InterestRateFutureOptionMarginTransactionMethod implements PricingMethod {

  /**
   * The method used to price the underlying security.
   */
  private final InterestRateFutureOptionMarginSecurityMethod _securityMethod;

  /**
   * Constructor.
   * @param securityMethod The method to price the underlying security.
   */
  public InterestRateFutureOptionMarginTransactionMethod(final InterestRateFutureOptionMarginSecurityMethod securityMethod) {
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
    final double pv = (price - option.getReferencePrice()) * option.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor() *
        option.getUnderlyingSecurity().getUnderlyingFuture().getNotional() * option.getQuantity();
    return CurrencyAmount.of(option.getUnderlyingSecurity().getCurrency(), pv);
  }

  /**
   * Computes the present value of a transaction from the future price and curve/volatility data.
   * @param transaction The future option transaction.
   * @param curves The yield curve bundle.
   * @param priceFuture The price of the underlying future.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves, final double priceFuture) {
    final double priceSecurity = _securityMethod.optionPriceFromFuturePrice(transaction.getUnderlyingSecurity(), curves, priceFuture);
    final CurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof InterestRateFutureOptionMarginTransaction, "The instrument should be a InterestRateFutureOptionMarginTransaction");
    final InterestRateFutureOptionMarginTransaction transaction = (InterestRateFutureOptionMarginTransaction) instrument;
    final double priceSecurity = _securityMethod.optionPrice(transaction.getUnderlyingSecurity(), curves);
    final CurrencyAmount pvTransaction = presentValueFromPrice(transaction, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param curves The yield curve bundle.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveBundle curves) {
    final InterestRateCurveSensitivity securitySensitivity = _securityMethod.priceCurveSensitivity(transaction.getUnderlyingSecurity(), curves);
    return securitySensitivity.multipliedBy(transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor());
  }

}

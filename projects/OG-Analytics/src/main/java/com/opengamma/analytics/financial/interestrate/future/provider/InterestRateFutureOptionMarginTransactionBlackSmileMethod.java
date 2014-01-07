/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderInterface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with daily margining. The pricing is done with a Black approach on the future rate (1.0-price).
 * The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public final class InterestRateFutureOptionMarginTransactionBlackSmileMethod extends InterestRateFutureOptionMarginTransactionGenericMethod<BlackSTIRFuturesSmileProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSmileMethod INSTANCE = new InterestRateFutureOptionMarginTransactionBlackSmileMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionBlackSmileMethod() {
    super(InterestRateFutureOptionMarginSecurityBlackSmileMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   * @return The method.
   */
  @Override
  public InterestRateFutureOptionMarginSecurityBlackSmileMethod getSecurityMethod() {
    return (InterestRateFutureOptionMarginSecurityBlackSmileMethod) super.getSecurityMethod();
  }

  /**
   * Computes the present value of a transaction from the future price and curve/volatility data.
   * @param transaction The future option transaction.
   * @param blackData The Black volatility and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionMarginTransaction transaction, final BlackSTIRFuturesSmileProviderInterface blackData,
      final double priceFuture) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    final double priceSecurity = getSecurityMethod().priceFromFuturePrice(transaction.getUnderlyingOption(), blackData, priceFuture);
    final MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The Black volatility and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public SurfaceValue presentValueBlackSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final BlackSTIRFuturesSmileProviderInterface blackData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    SurfaceValue securitySensitivity = getSecurityMethod().priceBlackSensitivity(transaction.getUnderlyingOption(), blackData);
    securitySensitivity = SurfaceValue.multiplyBy(securitySensitivity, transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

  /**
   * Computes the present value gamma of a transaction.
   * This is with respect to futures rate
   * @param transaction The future option transaction.
   * @param blackData The Black volatility and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public double presentValueGamma(final InterestRateFutureOptionMarginTransaction transaction, final BlackSTIRFuturesSmileProviderInterface blackData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    final double securityGamma = getSecurityMethod().priceGamma(transaction.getUnderlyingOption(), blackData);
    final double txnGamma = securityGamma * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnGamma;
  }

  /**
   * Computes the present value delta of a transaction.
   * This is with respect to futures price
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double presentValueDelta(final InterestRateFutureOptionMarginTransaction transaction, final BlackSTIRFuturesSmileProviderInterface blackData) {
    final double securityDelta = getSecurityMethod().priceDelta(transaction.getUnderlyingOption(), blackData);
    final double txnDelta = securityDelta
        * transaction.getQuantity()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnDelta;
  }

  /**
   * Computes the present value volatility sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double presentValueVega(final InterestRateFutureOptionMarginTransaction transaction, final BlackSTIRFuturesSmileProviderInterface blackData) {
    final double securitySensitivity = getSecurityMethod().priceVega(transaction.getUnderlyingOption(), blackData);
    final double txnSensitivity = securitySensitivity
        * transaction.getQuantity()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnSensitivity;
  }

}

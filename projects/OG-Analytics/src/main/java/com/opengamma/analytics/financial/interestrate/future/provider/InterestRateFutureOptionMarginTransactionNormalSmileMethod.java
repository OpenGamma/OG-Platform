/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with daily margining. The pricing is done with a Normal approach on the future price.
 * The normal parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public final class InterestRateFutureOptionMarginTransactionNormalSmileMethod extends
    InterestRateFutureOptionMarginTransactionGenericMethod<NormalSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionNormalSmileMethod INSTANCE = new InterestRateFutureOptionMarginTransactionNormalSmileMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionNormalSmileMethod() {
    super(InterestRateFutureOptionMarginSecurityNormalSmileMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionNormalSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   * @return The method.
   */
  @Override
  public InterestRateFutureOptionMarginSecurityNormalSmileMethod getSecurityMethod() {
    return (InterestRateFutureOptionMarginSecurityNormalSmileMethod) super.getSecurityMethod();
  }

  /**
   * Computes the present value of a transaction from the future price and curve/volatility data.
   * @param transaction The future option transaction.
   * @param normalData The Black volatility and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromFuturePrice(
      final InterestRateFutureOptionMarginTransaction transaction, final NormalSTIRFuturesProviderInterface normalData,
      final double priceFuture) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(normalData, "Normal / multi-curves provider");
    double priceSecurity = getSecurityMethod().priceFromFuturePrice(transaction.getUnderlyingSecurity(), normalData, priceFuture);
    MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param normalData The Black volatility and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public SurfaceValue presentValueNormalSensitivity(final InterestRateFutureOptionMarginTransaction transaction,
      final NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(normalData, "Normal / multi-curves provider");
    SurfaceValue securitySensitivity = getSecurityMethod().priceNormalSensitivity(transaction.getUnderlyingSecurity(), normalData);
    securitySensitivity = SurfaceValue.multiplyBy(securitySensitivity, transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

  /**
   * Computes the present value gamma of a transaction.
   * This is with respect to futures price
   * @param transaction The future option transaction.
   * @param normalData The normal volatility and multi-curves provider.
   * @return The present value gamma.
   */
  public double presentValueGamma(InterestRateFutureOptionMarginTransaction transaction,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(normalData, "normal volatility / multi-curves provider");
    double securityGamma = getSecurityMethod().priceGamma(transaction.getUnderlyingSecurity(), normalData);
    double presentValueGamma = securityGamma * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return presentValueGamma;
  }

  /**
   * Computes the present value delta of a transaction.
   * This is with respect to futures price
   * @param transaction The future option transaction.
   * @param normalData The curve and normal volatility data.
   * @return The present value delta.
   */
  public double presentValueDelta(InterestRateFutureOptionMarginTransaction transaction,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(normalData, "normal volatility / multi-curves provider");
    double securityDelta = getSecurityMethod().priceDelta(transaction.getUnderlyingSecurity(), normalData);
    double presentValueDelta = securityDelta * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return presentValueDelta;
  }

  /**
   * Computes the present value volatility sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param normalData The curve and normal volatility data.
   * @return The present value vega.
   */
  public double presentValueVega(InterestRateFutureOptionMarginTransaction transaction,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(normalData, "normal volatility / multi-curves provider");
    double securitySensitivity = getSecurityMethod().priceVega(transaction.getUnderlyingSecurity(), normalData);
    double presentValueVega = securitySensitivity * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return presentValueVega;
  }

  /**
   * Computes the present value theta of a transaction.
   * @param transaction the future option transaction.
   * @param normalData the curve and normal volatility data.
   * @return the present value theta.
   */
  public double presentValueTheta(InterestRateFutureOptionMarginTransaction transaction,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(normalData, "normal volatility / multi-curves provider");
    double securitySensitivity = getSecurityMethod().priceTheta(transaction.getUnderlyingSecurity(), normalData);
    double presentValueTheta = securitySensitivity * transaction.getQuantity()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor();
    return presentValueTheta;
  }
}

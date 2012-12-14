/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.util.surface.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a Black approach on the future rate (1.0-price).
 * The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public final class InterestRateFutureOptionMarginTransactionBlackSmileMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSmileMethod INSTANCE = new InterestRateFutureOptionMarginTransactionBlackSmileMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionBlackSmileMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The option security pricing method..
   */
  private static final InterestRateFutureOptionMarginSecurityBlackSmileMethod METHOD_SECURITY = InterestRateFutureOptionMarginSecurityBlackSmileMethod.getInstance();

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param option The future option.
   * @param price The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final InterestRateFutureOptionMarginTransaction option, final double price) {
    ArgumentChecker.notNull(option, "Option on STIR futures");
    double pv = (price - option.getReferencePrice()) * option.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor() * option.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * option.getQuantity();
    return MultipleCurrencyAmount.of(option.getUnderlyingOption().getCurrency(), pv);
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
    double priceSecurity = METHOD_SECURITY.priceFromFuturePrice(transaction.getUnderlyingOption(), blackData, priceFuture);
    MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  public MultipleCurrencyAmount presentValue(final InterestRateFutureOptionMarginTransaction transaction, final BlackSTIRFuturesSmileProviderInterface blackData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    double priceSecurity = METHOD_SECURITY.price(transaction.getUnderlyingOption(), blackData);
    MultipleCurrencyAmount pvTransaction = presentValueFromPrice(transaction, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The Black volatility and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionMarginTransaction transaction, BlackSTIRFuturesSmileProviderInterface blackData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    MulticurveSensitivity securitySensitivity = METHOD_SECURITY.priceCurveSensitivity(transaction.getUnderlyingOption(), blackData);
    return MultipleCurrencyMulticurveSensitivity.of(
        transaction.getCurrency(),
        securitySensitivity.multipliedBy(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
            * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor()));
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
    SurfaceValue securitySensitivity = METHOD_SECURITY.priceBlackSensitivity(transaction.getUnderlyingOption(), blackData);
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
    final double securityGamma = METHOD_SECURITY.priceGamma(transaction.getUnderlyingOption(), blackData);
    final double txnGamma = securityGamma * transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnGamma;
  }
}

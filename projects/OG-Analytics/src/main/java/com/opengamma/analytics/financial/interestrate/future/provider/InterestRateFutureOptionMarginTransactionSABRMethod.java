/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date, 
 * i.e. 0 for normal options and x for x-year mid-curve options.
 */
public final class InterestRateFutureOptionMarginTransactionSABRMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionSABRMethod INSTANCE = new InterestRateFutureOptionMarginTransactionSABRMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionSABRMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionSABRMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The option security pricing method..
   */
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SECURITY = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();

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
   * @param sabrData The SABR and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionMarginTransaction transaction, final SABRSTIRFuturesProviderInterface sabrData, final double priceFuture) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(sabrData, "SABR / multi-curves provider");
    double priceSecurity = METHOD_SECURITY.priceFromFuturePrice(transaction.getUnderlyingOption(), sabrData, priceFuture);
    MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value of a transaction from the curves and volatility data.
   * @param transaction The future option transaction.
   * @param sabrData The SABR and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InterestRateFutureOptionMarginTransaction transaction, final SABRSTIRFuturesProviderInterface sabrData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(sabrData, "SABR / multi-curves provider");
    double priceSecurity = METHOD_SECURITY.price(transaction.getUnderlyingOption(), sabrData);
    MultipleCurrencyAmount pvTransaction = presentValueFromPrice(transaction, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param sabrData The SABR and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final SABRSTIRFuturesProviderInterface sabrData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(sabrData, "SABR / multi-curves provider");
    MulticurveSensitivity securitySensitivity = METHOD_SECURITY.priceCurveSensitivity(transaction.getUnderlyingOption(), sabrData);
    return MultipleCurrencyMulticurveSensitivity.of(
        transaction.getCurrency(),
        securitySensitivity.multipliedBy(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
            * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor()));
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param sabrData The SABR and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final SABRSTIRFuturesProviderInterface sabrData) {
    PresentValueSABRSensitivityDataBundle securitySensitivity = METHOD_SECURITY.priceSABRSensitivity(transaction.getUnderlyingOption(), sabrData);
    securitySensitivity = securitySensitivity.multiplyBy(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

}

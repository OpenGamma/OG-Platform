/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date, 
 * i.e. 0 for normal options and x for x-year mid-curve options.
 */
public final class InterestRateFutureOptionMarginTransactionSABRMethod extends InterestRateFutureOptionMarginTransactionGenericMethod<SABRSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionSABRMethod INSTANCE = new InterestRateFutureOptionMarginTransactionSABRMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionSABRMethod() {
    super(InterestRateFutureOptionMarginSecuritySABRMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionSABRMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   * @return The method.
   */
  @Override
  public InterestRateFutureOptionMarginSecuritySABRMethod getSecurityMethod() {
    return (InterestRateFutureOptionMarginSecuritySABRMethod) super.getSecurityMethod();
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
    double priceSecurity = getSecurityMethod().priceFromFuturePrice(transaction.getUnderlyingSecurity(), sabrData, priceFuture);
    MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param sabrData The SABR and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final SABRSTIRFuturesProviderInterface sabrData) {
    PresentValueSABRSensitivityDataBundle securitySensitivity = getSecurityMethod().priceSABRSensitivity(transaction.getUnderlyingSecurity(), sabrData);
    securitySensitivity = securitySensitivity.multiplyBy(transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

}

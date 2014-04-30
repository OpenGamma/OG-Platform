/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for normal options and x for x-year mid-curve options.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionSABRMethod}
 */
@Deprecated
public final class InterestRateFutureOptionMarginTransactionSABRMethod extends InterestRateFutureOptionMarginTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionSABRMethod INSTANCE = new InterestRateFutureOptionMarginTransactionSABRMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionSABRMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionSABRMethod() {
    super(InterestRateFutureOptionMarginSecuritySABRMethod.getInstance());
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param sabrData The SABR data bundle.
   * @return The present value curve sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final SABRInterestRateDataBundle sabrData) {
    PresentValueSABRSensitivityDataBundle securitySensitivity = ((InterestRateFutureOptionMarginSecuritySABRMethod) getSecurityMethod()).priceSABRSensitivity(transaction.getUnderlyingSecurity(),
        sabrData);
    securitySensitivity = securitySensitivity.multiplyBy(transaction.getQuantity() * transaction.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

}

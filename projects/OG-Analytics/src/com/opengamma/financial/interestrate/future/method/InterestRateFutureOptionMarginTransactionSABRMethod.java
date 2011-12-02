/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date, 
 * i.e. 0 for normal options and x for x-year mid-curve options.
 */
public final class InterestRateFutureOptionMarginTransactionSABRMethod extends InterestRateFutureOptionMarginTransactionMethod {
  private static final InterestRateFutureOptionMarginTransactionSABRMethod INSTANCE = new InterestRateFutureOptionMarginTransactionSABRMethod();
  
  public static InterestRateFutureOptionMarginTransactionSABRMethod getInstance() {
    return INSTANCE;
  }
  /**
   * The method used to compute the underlying security price.
   */
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SECURITY = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();

  private InterestRateFutureOptionMarginTransactionSABRMethod() {
  }

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
   * Computes the present value of a transaction from yield curves and SABR data.
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
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureOptionMarginTransaction, "Interest rate future option transaction");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "SABR data required");
    return presentValue((InterestRateFutureOptionMarginTransaction) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param sabrData The SABR data bundle. 
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final SABRInterestRateDataBundle sabrData) {
    InterestRateCurveSensitivity securitySensitivity = METHOD_SECURITY.priceCurveSensitivity(transaction.getUnderlyingOption(), sabrData);
    return securitySensitivity.multiply(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param sabrData The SABR data bundle. 
   * @return The present value curve sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final SABRInterestRateDataBundle sabrData) {
    PresentValueSABRSensitivityDataBundle securitySensitivity = METHOD_SECURITY.priceSABRSensitivity(transaction.getUnderlyingOption(), sabrData);
    securitySensitivity.multiply(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

}

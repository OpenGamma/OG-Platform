/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Methods for the pricing of Federal Funds futures transactions by discounting (no convexity adjustment).
 */
public final class FederalFundsFutureTransactionDiscountingMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FederalFundsFutureTransactionDiscountingMethod INSTANCE = new FederalFundsFutureTransactionDiscountingMethod();

  /**
   * Constructor.
   */
  private FederalFundsFutureTransactionDiscountingMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FederalFundsFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Method
   */
  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_FFFUT_SEC = FederalFundsFutureSecurityDiscountingMethod.getInstance();

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param futures The future.
   * @param price The security price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final FederalFundsFutureTransaction futures, final double price) {
    ArgumentChecker.notNull(futures, "Futures");
    double pv = (price - futures.getReferencePrice()) * futures.getUnderlyingFuture().getPaymentAccrualFactor() * futures.getUnderlyingFuture().getNotional() * futures.getQuantity();
    return MultipleCurrencyAmount.of(futures.getUnderlyingFuture().getCurrency(), pv);
  }

  /**
   * Compute the present value of a future transaction from the curves.
   * @param futures The future.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FederalFundsFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    double price = METHOD_FFFUT_SEC.price(futures.getUnderlyingFuture(), multicurves);
    return presentValueFromPrice(futures, price);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param futures The future.
   * @param multicurves The multi-curve provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final FederalFundsFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    MulticurveSensitivity priceSensi = METHOD_FFFUT_SEC.priceCurveSensitivity(futures.getUnderlyingFuture(), multicurves);
    MulticurveSensitivity result = priceSensi.multipliedBy(futures.getUnderlyingFuture().getPaymentAccrualFactor() * futures.getUnderlyingFuture().getNotional() * futures.getQuantity());
    return MultipleCurrencyMulticurveSensitivity.of(futures.getUnderlyingFuture().getCurrency(), result);
  }

}

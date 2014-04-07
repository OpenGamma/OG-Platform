/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.provider.calculator.singlevalue.FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Generic futures transaction pricing method.
 */
public class FuturesTransactionBlackFlatBondFuturesMethod extends FuturesTransactionMethod {

  /** The calculator used to compute the present value curve sensitivity from the price curve sensitivity **/
  private static final FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator PVCSIC = FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator.getInstance();

  /**
   * Default constructor.
   */
  public FuturesTransactionBlackFlatBondFuturesMethod() {
    super(new FuturesSecurityBlackBondFuturesMethod());
  }

  /**
   * Gets the securityMethod.
   * @return the securityMethod
   */
  @Override
  public FuturesSecurityBlackBondFuturesMethod getSecurityMethod() {
    return (FuturesSecurityBlackBondFuturesMethod) super.getSecurityMethod();
  }

  /**
   * Compute the present value of a future transaction from a curve provider.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FuturesTransaction<?> futures, final BlackBondFuturesProviderInterface multicurve) {
    double price = getSecurityMethod().price(futures.getUnderlyingFuture(), multicurve);
    return presentValueFromPrice(futures, price);
  }

  /**
   * Compute the present value curve sensitivity to rates of a future.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final FuturesTransaction<?> futures, final BlackBondFuturesProviderInterface multicurve) {
    final MulticurveSensitivity priceSensitivity = getSecurityMethod().priceCurveSensitivity(futures.getUnderlyingFuture(), multicurve);
    return futures.accept(PVCSIC, priceSensitivity);

  }

}

/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.provider.calculator.singlevalue.FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface to generic futures security pricing method.
 */
public class FuturesTransactionHullWhiteIssuerMethod extends FuturesTransactionMethod {

  private static final FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator PVCSIC = FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator.getInstance();

  /**
   * Constructor.
   */
  public FuturesTransactionHullWhiteIssuerMethod() {
    super(new FuturesSecurityHullWhiteIssuerMethod());
  }

  /**
   * Gets the securityMethod.
   * @return the securityMethod
   */
  @Override
  public FuturesSecurityHullWhiteIssuerMethod getSecurityMethod() {
    return (FuturesSecurityHullWhiteIssuerMethod) super.getSecurityMethod();
  }

  /**
   * Compute the present value of a future transaction from a curve provider.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FuturesTransaction<?> futures, final HullWhiteIssuerProviderInterface multicurve) {
    double price = getSecurityMethod().price(futures.getUnderlyingSecurity(), multicurve);
    return presentValueFromPrice(futures, price);
  }

  /**
   * Compute the present value curve sensitivity to rates of a future.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value rate sensitivity.
   */

  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final FuturesTransaction<?> futures, final HullWhiteIssuerProviderInterface multicurve) {
    final MulticurveSensitivity priceSensitivity = getSecurityMethod().priceCurveSensitivity(futures.getUnderlyingSecurity(), multicurve);
    return futures.accept(PVCSIC, priceSensitivity);

  }

}

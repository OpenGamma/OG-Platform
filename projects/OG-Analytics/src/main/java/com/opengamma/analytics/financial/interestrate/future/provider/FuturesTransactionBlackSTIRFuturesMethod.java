/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSTIRFuturesCubeSensitivity;
import com.opengamma.analytics.financial.provider.calculator.singlevalue.FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator;
import com.opengamma.analytics.financial.provider.calculator.singlevalue.FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Generic futures transaction pricing method.
 */
public class FuturesTransactionBlackSTIRFuturesMethod extends FuturesTransactionMethod {

  /** The calculator used to compute the present value curve sensitivity from the price curve sensitivity **/
  private static final FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator PVCSIC =
      FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator.getInstance();
  /** The calculator used to compute the present value Black sensitivity from the price Black sensitivity **/
  private static final FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator PVBSIC =
      FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator.getInstance();

  /**
   * Default constructor.
   */
  public FuturesTransactionBlackSTIRFuturesMethod() {
    super(new FuturesSecurityBlackSTIRFuturesMethod());
  }

  /**
   * Gets the securityMethod.
   * @return the securityMethod
   */
  @Override
  public FuturesSecurityBlackSTIRFuturesMethod getSecurityMethod() {
    return (FuturesSecurityBlackSTIRFuturesMethod) super.getSecurityMethod();
  }

  /**
   * Compute the present value of a future transaction from a curve provider.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FuturesTransaction<?> futures, final BlackSTIRFuturesProviderInterface multicurve) {
    double price = getSecurityMethod().price(futures.getUnderlyingSecurity(), multicurve);
    return presentValueFromPrice(futures, price);
  }

  /**
   * Compute the present value curve sensitivity to rates of a future transaction.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final FuturesTransaction<?> futures, final BlackSTIRFuturesProviderInterface multicurve) {
    final MulticurveSensitivity priceSensitivity = getSecurityMethod().priceCurveSensitivity(futures.getUnderlyingSecurity(), multicurve);
    return futures.accept(PVCSIC, priceSensitivity);
  }

  /**
   * Compute the present value Black sensitivity of a future transaction.
   * @param futures The futures.
   * @param multicurve The multicurve and parameters provider.
   * @return The present value Black parameters sensitivity.
   */
  public PresentValueBlackSTIRFuturesCubeSensitivity presentValueBlackSensitivity(final FuturesTransaction<?> futures, final BlackSTIRFuturesProviderInterface multicurve) {
    final PresentValueBlackSTIRFuturesCubeSensitivity priceSensitivity = getSecurityMethod().priceBlackSensitivity(futures.getUnderlyingSecurity(), multicurve);
    return futures.accept(PVBSIC, priceSensitivity);
  }

}

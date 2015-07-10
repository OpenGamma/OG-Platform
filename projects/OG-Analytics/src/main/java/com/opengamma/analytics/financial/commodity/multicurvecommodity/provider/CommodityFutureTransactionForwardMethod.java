/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.provider;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CommodityFutureTransaction;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.MultipleCurrencyCommoditySensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * The unique instance of the calculator for commodity future transaction present value.
 */
public final class CommodityFutureTransactionForwardMethod extends CommodityFutureTransactionMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final CommodityFutureTransactionForwardMethod INSTANCE = new CommodityFutureTransactionForwardMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CommodityFutureTransactionForwardMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private CommodityFutureTransactionForwardMethod() {
  }

  private static final CommodityFutureSecurityForwardMethod METHOD_SECURITY = CommodityFutureSecurityForwardMethod.getInstance();

  /**
   * Computes the present value without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CommodityFutureTransaction futures, final CommodityProviderInterface multicurves) {
    return presentValueFromPrice(futures, METHOD_SECURITY.price(futures.getUnderlying(), multicurves));
  }

  /**
   * Computes the present value curve sensitivity by discounting without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyCommoditySensitivity presentValueCurveSensitivity(final CommodityFutureTransaction futures, final CommodityProviderInterface multicurves) {
    return presentValueCurveSensitivity(futures, METHOD_SECURITY.priceCurveSensitivity(futures.getUnderlying(), multicurves));
  }

}

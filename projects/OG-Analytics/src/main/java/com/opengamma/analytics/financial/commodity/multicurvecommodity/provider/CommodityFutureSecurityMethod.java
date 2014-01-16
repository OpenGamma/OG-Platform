/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.provider;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CommodityFutureSecurity;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.CommoditySensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Methods for the pricing of commodity futures generic to all models.
 */
public abstract class CommodityFutureSecurityMethod {

  /**
   * Compute the price of a commodity Future in a given model.
   * @param future The  future.
   * @param multicurve The multi-curve and parameters provider.
   * @return The price.
   */
  abstract double price(final CommodityFutureSecurity future, final CommodityProviderInterface multicurve);

  /**
   * Compute the price of a commodity Future in a given model.
   * @param future The  future.
   * @param multicurve The multi-curve and parameters provider.
   * @return The net amount.
   */
  abstract double netAmount(final CommodityFutureSecurity future, final CommodityProviderInterface multicurve);

  /**
   * Compute the price sensitivity to interest rates and commodity rates  of a interest rate future by discounting.
   * @param future The future.
   * @param multicurve The multi-curves provider. 
   * @return The price rate sensitivity.
   */
  public abstract CommoditySensitivity priceCurveSensitivity(final CommodityFutureSecurity future, final CommodityProviderInterface multicurve);

  /**
   * Returns the convexity adjustment, i.e. the difference between the price and the forward commodity value from the curve .
   * @param future The future.
   * @param multicurve The multi-curve and parameters provider.
   * @return The adjustment.
   */
  public double convexityAdjustment(final CommodityFutureSecurity future, final CommodityProviderInterface multicurve) {
    ArgumentChecker.notNull(future, "commodity future");
    ArgumentChecker.notNull(multicurve, "parameter provider");
    final double forward = multicurve.getForwardValue(future.getUnderlying(), future.getSettlementTime());
    final double price = price(future, multicurve);
    return price - forward;
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CommodityFutureSecurity;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.commodity.CommoditySensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 */
public final class CommodityFutureSecurityForwardMethod extends CommodityFutureSecurityMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final CommodityFutureSecurityForwardMethod INSTANCE = new CommodityFutureSecurityForwardMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CommodityFutureSecurityForwardMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private CommodityFutureSecurityForwardMethod() {
  }

  /**
   * Computes the price of a future from the curves using an estimation of the futures rate without convexity adjustment.
   * @param future The future.
   * @param multicurves The multi-curve provider.
   * @return The price.
   */
  @Override
  public double price(final CommodityFutureSecurity future, final CommodityProviderInterface multicurves) {
    ArgumentChecker.notNull(future, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    return multicurves.getForwardValue(future.getUnderlying(), future.getSettlementTime());
  }

  @Override
  double netAmount(final CommodityFutureSecurity future, final CommodityProviderInterface multicurve) {
    return price(future, multicurve) / multicurve.getDiscountFactor(future.getCurrency(), future.getSettlementTime());
  }

  /**
   * Compute the price sensitivity to rates of a commodity future .
   * @param future The future.
   * @param multicurves The multi-curve provider.
   * @return The price rate sensitivity.
   */
  @Override
  public CommoditySensitivity priceCurveSensitivity(final CommodityFutureSecurity future, final CommodityProviderInterface multicurves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");

    // Backward sweep
    final double pvBar = 1.0;
    final double forwardBar = pvBar;

    final Map<String, List<DoublesPair>> resultMapCommodity = new HashMap<>();
    final List<DoublesPair> listPrice = new ArrayList<>();
    listPrice.add(DoublesPair.of(future.getSettlementTime(), forwardBar));
    resultMapCommodity.put(multicurves.getName(future.getUnderlying()), listPrice);
    return CommoditySensitivity.ofCommodityForwardValue(resultMapCommodity);
  }
}

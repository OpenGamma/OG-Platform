/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Set;

import com.opengamma.core.position.Trade;

/**
 * Returns the names of the multicurve bundles that should be used in calculations for a trade.
 */
public interface CurveSelectorFn {

  /**
   * Returns the names of the multicurve bundles that should be used in calculations for the trade.
   *
   * @param trade the trade
   * @return the names of the multicurve bundles that should be used in calculations for the trade
   */
  Set<String> getMulticurveNames(Trade trade);
}

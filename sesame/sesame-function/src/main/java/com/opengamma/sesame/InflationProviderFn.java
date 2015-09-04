/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.result.Result;

/**
 * Creates a inflation bundle for curves .
 */
public interface InflationProviderFn {

  /**
   * Returns the inflation bundle for curves for a trade.
   *
   * @param env the environment to return the inflation bundle for.
   * @param trade the trade to return the inflation bundle for.
   * @return the inflation bundle
   */
  Result<InflationProviderBundle> getInflationBundle(Environment env, ZeroCouponInflationSwapTrade trade);
}

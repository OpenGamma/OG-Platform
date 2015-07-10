/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.equity;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.result.Result;

/**
 * Function to return instances of {@link StaticReplicationDataBundle}.
 */
public interface StaticReplicationDataBundleFn {

  /**
   * Returns the {@link StaticReplicationDataBundle} data bundle for an equity index options trade.
   *
   * @param env the environment to create the black data bundle for.
   * @param trade the trade to create the black data bundle for.
   * @return the data bundle for an equity index option trade.
   */
  Result<StaticReplicationDataBundle> getEquityIndexDataProvider(Environment env, EquityIndexOptionTrade trade);
}

/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.result.Result;

/**
 * Function to return instances of {@link ForwardCurve}.
 */
public interface ForwardCurveFn {

  /**
   * Returns the {@link ForwardCurve} for an equity index options trade.
   *
   * @param env the environment to create the black data bundle for.
   * @param trade the trade to create the black data bundle for.
   * @return the forward for an equity index option trade.
   */
  Result<ForwardCurve> getEquityIndexForwardCurve(Environment env, EquityIndexOptionTrade trade);
}

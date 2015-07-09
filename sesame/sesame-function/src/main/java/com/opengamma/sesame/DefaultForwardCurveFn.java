/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.sesame.marketdata.ForwardCurveId;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.result.Result;

/**
 * Provides a forward curve from the market date environment
 */
public class DefaultForwardCurveFn implements ForwardCurveFn {

  @Override
  public Result<ForwardCurve> getEquityIndexForwardCurve(Environment env, EquityIndexOptionTrade tradeWrapper) {
    EquityIndexOptionSecurity security = tradeWrapper.getSecurity();
    ForwardCurveId forwardCurveId = ForwardCurveId.of(security.getUnderlyingId().getValue());
    return env.getMarketDataBundle().get(forwardCurveId, ForwardCurve.class);
  }
}

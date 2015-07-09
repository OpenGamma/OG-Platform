/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.result.Result;

/**
 * Provides a constant forward curve built from a single underlying price
 */
public class UnderlyingForwardCurveFn implements ForwardCurveFn {

  @Override
  public Result<ForwardCurve> getEquityIndexForwardCurve(Environment env, EquityIndexOptionTrade tradeWrapper) {
    EquityIndexOptionSecurity security = tradeWrapper.getSecurity();
    RawId<Double> rawId = RawId.of(security.getUnderlyingId().toBundle());
    Result<Double> underlyingResult = env.getMarketDataBundle().get(rawId, Double.class);
    if (underlyingResult.isSuccess()) {
      // creation of a constant curve as the only point of interest is the expiry of the option
      ForwardCurve forwardCurve = new ForwardCurve(underlyingResult.getValue());
      return Result.success(forwardCurve);
    } else {
      return Result.failure(underlyingResult);
    }
  }
}

/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.result.Result;

/**
 * Function to provide the Normal volatility surface for interest rate future options
 */
public interface IRFutureOptionNormalSurfaceProviderFn {

/**
 * Returns the normal surface provider for a IR future option.
 *
 * @param env the environment.
 * @param trade the IRFutureOptionTrade trade
 * @param underlyingFuture
 * @param value
 * @return the NormalSTIRFuturesExpSimpleMoneynessProviderDiscount for a IR future option trade.
 */
  Result<NormalSTIRFuturesExpSimpleMoneynessProviderDiscount> getNormalSurfaceProvider(Environment env,
                                                                                       IRFutureOptionTrade trade,
                                                                                       InterestRateFutureSecurity underlyingFuture,
                                                                                       MulticurveBundle value);

}

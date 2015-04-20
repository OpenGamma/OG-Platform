/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Test function to provide the Normal volatility surface for interest rate future options
 */
public class TestIRFutureOptionNormalSurfaceProviderFn implements IRFutureOptionNormalSurfaceProviderFn {

  private final boolean _moneynessOnPrice;

  /**
   * Constructs IRFutureOptionNormalSurfaceProviderFn to provide the normal volatility surface

   * @param moneynessOnPrice flag indicating if the moneyness is on the price (true) or on the rate (false).
   */
  public TestIRFutureOptionNormalSurfaceProviderFn(boolean moneynessOnPrice) {
    _moneynessOnPrice = ArgumentChecker.notNull(moneynessOnPrice, "moneynessOnPrice");
  }

  @Override
  public Result<NormalSTIRFuturesExpSimpleMoneynessProviderDiscount> getNormalSurfaceProvider(Environment env,
                                                                                              IRFutureOptionTrade trade,
                                                                                              InterestRateFutureSecurity underlyingFuture,
                                                                                              MulticurveBundle multicurveBundle) {
    Result<VolatilitySurface> surfaceResult =
        env.getMarketDataBundle().get(VolatilitySurfaceId.of(trade.getSecurity().getExchange()), VolatilitySurface.class);

    if (!surfaceResult.isSuccess()) {
      return Result.failure(surfaceResult);
    }

    VolatilitySurface volSurface = surfaceResult.getValue();

    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalSurface =
        new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(multicurveBundle.getMulticurveProvider(),
                                                                volSurface.getSurface(),
                                                                underlyingFuture.getIborIndex(),
                                                                _moneynessOnPrice);
    return Result.success(normalSurface);
  }
}

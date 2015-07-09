/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.equity;

import java.util.Map;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ForwardCurveFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * {@link StaticReplicationDataBundleFn } implementation to return instances of
 * {@link StaticReplicationDataBundle} with {@link BlackVolatilitySurfaceStrike}.
 */
public class StrikeDataBundleFn implements StaticReplicationDataBundleFn {

  private final DiscountingMulticurveCombinerFn _multicurveCombinerFn;
  private final ForwardCurveFn _forwardCurveFn;

  /**
   * Constructors a black volatility provider function for bond future options.
   * @param discountingMulticurveCombinerFn the discounting multicurve combiner function, not null.
   * @param forwardCurveFn the function to provide the forward curve, not null.
   */
  public StrikeDataBundleFn(DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn,
                            ForwardCurveFn forwardCurveFn) {
    _multicurveCombinerFn = ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
    _forwardCurveFn = ArgumentChecker.notNull(forwardCurveFn, "forwardCurveFn");
  }

  @Override
  public Result<StaticReplicationDataBundle> getEquityIndexDataProvider(Environment env,
                                                                        EquityIndexOptionTrade tradeWrapper) {
    EquityIndexOptionSecurity security = tradeWrapper.getSecurity();
    Result<MulticurveBundle> bundleResult = _multicurveCombinerFn.getMulticurveBundle(env, tradeWrapper);
    String volId = security.getOptionType() + "_" + security.getUnderlyingId().getValue();
    Result<VolatilitySurface> surfaceResult =
        env.getMarketDataBundle().get(VolatilitySurfaceId.of(volId), VolatilitySurface.class);
    Result<ForwardCurve> forwardResult = _forwardCurveFn.getEquityIndexForwardCurve(env, tradeWrapper);

    if (Result.allSuccessful(bundleResult, surfaceResult, forwardResult)) {
      MulticurveProviderDiscount multicurve = bundleResult.getValue().getMulticurveProvider();
      VolatilitySurface volSurface = surfaceResult.getValue();
      ForwardCurve forwardCurve = forwardResult.getValue();

      Map<Currency, YieldAndDiscountCurve> discountingCurves = multicurve.getDiscountingCurves();
      YieldAndDiscountCurve discountCurve = discountingCurves.get(security.getCurrency());

      BlackVolatilitySurfaceStrike blackVolSurface = new BlackVolatilitySurfaceStrike(volSurface.getSurface());
      StaticReplicationDataBundle bundle = new StaticReplicationDataBundle(blackVolSurface,
                                                                           discountCurve,
                                                                           forwardCurve);
      return Result.success(bundle);
    } else {
      return Result.failure(bundleResult, surfaceResult, forwardResult);
    }
  }

}

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
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.surface.DoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ForwardCurveFn;
import com.opengamma.sesame.GridInterpolator2DFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.SurfaceId;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * {@link StaticReplicationDataBundleFn } implementation to return instances of
 * {@link StaticReplicationDataBundle} with {@link BlackVolatilitySurfaceStrike}.
 */
public class StrikeDataFromPriceBundleFn implements StaticReplicationDataBundleFn {

  private final DiscountingMulticurveCombinerFn _multicurveCombinerFn;
  private final ForwardCurveFn _forwardCurveFn;
  private final GridInterpolator2DFn _interpolatorFn;

  /**
   * Constructors a black volatility provider function for bond future options.
   * @param discountingMulticurveCombinerFn the discounting multicurve combiner function, not null.
   * @param forwardCurveFn the function to provide the forward curve, not null.
   * @param interpolatorFn the interpolator to use when constructing the surface
   */
  public StrikeDataFromPriceBundleFn(DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn,
                                     ForwardCurveFn forwardCurveFn,
                                     GridInterpolator2DFn interpolatorFn) {
    _interpolatorFn = ArgumentChecker.notNull(interpolatorFn, "interpolator");
    _multicurveCombinerFn = ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
    _forwardCurveFn = ArgumentChecker.notNull(forwardCurveFn, "forwardCurveFn");
  }

  @Override
  public Result<StaticReplicationDataBundle> getEquityIndexDataProvider(Environment env,
                                                                        EquityIndexOptionTrade tradeWrapper) {
    EquityIndexOptionSecurity security = tradeWrapper.getSecurity();
    Result<MulticurveBundle> bundleResult = _multicurveCombinerFn.getMulticurveBundle(env, tradeWrapper);
    String volId = security.getOptionType() + "_" + security.getUnderlyingId().getValue();
    Result<DoublesSurface> surfaceResult =
        env.getMarketDataBundle().get(SurfaceId.of(volId), DoublesSurface.class);
    Result<ForwardCurve> forwardResult = _forwardCurveFn.getEquityIndexForwardCurve(env, tradeWrapper);
    Result<GridInterpolator2D> interpolatorResult = _interpolatorFn.createGridInterpolator2DFn(env);

    if (Result.allSuccessful(bundleResult, surfaceResult, forwardResult, interpolatorResult)) {

      boolean isCall = security.getOptionType().equals(OptionType.CALL);
      MulticurveProviderDiscount multicurve = bundleResult.getValue().getMulticurveProvider();
      DoublesSurface rawSurface = surfaceResult.getValue();
      ForwardCurve forwardCurve = forwardResult.getValue();
      Map<Currency, YieldAndDiscountCurve> discountingCurves = multicurve.getDiscountingCurves();
      YieldAndDiscountCurve discountCurve = discountingCurves.get(security.getCurrency());

      Double[] priceData = rawSurface.getZData();
      Double[] timeData = rawSurface.getXData();
      Double[] strikeData = rawSurface.getYData();
      Double[] volData = new Double[priceData.length];

      for (int i = 0; i < priceData.length; i++) {

        double timeToExpiry = timeData[i];
        double optionPrice = priceData[i];
        double forwardOptionPrice = optionPrice / discountCurve.getDiscountFactor(timeToExpiry);
        double forward = forwardCurve.getForward(timeToExpiry);

        try {
          volData[i] = BlackFormulaRepository.impliedVolatility(forwardOptionPrice,
                                                                forward,
                                                                strikeData[i],
                                                                timeToExpiry,
                                                                isCall);
        } catch (Exception e) {
          return Result.failure(FailureStatus.INVALID_INPUT, e,
                                "Error constructing surface {} for price {} and strike {} at maturity {}. {}",
                                volId, optionPrice, strikeData[i], timeToExpiry, e.getMessage());

        }
      }

      InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(timeData,
                                                                           strikeData,
                                                                           volData,
                                                                           interpolatorResult.getValue());
      BlackVolatilitySurfaceStrike blackVolSurface = new BlackVolatilitySurfaceStrike(surface);
      StaticReplicationDataBundle bundle = new StaticReplicationDataBundle(blackVolSurface,
                                                                           discountCurve,
                                                                           forwardCurve);
      return Result.success(bundle);
    } else {
      return Result.failure(bundleResult, surfaceResult, forwardResult, interpolatorResult);
    }
  }

}

/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Uses a curve selector to find the curve names for a trade and looks up the named curve bundles
 * in a {@link MarketDataBundle}.
 */
public class CurveSelectorMulticurveBundleFn implements DiscountingMulticurveCombinerFn {

  private final CurveSelectorFn _curveSelectorFn;

  public CurveSelectorMulticurveBundleFn(CurveSelectorFn curveSelectorFn) {
    _curveSelectorFn = ArgumentChecker.notNull(curveSelectorFn, "curveSelectorFn");
  }

  @Override
  public Result<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> createMergedMulticurveBundle(
      Environment env,
      TradeWrapper trade,
      FXMatrix fxMatrix) {

    Result<MulticurveBundle> result = getMulticurveBundle(env, trade);

    if (!result.isSuccess()) {
      return Result.failure(result);
    }
    MulticurveBundle bundle = result.getValue();
    return Result.success(Pairs.of(bundle.getMulticurveProvider(), bundle.getCurveBuildingBlockBundle()));
  }

  @Override
  public Result<MulticurveBundle> getMulticurveBundle(Environment env, TradeWrapper<?> trade) {
    Set<String> multicurveNames = _curveSelectorFn.getMulticurveNames(trade.getTrade());
    List<MulticurveBundle> bundleResults = new ArrayList<>();
    List<Result<MulticurveBundle>> failures = new ArrayList<>();

    for (String multicurveName : multicurveNames) {
      Result<MulticurveBundle> result = env.getMarketDataBundle().get(MulticurveId.of(multicurveName),
                                                                      MulticurveBundle.class);
      if (!result.isSuccess()) {
        failures.add(result);
      } else {
        bundleResults.add(result.getValue());
      }
    }
    if (!failures.isEmpty()) {
      return Result.failure(failures);
    }
    return Result.success(mergeBundles(bundleResults));
  }

  // TODO move to a helper class
  private static MulticurveBundle mergeBundles(List<MulticurveBundle> bundles) {
    if (bundles.size() == 1) {
      return bundles.get(0);
    }
    CurveBuildingBlockBundle mergedBlockBundle = new CurveBuildingBlockBundle();
    List<MulticurveProviderDiscount> multicurves = new ArrayList<>();

    for (MulticurveBundle bundle : bundles) {
      mergedBlockBundle.addAll(bundle.getCurveBuildingBlockBundle());
      multicurves.add(bundle.getMulticurveProvider());
    }
    MulticurveProviderDiscount mergedProvider = ProviderUtils.mergeDiscountingProviders(multicurves);
    return new MulticurveBundle(mergedProvider, mergedBlockBundle);
  }
}

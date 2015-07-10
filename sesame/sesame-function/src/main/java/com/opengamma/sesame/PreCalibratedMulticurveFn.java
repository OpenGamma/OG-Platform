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
import com.opengamma.sesame.function.scenarios.ScenarioFunction;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Returns a pre-calibrated curve that should be used for calculations for a trade.
 * <p>
 * The curve is taken from the scenario arguments.
 */
public class PreCalibratedMulticurveFn implements DiscountingMulticurveCombinerFn,
    ScenarioFunction<PreCalibratedMulticurveArguments, PreCalibratedMulticurveFn> {

  private final CurveSelector _curveSelector;

  /**
   * @param curveSelector provides the names of curves that should be used for a trade
   */
  public PreCalibratedMulticurveFn(CurveSelector curveSelector) {
    _curveSelector = ArgumentChecker.notNull(curveSelector, "exposureSelector");
  }

  /**
   * Delegates to {@link #getMulticurveBundle(Environment, TradeWrapper)}.
   *
   * @param env the environment used for calculations
   * @param trade the trade
   * @param fxMatrix not used
   * @return the multicurve bundle for the trade
   */
  @Override
  public Result<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> createMergedMulticurveBundle(
      Environment env, TradeWrapper trade, FXMatrix fxMatrix) {
    Result<MulticurveBundle> result = getMulticurveBundle(env, trade);

    if (!result.isSuccess()) {
      return Result.failure(result);
    }
    MulticurveBundle multicurve = result.getValue();
    return Result.success(Pairs.of(multicurve.getMulticurveProvider(), multicurve.getCurveBuildingBlockBundle()));
  }

  @Override
  public Result<MulticurveBundle> getMulticurveBundle(Environment env, TradeWrapper<?> trade) {
    List<PreCalibratedMulticurveArguments> scenarioArguments = env.getScenarioArguments(this);

    if (scenarioArguments.size() != 1) {
      return Result.failure(FailureStatus.MISSING_DATA, "One instance of PreCalibratedMulticurveArguments expected " +
          "in the scenario arguments, found {}", scenarioArguments.size());
    }
    List<MulticurveProviderDiscount> multicurves = new ArrayList<>();
    List<CurveBuildingBlockBundle> blockBundles = new ArrayList<>();
    List<Result<?>> failures = new ArrayList<>();
    Set<String> curveNames = _curveSelector.getMulticurveNames(trade.getTrade());

    if (curveNames.isEmpty()) {
      return Result.failure(FailureStatus.MISSING_DATA, "No matching curves found for trade: {}", trade);
    }
    // TODO it's unclear whether there will ever be multiple curves for a trade - if not this can be simplified
    
    if (curveNames.size() == 1) {
      return scenarioArguments.get(0).getMulticurveBundle(curveNames.iterator().next());
    }
    
    for (String curveName : curveNames) {
      Result<MulticurveBundle> multicurve = scenarioArguments.get(0).getMulticurveBundle(curveName);

      if (multicurve.isSuccess()) {
        multicurves.add(multicurve.getValue().getMulticurveProvider());
        blockBundles.add(multicurve.getValue().getCurveBuildingBlockBundle());
      } else {
        failures.add(multicurve);
      }
    }
    if (!failures.isEmpty()) {
      return Result.failure(failures);
    }
    MulticurveProviderDiscount multicurve = ProviderUtils.mergeDiscountingProviders(multicurves);
    CurveBuildingBlockBundle mergedBlockBundle = new CurveBuildingBlockBundle();

    for (CurveBuildingBlockBundle blockBundle : blockBundles) {
      mergedBlockBundle.addAll(blockBundle);
    }
    return Result.success(new MulticurveBundle(multicurve, mergedBlockBundle));
  }

  @Override
  public Class<PreCalibratedMulticurveArguments> getArgumentType() {
    return PreCalibratedMulticurveArguments.class;
  }
}

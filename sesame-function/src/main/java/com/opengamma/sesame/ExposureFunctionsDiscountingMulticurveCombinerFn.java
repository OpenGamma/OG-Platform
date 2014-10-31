/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Creates a collection of multicurves based on a security and market exposure
 * selector, combining them into a single multicurve.
 */
public class ExposureFunctionsDiscountingMulticurveCombinerFn implements DiscountingMulticurveCombinerFn {

  private static final Logger s_logger = LoggerFactory.getLogger(ExposureFunctionsDiscountingMulticurveCombinerFn.class);

  /**
   * Generates the market exposure selector. In turn this can be used to get
   * an ExposureFunction.
   */
  private final MarketExposureSelectorFn _marketExposureSelectorFn;

  /**
   * Generates a discounting multicurve bundle.
   */
  private final DiscountingMulticurveBundleResolverFn _bundleResolver;

  /**
   * Constructor for a multicurve function that selects the multicurves by either trade or security.
   *
   * @param marketExposureSelectorFn  the exposure function selector.
   * @param bundleResolver  the function used to resolve the multicurves.
   */
  public ExposureFunctionsDiscountingMulticurveCombinerFn(MarketExposureSelectorFn marketExposureSelectorFn,
                                                          DiscountingMulticurveBundleResolverFn bundleResolver) {
    _marketExposureSelectorFn =
        ArgumentChecker.notNull(marketExposureSelectorFn, "marketExposureSelectorFn");
    _bundleResolver = ArgumentChecker.notNull(bundleResolver, "bundleResolver");
  }
  
  @Override
  public Result<MulticurveBundle> getMulticurveBundle(Environment env, TradeWrapper trade, FXMatrix fxMatrix) {
    Result<MarketExposureSelector> mesResult = _marketExposureSelectorFn.getMarketExposureSelector();

    if (mesResult.isSuccess()) {
      Set<Result<?>> incompleteBundles = new HashSet<>();
      Set<MulticurveProviderDiscount> bundles = new HashSet<>();
      CurveBuildingBlockBundle mergedJacobianBundle = new CurveBuildingBlockBundle();

      MarketExposureSelector selector = mesResult.getValue();
      Set<CurveConstructionConfiguration> curveConfigs = selector.determineCurveConfigurations(trade.getTrade());
      for (CurveConstructionConfiguration curveConfig : curveConfigs) {

        s_logger.debug("Generating bundle '{}', valuationTime {}", curveConfig.getName(), env.getValuationTime());
        Result<MulticurveBundle> bundleResult = _bundleResolver.generateBundle(env, curveConfig);

        if (bundleResult.isSuccess()) {
          MulticurveBundle result = bundleResult.getValue();
          bundles.add(result.getMulticurveProvider());
          mergedJacobianBundle.addAll(result.getCurveBuildingBlockBundle());
        } else {
          incompleteBundles.add(bundleResult);
        }
      }

      // TODO this can be cleaned up
      if (!curveConfigs.isEmpty() && incompleteBundles.isEmpty()) {
        return Result.success(new MulticurveBundle(mergeBundlesAndMatrix(bundles, fxMatrix), mergedJacobianBundle));
      } else if (curveConfigs.isEmpty()) {
        return Result.failure(FailureStatus.MISSING_DATA, "No matching curves found for trade: {}", trade);
      } else {
        return Result.failure(incompleteBundles);
      }
    } else {
      return Result.failure(mesResult);
    }
  }

  @Override
  public Result<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> createMergedMulticurveBundle(
      Environment env, TradeWrapper trade, FXMatrix fxMatrix) {
    Result<MulticurveBundle> result = getMulticurveBundle(env, trade, fxMatrix);

    if (!result.isSuccess()) {
      return Result.failure(result);
    }
    MulticurveBundle multicurve = result.getValue();
    return Result.success(Pairs.of(multicurve.getMulticurveProvider(), multicurve.getCurveBuildingBlockBundle()));
  }

  @Override
  public Result<MulticurveBundle> getMulticurveBundle(Environment env, TradeWrapper<?> trade) {
    return getMulticurveBundle(env, trade, new FXMatrix());
  }


  private MulticurveProviderDiscount mergeBundlesAndMatrix(Collection<MulticurveProviderDiscount> providers,
                                                           FXMatrix fxMatrix) {
    return providers.size() > 1 ?
        ProviderUtils.mergeDiscountingProviders(mergeBundles(providers), fxMatrix) :
        providers.iterator().next();
  }

  private MulticurveProviderDiscount mergeBundles(Collection<MulticurveProviderDiscount> providers) {
    return ProviderUtils.mergeDiscountingProviders(providers);
  }
}

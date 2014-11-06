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

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.security.CurrenciesVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
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
  private final MarketExposureSelector _marketExposureSelector;

  /**
   * Generates a discounting multicurve bundle.
   */
  private final DiscountingMulticurveBundleResolverFn _bundleResolver;

  /** Generates a matrix of FX rates. */
  private final FXMatrixFn _fxMatrixFn;

  /**
   * Constructor for a multicurve function that selects the multicurves by either trade or security.
   *
   * @param marketExposureSelector  the exposure function selector.
   * @param bundleResolver  the function used to resolve the multicurves.
   * @param fxMatrixFn for generating a matrix of FX rates
   */
  public ExposureFunctionsDiscountingMulticurveCombinerFn(MarketExposureSelector marketExposureSelector,
                                                          DiscountingMulticurveBundleResolverFn bundleResolver,
                                                          FXMatrixFn fxMatrixFn) {
    _fxMatrixFn = ArgumentChecker.notNull(fxMatrixFn, "fxMatrixFn");
    _marketExposureSelector = ArgumentChecker.notNull(marketExposureSelector, "marketExposureSelector");
    _bundleResolver = ArgumentChecker.notNull(bundleResolver, "bundleResolver");
  }
  
  @Override
  public Result<MulticurveBundle> getMulticurveBundle(Environment env, TradeWrapper trade) {
    Set<Result<?>> incompleteBundles = new HashSet<>();
    Set<MulticurveProviderDiscount> bundles = new HashSet<>();
    CurveBuildingBlockBundle mergedJacobianBundle = new CurveBuildingBlockBundle();

    Set<CurveConstructionConfiguration> curveConfigs =
        _marketExposureSelector.determineCurveConfigurations(trade.getTrade());
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

    if (!curveConfigs.isEmpty() && incompleteBundles.isEmpty()) {
      Result<FXMatrix> fxMatrixResult = getFxMatrix(env, trade.getSecurity());

      if (fxMatrixResult.isSuccess()) {
        FXMatrix fxMatrix = fxMatrixResult.getValue();
        return Result.success(new MulticurveBundle(mergeBundlesAndMatrix(bundles, fxMatrix), mergedJacobianBundle));
      } else {
        return Result.failure(fxMatrixResult);
      }
    } else if (curveConfigs.isEmpty()) {
      return Result.failure(FailureStatus.MISSING_DATA, "No matching curves found for trade: {}", trade);
    } else {
      return Result.failure(incompleteBundles);
    }
  }

  /**
   * Returns an {@link FXMatrix} containing FX rates for all the currencies in the security.
   *
   * @param env the environment used in calculations
   * @param security a security
   * @return an {@code FXMatrix} containing FX rates for all the currencies in the security.
   */
  private Result<FXMatrix> getFxMatrix(Environment env, Security security) {
    if (security instanceof FinancialSecurity) {
      Collection<Currency> currencies = ((FinancialSecurity) security).accept(new CurrenciesVisitor());
      return _fxMatrixFn.getFXMatrix(env, ImmutableSet.copyOf(currencies));
    } else {
      return Result.failure(FailureStatus.CALCULATION_FAILED,
                            "Security {} isn't a FinancialSecurity, can't get currencies for FX matrix", security);
    }
  }

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

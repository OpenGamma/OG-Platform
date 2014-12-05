/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.marketdata.IssuerMulticurveMarketDataBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Function implementation that provides a discounting issuer provider curve bundle.
 *
 * @deprecated issuer curves are built using {@link IssuerMulticurveMarketDataBuilder}.
 */
public class DefaultDiscountingIssuerProviderBundleFn implements IssuerProviderBundleFn {

  private final CurveBundleProviderFn _curveBundleProviderFn;
  private final FXMatrixFn _fxMatrixProvider;
  private final RootFinderConfiguration _rootFinderConfiguration;
  private final CurveNodeInstrumentDefinitionFactory _curveNodeInstrumentDefinitionFactory;
  private final Set<String> _impliedCurveNames;

  /**
   * Creates the discounting issuer provider curve bundle.
   *
   * @param curveBundleProviderFn provides the curve bundle, not null.
   * @param fxMatrixProvider provides the fx matrix, not null.
   * @param rootFinderConfiguration root finder config, not null.
   * @param curveNodeInstrumentDefinitionFactory factory to build node definitions, not null.
   * @param impliedCurveNames set of implied curve names not null.
   *
   */
  //TODO PLAT-6801 remove conventionBundleSource once BondNodeConverter no longer uses it
  public DefaultDiscountingIssuerProviderBundleFn(CurveBundleProviderFn curveBundleProviderFn,
                                                  FXMatrixFn fxMatrixProvider,
                                                  RootFinderConfiguration rootFinderConfiguration,
                                                  CurveNodeInstrumentDefinitionFactory curveNodeInstrumentDefinitionFactory,
                                                  StringSet impliedCurveNames) {

    _curveBundleProviderFn = ArgumentChecker.notNull(curveBundleProviderFn, "curveBundleProviderFn");
    _fxMatrixProvider = ArgumentChecker.notNull(fxMatrixProvider, "fxMatrixProvider");
    _rootFinderConfiguration = ArgumentChecker.notNull(rootFinderConfiguration, "rootFinderConfiguration");
    _curveNodeInstrumentDefinitionFactory =
        ArgumentChecker.notNull(curveNodeInstrumentDefinitionFactory, "curveNodeInstrumentDefinitionFactory");
    ArgumentChecker.notNull(impliedCurveNames, "impliedCurveNames");
    _impliedCurveNames = impliedCurveNames.getStrings();
  }

  @Override
  public Result<IssuerProviderBundle> generateBundle(Environment env, CurveConstructionConfiguration curveConfig) {
    Result<FXMatrix> fxMatrixResult = _fxMatrixProvider.getFXMatrix(env, curveConfig);
    Result<IssuerProviderDiscount> exogenousBundles = buildExogenousBundles(fxMatrixResult);

    if (Result.allSuccessful(fxMatrixResult, exogenousBundles)) {
      Result<Pair<IssuerProviderDiscount, CurveBuildingBlockBundle>> calibratedCurves =
          _curveBundleProviderFn.getCurves(env, curveConfig, exogenousBundles.getValue(), fxMatrixResult.getValue(),
                                           _impliedCurveNames, createBuilder());
      if (calibratedCurves.isSuccess()) {
        IssuerProviderBundle bundle = new IssuerProviderBundle(calibratedCurves.getValue().getFirst(),
                                                               calibratedCurves.getValue().getSecond());
        return Result.success(bundle);
      } else {
        return Result.failure(calibratedCurves);
      }
    } else {
      return Result.failure(fxMatrixResult, exogenousBundles);
    }
  }

  private IssuerDiscountBuildingRepository createBuilder() {
    return new IssuerDiscountBuildingRepository(
        _rootFinderConfiguration.getAbsoluteTolerance(),
        _rootFinderConfiguration.getRelativeTolerance(),
        _rootFinderConfiguration.getMaxIterations());
  }

  private Result<IssuerProviderDiscount> buildExogenousBundles(Result<FXMatrix> fxMatrixResult) {
    //TODO PLAT-6800 add exogenous coverage here
    if (fxMatrixResult.isSuccess()) {
      FXMatrix fxMatrix = fxMatrixResult.getValue();
      return Result.success(new IssuerProviderDiscount(fxMatrix));
    } else {
      return Result.failure(fxMatrixResult);
    }
  }

}

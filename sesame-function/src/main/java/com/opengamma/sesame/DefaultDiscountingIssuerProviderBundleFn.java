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
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Function implementation that provides a discounting issuer provider curve bundle.
 */
public class DefaultDiscountingIssuerProviderBundleFn implements IssuerProviderBundleFn {

  private final CurveSpecificationFn _curveSpecificationProvider;
  private final CurveSpecificationMarketDataFn _curveSpecMarketDataProvider;
  private final FXMatrixFn _fxMatrixProvider;
  private final SecuritySource _securitySource;
  private final ConventionSource _conventionSource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final CurveNodeConverterFn _curveNodeConverter;
  private final LegalEntitySource _legalEntitySource;
  private final ConventionBundleSource _conventionBundleSource;
  private final RootFinderConfiguration _rootFinderConfiguration;
  private final Set<String> _impliedCurveNames;

  /**
   * Creates the discounting issuer provider curve bundle.
   *
   * @param curveSpecificationProvider provides the curve spec, not null.
   * @param curveSpecMarketDataProvider market data required for a curve specification, not null.
   * @param fxMatrixProvider provides the fx matrix, not null.
   * @param securitySource source for securities, not null.
   * @param conventionSource source for conventions, not null.
   * @param conventionBundleSource source for convention bundle, not null.
   * @param holidaySource source for holidays, not null.
   * @param regionSource source for regions, not null.
   * @param legalEntitySource source for legal entities, not null.
   * @param curveNodeConverter converter bor curve nodes, not null.
   * @param rootFinderConfiguration root finder config, not null.
   * @param impliedCurveNames set of implied curve names not null.
   *
   */
  //TODO PLAT-6801 remove conventionBundleSource once BondNodeConverter no longer uses it
  public DefaultDiscountingIssuerProviderBundleFn(CurveSpecificationFn curveSpecificationProvider,
                                                  CurveSpecificationMarketDataFn curveSpecMarketDataProvider,
                                                  FXMatrixFn fxMatrixProvider,
                                                  SecuritySource securitySource,
                                                  ConventionSource conventionSource,
                                                  ConventionBundleSource conventionBundleSource,
                                                  HolidaySource holidaySource,
                                                  RegionSource regionSource,
                                                  LegalEntitySource legalEntitySource,
                                                  CurveNodeConverterFn curveNodeConverter,
                                                  RootFinderConfiguration rootFinderConfiguration,
                                                  StringSet impliedCurveNames) {

    _curveSpecificationProvider = ArgumentChecker.notNull(curveSpecificationProvider, "curveSpecificationProvider");
    _curveSpecMarketDataProvider = ArgumentChecker.notNull(curveSpecMarketDataProvider, "curveSpecMarketDataProvider");
    _fxMatrixProvider = ArgumentChecker.notNull(fxMatrixProvider, "fxMatrixProvider");
    _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    _conventionSource = ArgumentChecker.notNull(conventionSource, "conventionSource");
    _conventionBundleSource = ArgumentChecker.notNull(conventionBundleSource, "conventionBundleSource");
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _legalEntitySource = ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    _curveNodeConverter = ArgumentChecker.notNull(curveNodeConverter, "curveNodeConverter");
    _rootFinderConfiguration = ArgumentChecker.notNull(rootFinderConfiguration, "rootFinderConfiguration");
    ArgumentChecker.notNull(impliedCurveNames, "impliedCurveNames");
    _impliedCurveNames = impliedCurveNames.getStrings();
  }

  @Override
  public Result<IssuerProviderBundle> generateBundle(Environment env, CurveConstructionConfiguration curveConfig) {
    Result<FXMatrix> fxMatrixResult = _fxMatrixProvider.getFXMatrix(env, curveConfig);

    Result<IssuerProviderDiscount> exogenousBundles = buildExogenousBundles(fxMatrixResult);

    CurveBundleProvider utils = new CurveBundleProvider(_conventionSource,
                                                        _conventionBundleSource,
                                                        _holidaySource,
                                                        _regionSource,
                                                        _legalEntitySource,
                                                        _securitySource,
                                                        _curveNodeConverter,
                                                        _curveSpecificationProvider,
                                                        _curveSpecMarketDataProvider);
    Result<Pair<IssuerProviderDiscount, CurveBuildingBlockBundle>> calibratedCurves =
        utils.getCurves(env, curveConfig, exogenousBundles, fxMatrixResult, _impliedCurveNames, createBuilder());
    IssuerProviderBundle bundle = new IssuerProviderBundle(calibratedCurves.getValue().getFirst(), calibratedCurves.getValue().getSecond());
    return Result.success(bundle);

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

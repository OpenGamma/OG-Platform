/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveNodeInstrumentDefinitionFactory;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Market data builder for multicurve bundles.
 */
public class MulticurveMarketDataBuilder
    extends AbstractMulticurveMarketDataBuilder<MulticurveBundle>
    implements MarketDataBuilder {

  private static final ParSpreadMarketQuoteDiscountingCalculator DISCOUNTING_CALCULATOR =
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();

  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator CURVE_SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private final MulticurveDiscountBuildingRepository _curveBuilder;

  /**
   * @param curveSpecBuilder for building curve specifications
   * @param curveNodeConverter for converting curve node instruments to derivatives
   * @param definitionFactory creates instrument definitions for curve node instruments
   * @param curveBuilder analytics object for building and calibrating the curve bundle
   */
  public MulticurveMarketDataBuilder(CurveSpecificationBuilder curveSpecBuilder,
                                     CurveNodeConverterFn curveNodeConverter,
                                     CurveNodeInstrumentDefinitionFactory definitionFactory,
                                     MulticurveDiscountBuildingRepository curveBuilder) {
    super(curveSpecBuilder, curveNodeConverter, definitionFactory);
    _curveBuilder = ArgumentChecker.notNull(curveBuilder, "curveBuilder");
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {

    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource,
                                                                  CyclePerturbations cyclePerturbations) {
    ImmutableMap.Builder<SingleValueRequirement, Result<?>> results = ImmutableMap.builder();

    for (SingleValueRequirement requirement : requirements) {
      MulticurveId marketDataId = (MulticurveId) requirement.getMarketDataId();
      CurveConstructionConfiguration curveConfig = marketDataId.resolveConfig();
      MulticurveBundle bundle =
          buildBundle(marketDataBundle, valuationTime, curveConfig, requirement, cyclePerturbations);
      results.put(requirement, Result.success(bundle));
    }
    return results.build();
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource,
      CyclePerturbations cyclePerturbations) {

    // TODO implement this
    return Collections.emptyMap();
  }

  @Override
  public Class<MulticurveId> getKeyType() {
    return MulticurveId.class;
  }

  @Override
  CurveConstructionConfiguration getCurveConfig(SingleValueRequirement requirement) {
    MulticurveId marketDataId = (MulticurveId) requirement.getMarketDataId();
    return marketDataId.resolveConfig();
  }

  @Override
  Set<MarketDataRequirement> getParentBundleRequirements(SingleValueRequirement requirement,
                                                         CurveConstructionConfiguration curveConfig) {

    ImmutableSet.Builder<MarketDataRequirement> parentBundleRequirements = ImmutableSet.builder();

    for (String parentBundleName : curveConfig.getExogenousConfigurations()) {
      MulticurveId curveBundleId = MulticurveId.of(parentBundleName);
      parentBundleRequirements.add(SingleValueRequirement.of(curveBundleId, requirement.getMarketDataTime()));
    }
    return parentBundleRequirements.build();
  }

  /**
   * Builds a multicurve bundle
   *
   * @param marketDataBundle the market data
   * @param valuationTime the valuation time for which the curve bundle should be built
   * @param bundleConfig the configuration for the multicurve bundle
   * @param cyclePerturbations the perturbations that should be applied to the market data for this calculation cycle
   * @return a multicurve bundle built from the configuration
   */
  @SuppressWarnings("unchecked")
  private MulticurveBundle buildBundle(MarketDataBundle marketDataBundle,
                                       ZonedDateTime valuationTime,
                                       CurveConstructionConfiguration bundleConfig,
                                       MarketDataRequirement bundleRequirement,
                                       CyclePerturbations cyclePerturbations) {

    Set<Currency> currencies = getCurrencies(bundleConfig, valuationTime);
    FxMatrixId fxMatrixKey = FxMatrixId.of(currencies);
    FXMatrix fxMatrix = marketDataBundle.get(fxMatrixKey, FXMatrix.class).getValue();
    MulticurveProviderDiscount parentBundle = createParentBundle(marketDataBundle, bundleConfig, fxMatrix);

    IntermediateResults intermediateResults = buildIntermediateResults(marketDataBundle,
                                                                       valuationTime,
                                                                       bundleConfig,
                                                                       bundleRequirement,
                                                                       cyclePerturbations);

    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> calibratedCurves =
        _curveBuilder.makeCurvesFromDerivatives(intermediateResults.getCurveBundles(),
                                                parentBundle,
                                                intermediateResults.getCurrenciesByCurveName(),
                                                intermediateResults.getIborIndexByCurveName(),
                                                intermediateResults.getOnIndexByCurveName(),
                                                DISCOUNTING_CALCULATOR,
                                                CURVE_SENSITIVITY_CALCULATOR);

    // TODO include curve node IDs - PLT-591
    return new MulticurveBundle(calibratedCurves.getFirst(), calibratedCurves.getSecond());
  }

  /**
   * Creates the parent curve bundle for a curve bundle.
   *
   * @param marketDataBundle the market data
   * @param bundleConfig the curve bundle configuration
   * @param fxMatrix the FX rates for the currencies used by the curve
   * @return the curve bundle's parent bundle
   */
  private static MulticurveProviderDiscount createParentBundle(MarketDataBundle marketDataBundle,
                                                               CurveConstructionConfiguration bundleConfig,
                                                               FXMatrix fxMatrix) {
    Set<MulticurveProviderDiscount> parentBundles = new HashSet<>();

    for (String parentBundleName : bundleConfig.getExogenousConfigurations()) {
      MulticurveId dependencyKey = MulticurveId.of(parentBundleName);
      MulticurveBundle bundle = marketDataBundle.get(dependencyKey, MulticurveBundle.class).getValue();
      parentBundles.add(bundle.getMulticurveProvider());
    }
    if (parentBundles.isEmpty()) {
      return new MulticurveProviderDiscount(fxMatrix);
    } else {
      MulticurveProviderDiscount mergedBundle = ProviderUtils.mergeDiscountingProviders(parentBundles);
      return ProviderUtils.mergeDiscountingProviders(mergedBundle, fxMatrix);
    }
  }
}

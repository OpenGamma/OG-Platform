/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.inflation.InflationDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveNodeInstrumentDefinitionFactory;
import com.opengamma.sesame.InflationProviderBundle;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;



/**
 * Market data builder for inflation curve bundles.
 */
public class InflationMulticurveMarketDataBuilder
    extends AbstractMulticurveMarketDataBuilder<InflationProviderBundle>
    implements MarketDataBuilder {

  private static final ParSpreadInflationMarketQuoteDiscountingCalculator DISCOUNTING_CALCULATOR =
      ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  /** The sensitivity calculator */
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator CURVE_SENSITIVITY_CALCULATOR =
      ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();


  private final InflationDiscountBuildingRepository _curveBuilder;

  /**
   * @param curveSpecBuilder for building curve specifications
   * @param curveNodeConverter for converting curve node instruments to derivatives
   * @param definitionFactory creates instrument definitions for curve node instruments
   * @param curveBuilder analytics object for building and calibrating the curve bundle
   */
  public InflationMulticurveMarketDataBuilder(CurveSpecificationBuilder curveSpecBuilder,
                                              CurveNodeConverterFn curveNodeConverter,
                                              CurveNodeInstrumentDefinitionFactory definitionFactory,
                                              InflationDiscountBuildingRepository curveBuilder) {
    super(curveSpecBuilder, curveNodeConverter, definitionFactory);
    _curveBuilder = ArgumentChecker.notNull(curveBuilder, "curveBuilder");
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(
      TimeSeriesRequirement requirement,
      Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {

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
      InflationMulticurveId marketDataId = (InflationMulticurveId) requirement.getMarketDataId();
      CurveConstructionConfiguration curveConfig = marketDataId.resolveConfig();
      InflationProviderBundle bundle =
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

    return Collections.emptyMap();
  }

  @Override
  public Class<InflationMulticurveId> getKeyType() {
    return InflationMulticurveId.class;
  }

  @Override
  CurveConstructionConfiguration getCurveConfig(SingleValueRequirement requirement) {
    InflationMulticurveId marketDataId = (InflationMulticurveId) requirement.getMarketDataId();
    return marketDataId.resolveConfig();
  }

  @Override
  Set<MarketDataRequirement> getParentBundleRequirements(SingleValueRequirement requirement,
                                                         CurveConstructionConfiguration curveConfig) {

    ImmutableSet.Builder<MarketDataRequirement> parentBundleRequirements = ImmutableSet.builder();

    for (String parentBundleName : curveConfig.getExogenousConfigurations()) {
      InflationMulticurveId curveBundleId = InflationMulticurveId.of(parentBundleName);
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
  private InflationProviderBundle buildBundle(MarketDataBundle marketDataBundle,
                                           ZonedDateTime valuationTime,
                                           CurveConstructionConfiguration bundleConfig,
                                           MarketDataRequirement bundleRequirement,
                                           CyclePerturbations cyclePerturbations) {


    //TODO get InstrumentDefinitions and implement override of getParameterGuessForCurves see InflationProviderDiscountingFunction
    //TODO ensure guesses are fed to single curve bundle and multicurve bundle
    //TODO deal with seasonality data see InflationProviderDiscountingFunction
    Set<Currency> currencies = getCurrencies(bundleConfig, valuationTime);
    FxMatrixId fxMatrixKey = FxMatrixId.of(currencies);
    FXMatrix fxMatrix = marketDataBundle.get(fxMatrixKey, FXMatrix.class).getValue();
    InflationProviderDiscount parentBundle = createParentBundle(marketDataBundle, bundleConfig, fxMatrix);

    IntermediateResults intermediateResults = buildIntermediateResults(marketDataBundle,
                                                                       valuationTime,
                                                                       bundleConfig,
                                                                       bundleRequirement,
                                                                       cyclePerturbations);

    Pair<InflationProviderDiscount, CurveBuildingBlockBundle> calibratedCurves =
        _curveBuilder.makeCurvesFromDerivatives(intermediateResults.getCurveBundles(),
                                                parentBundle,
                                                intermediateResults.getCurrenciesByCurveName(),
                                                intermediateResults.getIborIndexByCurveName(),
                                                intermediateResults.getOnIndexByCurveName(),
                                                createInflationMap(intermediateResults.getConfigTypes()),
                                                DISCOUNTING_CALCULATOR,
                                                CURVE_SENSITIVITY_CALCULATOR);

    return new InflationProviderBundle(calibratedCurves.getFirst(), calibratedCurves.getSecond());
  }

  /**
   * Returns a map of curve names to inflation details where the curve
   * configuration type is {@link InflationCurveTypeConfiguration}.
   *
   * @param configTypes the configuration types of the curves, keyed by curve name
   * @return a map of curve names to inflation details where the curve
   * configuration type is {@link InflationCurveTypeConfiguration}
   */
  private Multimap<String, IndexPrice> createInflationMap(Multimap<String, CurveTypeConfiguration> configTypes) {

    Multimap<String, IndexPrice> inflationMap = ArrayListMultimap.create();

    for (Map.Entry<String, CurveTypeConfiguration> entry : configTypes.entries()) {
        String curveName = entry.getKey();
        CurveTypeConfiguration configType = entry.getValue();

      if (configType instanceof InflationCurveTypeConfiguration) {
        InflationCurveTypeConfiguration inflationConfiguration = (InflationCurveTypeConfiguration) configType;
        ExternalId externalId = inflationConfiguration.getPriceIndex();
        PriceIndex index  = SecurityLink.resolvable(externalId, PriceIndex.class).resolve();
        IndexPrice indexPrice = new IndexPrice(index.getName(), Currency.of(inflationConfiguration.getReference()));
        inflationMap.put(curveName, indexPrice);
      } else {
        new OpenGammaRuntimeException("Cannot handle " + configType.getClass().getName());
      }
    }

    return inflationMap;
  }

  /**
   * Creates the parent curve bundle for a curve bundle.
   * TODO this doesn't work yet, is it supported anywhere? ProviderUtils doesn't have a method for merging them
   *
   * @param marketDataBundle the market data
   * @param bundleConfig the curve bundle configuration
   * @param fxMatrix the FX rates for the currencies used by the curve
   * @return the curve bundle's parent bundle
   */
  private static InflationProviderDiscount createParentBundle(MarketDataBundle marketDataBundle,
                                                           CurveConstructionConfiguration bundleConfig,
                                                           FXMatrix fxMatrix) {
    return new InflationProviderDiscount(fxMatrix);
  }
}

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveNodeInstrumentDefinitionFactory;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Market data builder for issuer curve bundles.
 */
public class IssuerMulticurveMarketDataBuilder
    extends AbstractMulticurveMarketDataBuilder<IssuerProviderBundle>
    implements MarketDataBuilder {

  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator DISCOUNTING_CALCULATOR =
      ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();

  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator CURVE_SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();
  private final IssuerDiscountBuildingRepository _curveBuilder;

  /**
   * @param curveSpecBuilder for building curve specifications
   * @param curveNodeConverter for converting curve node instruments to derivatives
   * @param definitionFactory creates instrument definitions for curve node instruments
   * @param curveBuilder analytics object for building and calibrating the curve bundle
   */
  public IssuerMulticurveMarketDataBuilder(CurveSpecificationBuilder curveSpecBuilder,
                                           CurveNodeConverterFn curveNodeConverter,
                                           CurveNodeInstrumentDefinitionFactory definitionFactory,
                                           IssuerDiscountBuildingRepository curveBuilder) {
    super(curveSpecBuilder, curveNodeConverter, definitionFactory);
    _curveBuilder = ArgumentChecker.notNull(curveBuilder, "curveBuilder");
  }

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Set<MarketDataId<?>> suppliedData) {
    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @Override
  public Map<SingleValueRequirement, Result<?>> buildSingleValues(MarketDataBundle marketDataBundle,
                                                                  ZonedDateTime valuationTime,
                                                                  Set<SingleValueRequirement> requirements,
                                                                  MarketDataSource marketDataSource) {
    ImmutableMap.Builder<SingleValueRequirement, Result<?>> results = ImmutableMap.builder();

    for (SingleValueRequirement requirement : requirements) {
      IssuerMulticurveId marketDataId = (IssuerMulticurveId) requirement.getMarketDataId();
      CurveConstructionConfiguration curveConfig = marketDataId.getConfig();
      IssuerProviderBundle bundle = buildBundle(marketDataBundle, valuationTime, curveConfig, requirement);
      results.put(requirement, Result.success(bundle));
    }
    return results.build();
  }

  @Override
  public Map<TimeSeriesRequirement, Result<DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource) {

    // TODO implement this
    return Collections.emptyMap();
  }

  @Override
  public Class<IssuerMulticurveId> getKeyType() {
    return IssuerMulticurveId.class;
  }

  @Override
  CurveConstructionConfiguration getCurveConfig(SingleValueRequirement requirement) {
    IssuerMulticurveId marketDataId = (IssuerMulticurveId) requirement.getMarketDataId();
    return marketDataId.getConfig();
  }

  @Override
  Set<MarketDataRequirement> getParentBundleRequirements(SingleValueRequirement requirement,
                                                         CurveConstructionConfiguration curveConfig) {

    ImmutableSet.Builder<MarketDataRequirement> parentBundleRequirements = ImmutableSet.builder();

    for (String parentBundleName : curveConfig.getExogenousConfigurations()) {
      IssuerMulticurveId curveBundleId = IssuerMulticurveId.of(parentBundleName);
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
   * @return a multicurve bundle built from the configuration
   */
  @SuppressWarnings("unchecked")
  private IssuerProviderBundle buildBundle(MarketDataBundle marketDataBundle,
                                           ZonedDateTime valuationTime,
                                           CurveConstructionConfiguration bundleConfig,
                                           MarketDataRequirement bundleRequirement) {

    Set<Currency> currencies = getCurrencies(bundleConfig, valuationTime);
    FxMatrixId fxMatrixKey = FxMatrixId.of(currencies);
    FXMatrix fxMatrix = marketDataBundle.get(fxMatrixKey, FXMatrix.class).getValue();
    IssuerProviderDiscount parentBundle = createParentBundle(marketDataBundle, bundleConfig, fxMatrix);

    IntermediateResults intermediateResults = buildIntermediateResults(marketDataBundle,
                                                                       valuationTime,
                                                                       bundleConfig,
                                                                       bundleRequirement);

    Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> calibratedCurves =
        _curveBuilder.makeCurvesFromDerivatives(intermediateResults.getCurveBundles(),
                                                parentBundle,
                                                intermediateResults.getCurrenciesByCurveName(),
                                                intermediateResults.getIborIndexByCurveName(),
                                                intermediateResults.getOnIndexByCurveName(),
                                                createIssuerMap(intermediateResults.getConfigTypes()),
                                                DISCOUNTING_CALCULATOR,
                                                CURVE_SENSITIVITY_CALCULATOR);

    return new IssuerProviderBundle(calibratedCurves.getFirst(), calibratedCurves.getSecond());
  }

  /**
   * Returns a map of curve names to issuer details where the curve
   * configuration type is {@link IssuerCurveTypeConfiguration}.
   *
   * @param configTypes the configuration types of the curves, keyed by curve name
   * @return a map of curve names to issuer details where the curve
   * configuration type is {@link IssuerCurveTypeConfiguration}
   */
  private LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> createIssuerMap(
      Multimap<String, CurveTypeConfiguration> configTypes) {

    LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> results = LinkedListMultimap.create();

    for (Map.Entry<String, CurveTypeConfiguration> entry : configTypes.entries()) {
      String curveName = entry.getKey();
      CurveTypeConfiguration configType = entry.getValue();

      if (configType instanceof IssuerCurveTypeConfiguration) {
        IssuerCurveTypeConfiguration issuerType = (IssuerCurveTypeConfiguration) configType;
        results.put(curveName, Pairs.<Object, LegalEntityFilter<LegalEntity>>of(issuerType.getKeys(), issuerType.getFilters()));
      }
    }
    return results;
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
  private static IssuerProviderDiscount createParentBundle(MarketDataBundle marketDataBundle,
                                                           CurveConstructionConfiguration bundleConfig,
                                                           FXMatrix fxMatrix) {
    return new IssuerProviderDiscount(fxMatrix);
  }
}

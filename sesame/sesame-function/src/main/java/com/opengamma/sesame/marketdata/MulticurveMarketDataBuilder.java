/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.sesame.CurveNodeInstrumentDefinitionFactory;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.TenorCurveNodeId;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.Tenor;
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
      Result<MulticurveBundle> bundleResult =
          buildBundle(marketDataBundle, valuationTime, curveConfig, requirement, cyclePerturbations);
      results.put(requirement, bundleResult);
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
  private Result<MulticurveBundle> buildBundle(
      MarketDataBundle marketDataBundle,
      ZonedDateTime valuationTime,
      CurveConstructionConfiguration bundleConfig,
      MarketDataRequirement bundleRequirement,
      CyclePerturbations cyclePerturbations) {
    Map<String, List<? extends CurveNodeId>> nodeIds = nodeIds(bundleConfig);
    Set<Currency> currencies = getCurrencies(bundleConfig, valuationTime);
    FxMatrixId fxMatrixKey = FxMatrixId.of(currencies);
    Result<FXMatrix> fxMatrixResult = marketDataBundle.get(fxMatrixKey, FXMatrix.class);

    if (!fxMatrixResult.isSuccess()) {
      return Result.failure(fxMatrixResult);
    }
    FXMatrix fxMatrix = fxMatrixResult.getValue();
    MulticurveProviderDiscount parentBundle = createParentBundle(marketDataBundle, bundleConfig, fxMatrix);

    IntermediateResults intermediateResults =
        buildIntermediateResults(
            marketDataBundle,
            valuationTime,
            bundleConfig,
            bundleRequirement,
            cyclePerturbations);

    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> calibratedCurves =
        _curveBuilder.makeCurvesFromDerivatives(
            intermediateResults.getCurveBundles(),
            parentBundle,
            intermediateResults.getCurrenciesByCurveName(),
            intermediateResults.getIborIndexByCurveName(),
            intermediateResults.getOnIndexByCurveName(),
            DISCOUNTING_CALCULATOR,
            CURVE_SENSITIVITY_CALCULATOR);

    return Result.success(new MulticurveBundle(calibratedCurves.getFirst(), nodeIds, calibratedCurves.getSecond()));
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
  
  /**
   * Builds a map of curve name to a list of curve node IDs which identify the nodes by tenor or futures expiry.
   */
  private static Map<String, List<? extends CurveNodeId>> nodeIds(CurveConstructionConfiguration multicurveConfig) {
    ImmutableMap.Builder<String, List<? extends CurveNodeId>> nodeMapBuilder = ImmutableMap.builder();

    for (CurveGroupConfiguration groupConfig : multicurveConfig.getCurveGroups()) {
      Set<AbstractCurveDefinition> curveDefinitions = groupConfig.resolveTypesForCurves().keySet();

      for (AbstractCurveDefinition abstractCurveDefinition : curveDefinitions) {
        if (abstractCurveDefinition instanceof CurveDefinition) {
          CurveDefinition curveDefinition = (CurveDefinition) abstractCurveDefinition;
          String curveName = curveDefinition.getName();
          SortedSet<CurveNode> nodes = curveDefinition.getNodes();
          ImmutableList.Builder<CurveNodeId> nodeListBuilder = ImmutableList.builder();

          for (CurveNode node : nodes) {
            if (node instanceof RateFutureNode) {
              // TODO create FuturesExpiryCurveNodeId and add to nodeListBuilder
              // RateFutureNode futureNode = (RateFutureNode) node;
              throw new OpenGammaRuntimeException("Future nodes are not supported");
            } else {
              Tenor tenor = node.getResolvedMaturity();
              TenorCurveNodeId nodeId = TenorCurveNodeId.of(tenor);
              
              nodeListBuilder.add(nodeId);
            }
          }
          nodeMapBuilder.put(curveName, nodeListBuilder.build());
        }
      }
    }
    return nodeMapBuilder.build();
  }
}

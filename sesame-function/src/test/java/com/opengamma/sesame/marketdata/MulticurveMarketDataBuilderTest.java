/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurrencyPairsFn;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveSpecificationFn;
import com.opengamma.sesame.CurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultCurrencyPairsFn;
import com.opengamma.sesame.DefaultCurveDefinitionFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveSpecificationFn;
import com.opengamma.sesame.DefaultCurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DefaultFXMatrixFn;
import com.opengamma.sesame.DiscountingMulticurveBundleFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FXMatrixFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CurveNameMulticurveFilter;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.sesame.marketdata.scenarios.MulticurveInputParallelShift;
import com.opengamma.sesame.marketdata.scenarios.SinglePerturbationMapping;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

@Test(groups = TestGroup.UNIT)
public class MulticurveMarketDataBuilderTest {

  private static MarketDataBuilder curveBuilder() {
    FunctionModelConfig curveBuilderConfig =
        config(
            arguments(
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    MulticurveDiscountBuildingRepository.class,
                    argument("toleranceAbs", 1e-9),
                    argument("toleranceRel", 1e-9),
                    argument("stepMaximum", 1000)),
                function(
                    ConfigDBCurveSpecificationBuilder.class,
                    argument("versionCorrection", VersionCorrection.LATEST))),
            implementations(
                CurveSpecificationBuilder.class, ConfigDBCurveSpecificationBuilder.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class));

    Map<Class<?>, Object> components = InterestRateMockSources.generateBaseComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
    ComponentMap componentMap = ComponentMap.of(components);
    return FunctionModel.build(MulticurveMarketDataBuilder.class, curveBuilderConfig, componentMap);
  }

  private static MarketDataBundle createMarketDataBundle() {
    MarketDataEnvironmentBuilder builder = InterestRateMockSources.createMarketDataEnvironment().toBuilder();
    RawId liborId = RawId.of(InterestRateMockSources.LIBOR_INDEX_ID.toBundle());
    builder.add(liborId, InterestRateMockSources.FLAT_TIME_SERIES);
    return new MapMarketDataBundle(builder.build());
  }

  /**
   * Compares a curve built by {@link MulticurveMarketDataBuilder} to one built by
   * {@link DefaultDiscountingMulticurveBundleResolverFn}
   */
  public void buildSingleCurveBundle() {
    MarketDataBuilder curveBuilder = curveBuilder();
    CyclePerturbations cyclePerturbations = new CyclePerturbations(ImmutableSet.<MarketDataRequirement>of(),
                                                                   ImmutableList.<SinglePerturbationMapping>of());

    MarketDataEnvironment baseMarketData = InterestRateMockSources.createMarketDataEnvironment();
    FXMatrix fxMatrix = new FXMatrix();
    MarketDataEnvironment marketData = baseMarketData.toBuilder().add(FxMatrixId.of(Currency.USD), fxMatrix).build();
    SingleValueRequirement curveReq = SingleValueRequirement.of(MulticurveId.of("USD_ON-OIS_LIBOR3M-FRAIRS_1U"));
    Map<SingleValueRequirement, Result<?>> results =
        curveBuilder.buildSingleValues(marketData.toBundle(),
                                       marketData.getValuationTime(),
                                       ImmutableSet.of(curveReq),
                                       mock(MarketDataSource.class),
                                       cyclePerturbations);

    MulticurveBundle curveBundle1 = (MulticurveBundle) results.get(curveReq).getValue();

    FunctionModelConfig config =
        config(
            arguments(
                function(
                    RootFinderConfiguration.class,
                    argument("rootFinderAbsoluteTolerance", 1e-9),
                    argument("rootFinderRelativeTolerance", 1e-9),
                    argument("rootFinderMaxIterations", 1000)),
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    DefaultDiscountingMulticurveBundleFn.class,
                    argument("impliedCurveNames", StringSet.of()))),
            implementations(
                CurrencyPairsFn.class, DefaultCurrencyPairsFn.class,
                CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
                FXMatrixFn.class, DefaultFXMatrixFn.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                DiscountingMulticurveBundleFn.class, DefaultDiscountingMulticurveBundleFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
                CurveConstructionConfigurationSource.class, ConfigDBCurveConstructionConfigurationSource.class,
                MarketDataFn.class, DefaultMarketDataFn.class));

    Map<Class<?>, Object> components = InterestRateMockSources.generateBaseComponents();
    DefaultDiscountingMulticurveBundleResolverFn fn =
        FunctionModel.build(DefaultDiscountingMulticurveBundleResolverFn.class, config, ComponentMap.of(components));
    CurveConstructionConfiguration curveConfig =
        ConfigLink.resolvable("USD_ON-OIS_LIBOR3M-FRAIRS_1U", CurveConstructionConfiguration.class).resolve();
    ZonedDateTime valuationTime = DateUtils.getUTCDate(2014, 1, 22);
    Environment env = new SimpleEnvironment(valuationTime, createMarketDataBundle());
    Result<MulticurveBundle> result = fn.generateBundle(env, curveConfig);
    MulticurveBundle curveBundle2 = result.getValue();

    assertEquals(curveBundle1.getCurveBuildingBlockBundle(), curveBundle2.getCurveBuildingBlockBundle());
    MulticurveProviderDiscount multicurveProvider1 = curveBundle1.getMulticurveProvider();
    MulticurveProviderDiscount multicurveProvider2 = curveBundle2.getMulticurveProvider();
    assertEquals(multicurveProvider1.getDiscountingCurves(), multicurveProvider2.getDiscountingCurves());
    assertEquals(multicurveProvider1.getForwardIborCurves(), multicurveProvider2.getForwardIborCurves());
    assertEquals(multicurveProvider1.getForwardONCurves(), multicurveProvider2.getForwardONCurves());
    assertEquals(multicurveProvider1.getFxRates(), multicurveProvider2.getFxRates());
    assertEquals(multicurveProvider1.getAllCurveNames(), multicurveProvider2.getAllCurveNames());
  }

  public void parallelShiftInputs() {
    MarketDataBuilder curveBuilder = curveBuilder();
    CyclePerturbations emptyPerturbations = new CyclePerturbations(ImmutableSet.<MarketDataRequirement>of(),
                                                                   ImmutableList.<SinglePerturbationMapping>of());

    MarketDataEnvironment baseMarketData = InterestRateMockSources.createMarketDataEnvironment();
    FXMatrix fxMatrix = new FXMatrix();
    MarketDataEnvironment marketData = baseMarketData.toBuilder().add(FxMatrixId.of(Currency.USD), fxMatrix).build();
    SingleValueRequirement curveReq = SingleValueRequirement.of(MulticurveId.of("USD_ON-OIS_LIBOR3M-FRAIRS_1U"));
    Map<SingleValueRequirement, Result<?>> unshiftedResults =
        curveBuilder.buildSingleValues(marketData.toBundle(),
                                       marketData.getValuationTime(),
                                       ImmutableSet.of(curveReq),
                                       new EmptyMarketDataFactory.DataSource(),
                                       emptyPerturbations);
    // discounting: USD-ON-OIS, forwardON: USD-ON-OIS, forwardIbor: USD-LIBOR3M-FRAIRS
    String curveName = "USD-ON-OIS";
    double shiftAmount = 0.01;

    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(new CurveNameMulticurveFilter(curveName))
            .perturbation(MulticurveInputParallelShift.absolute(shiftAmount))
            .build();

    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    ImmutableSet<SingleValueRequirement> requirements = ImmutableSet.of(curveReq);
    CyclePerturbations shiftPerturbations = new CyclePerturbations(requirements, mappings);
    Map<SingleValueRequirement, Result<?>> shiftedResults =
        curveBuilder.buildSingleValues(marketData.toBundle(),
                                       marketData.getValuationTime(),
                                       ImmutableSet.of(curveReq),
                                       new EmptyMarketDataFactory.DataSource(),
                                       shiftPerturbations);

    Result<?> unshiftedResult = unshiftedResults.get(curveReq);
    assertSuccess(unshiftedResult);
    MulticurveBundle unshiftedBundle = (MulticurveBundle) unshiftedResult.getValue();
    YieldAndDiscountCurve unshiftedDiscountCurve = unshiftedBundle.getMulticurveProvider().getCurve(curveName);

    Result<?> shiftedResult = shiftedResults.get(curveReq);
    assertSuccess(shiftedResult);
    MulticurveBundle shiftedBundle = (MulticurveBundle) shiftedResult.getValue();
    YieldAndDiscountCurve shiftedDiscountCurve = shiftedBundle.getMulticurveProvider().getCurve(curveName);

    double unshiftedRate = unshiftedDiscountCurve.getForwardRate(2);
    double shiftedRate = shiftedDiscountCurve.getForwardRate(2);
    // TODO confirm this is close enough
    assertEquals(unshiftedRate + shiftAmount, shiftedRate, 1e-4);
  }
}

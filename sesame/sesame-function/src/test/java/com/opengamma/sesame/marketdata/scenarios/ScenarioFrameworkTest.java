/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.LinkedHashMap;
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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.analytics.curve.ConfigDBCurveSpecificationBuilder;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.FxMatrixId;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.MulticurveMarketDataBuilder;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

@Test(groups = TestGroup.UNIT)
public class ScenarioFrameworkTest {

  /**
   * Tests applying a parallel shift to a calibrated curve built by the engine
   */
  public void curveOutputsParallelShift() {
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

    MulticurveMarketDataBuilder builder =
        FunctionModel.build(
            MulticurveMarketDataBuilder.class,
            curveBuilderConfig,
            componentMap);

    MarketDataEnvironment baseMarketData = InterestRateMockSources.createMarketDataEnvironment();
    FXMatrix fxMatrix = new FXMatrix();
    MarketDataEnvironment suppliedData = baseMarketData.toBuilder().add(FxMatrixId.of(Currency.USD), fxMatrix).build();
    SingleValueRequirement curveReq = SingleValueRequirement.of(MulticurveId.of("USD_ON-OIS_LIBOR3M-FRAIRS_1U"));
    String curveName = "USD-ON-OIS";
    double shiftAmount = 0.01;

    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(new CurveNameMulticurveFilter(curveName))
            .perturbation(MulticurveOutputParallelShift.absolute(shiftAmount))
            .build();

    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    SingleScenarioDefinition scenario = SingleScenarioDefinition.of("scenarioName", mappings);
    MarketDataEnvironmentFactory environmentFactory =
        new MarketDataEnvironmentFactory(new EmptyMarketDataFactory(), builder);
    ZonedDateTime valuationTime = ZonedDateTime.now();

    MarketDataEnvironment perturbedData =
        environmentFactory.build(
            suppliedData,
            ImmutableSet.<MarketDataRequirement>of(curveReq),
            scenario,
            EmptyMarketDataSpec.INSTANCE,
            valuationTime);

    MarketDataEnvironment unperturbedData =
        environmentFactory.build(
            suppliedData,
            ImmutableSet.<MarketDataRequirement>of(curveReq),
            SingleScenarioDefinition.base(),
            EmptyMarketDataSpec.INSTANCE,
            valuationTime);

    MulticurveBundle unperturbedBundle = (MulticurveBundle) unperturbedData.getData().get(curveReq);
    YieldAndDiscountCurve unperturbedCurve = unperturbedBundle.getMulticurveProvider().getCurve(curveName);

    MulticurveBundle perturbedBundle = (MulticurveBundle) perturbedData.getData().get(curveReq);
    YieldAndDiscountCurve perturbedCurve = perturbedBundle.getMulticurveProvider().getCurve(curveName);

    assertEquals(unperturbedCurve.getInterestRate(0.1) + shiftAmount, perturbedCurve.getInterestRate(0.1));
    assertEquals(unperturbedCurve.getInterestRate(1d) + shiftAmount, perturbedCurve.getInterestRate(1d));
  }

  /**
   * Tests applying a parallel shift to a calibrated curve supplied by the user
   */
  public void suppliedCurveOutputsParallelShift() {
    MulticurveId multicurveId = MulticurveId.of("curveBundle");
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    ConstantDoublesCurve curve = new ConstantDoublesCurve(1.5, "curveName");
    YieldCurve yieldCurve = YieldCurve.from(curve);
    multicurve.setCurve(Currency.USD, yieldCurve);
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> emptyMap = new LinkedHashMap<>();
    MulticurveBundle bundle = new MulticurveBundle(multicurve, new CurveBuildingBlockBundle(emptyMap));
    ZonedDateTime valuationTime = ZonedDateTime.now();
    MarketDataEnvironment suppliedData =
        new MarketDataEnvironmentBuilder()
            .add(multicurveId, bundle)
            .valuationTime(valuationTime)
            .build();

    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(new EmptyMarketDataFactory());

    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(new CurveNameMulticurveFilter("curveName"))
            .perturbation(MulticurveOutputParallelShift.absolute(0.1))
            .build();
    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    SingleScenarioDefinition scenario = SingleScenarioDefinition.of("scenarioName", mappings);

    MarketDataEnvironment perturbedMarketData =
        environmentFactory.build(
            suppliedData,
            ImmutableSet.<MarketDataRequirement>of(),
            scenario,
            EmptyMarketDataSpec.INSTANCE,
            valuationTime);

    SingleValueRequirement bundleRequirement = SingleValueRequirement.of(multicurveId);
    MulticurveBundle perturbedBundle = (MulticurveBundle) perturbedMarketData.getData().get(bundleRequirement);
    MulticurveProviderDiscount perturbedMulticurve = perturbedBundle.getMulticurveProvider();
    YieldAndDiscountCurve perturbedCurve = perturbedMulticurve.getDiscountingCurves().get(Currency.USD);
    assertEquals(1.6, perturbedCurve.getInterestRate(0d));
    assertEquals(1.6, perturbedCurve.getInterestRate(1d));
  }

  /**
   * Tests CyclePerturbations.applyPerturbations applying a parallel shift to a calibrated curve
   */
  public void cyclePerturbationsApplyPerturbations() {
    SinglePerturbationMapping mapping =
        SinglePerturbationMapping.builder()
            .filter(new CurveNameMulticurveFilter("curveName"))
            .perturbation(MulticurveOutputParallelShift.absolute(0.1))
            .build();

    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    ConstantDoublesCurve curve = new ConstantDoublesCurve(1.5, "curveName");
    YieldCurve yieldCurve = YieldCurve.from(curve);
    multicurve.setCurve(Currency.USD, yieldCurve);
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> emptyMap = new LinkedHashMap<>();
    MulticurveBundle bundle = new MulticurveBundle(multicurve, new CurveBuildingBlockBundle(emptyMap));
    MulticurveId multicurveId = MulticurveId.of("curveBundle");
    MarketDataEnvironment marketData =
        new MarketDataEnvironmentBuilder()
            .add(multicurveId, bundle)
            .valuationTime(ZonedDateTime.now())
            .build();

    SingleValueRequirement bundleRequirement = SingleValueRequirement.of(multicurveId);
    List<SinglePerturbationMapping> mappings = ImmutableList.of(mapping);
    SingleScenarioDefinition scenario = SingleScenarioDefinition.of("scenarioName", mappings);
    ImmutableSet<SingleValueRequirement> requirements = ImmutableSet.of(bundleRequirement);
    CyclePerturbations cyclePerturbations = new CyclePerturbations(requirements, scenario);

    MarketDataEnvironment perturbedData = cyclePerturbations.apply(marketData);
    MulticurveBundle perturbedBundle = (MulticurveBundle) perturbedData.getData().get(bundleRequirement);
    assertNotNull(perturbedBundle);
    MulticurveProviderDiscount perturbedMulticurve = perturbedBundle.getMulticurveProvider();
    YieldAndDiscountCurve perturbedCurve = perturbedMulticurve.getDiscountingCurves().get(Currency.USD);
    assertEquals(1.6, perturbedCurve.getInterestRate(0d));
    assertEquals(1.6, perturbedCurve.getInterestRate(1d));
  }
}

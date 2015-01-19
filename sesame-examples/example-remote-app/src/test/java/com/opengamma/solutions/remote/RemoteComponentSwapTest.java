/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.remote;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesResolver;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurrencyPairsFn;
import com.opengamma.sesame.CurveDefinitionCurveLabellingFn;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DefaultCurrencyPairsFn;
import com.opengamma.sesame.DefaultCurveDefinitionFn;
import com.opengamma.sesame.DefaultFXMatrixFn;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FXMatrixFn;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.irs.DefaultInterestRateSwapConverterFn;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculator;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculator;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapConverterFn;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.SnapshotMarketDataFactory;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.solutions.util.SwapViewUtils;
import com.opengamma.util.function.Function;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests that runs locally with remote components
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION, enabled = false)
public class RemoteComponentSwapTest {

  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private InterestRateSwapFn _swapFunction;
  private FunctionRunner _functionRunner;
  private static String _url;

  @BeforeClass
  public void setUp() {
    _url = Objects.firstNonNull(System.getProperty("server.url"), RemoteTestUtils.LOCALHOST);

    URI htsResolverUri = URI.create(_url + "components/HistoricalTimeSeriesResolver/shared");
    HistoricalTimeSeriesResolver htsResolver = new RemoteHistoricalTimeSeriesResolver(htsResolverUri);
    Map<Class<?>, Object> comps = ImmutableMap.<Class<?>, Object>of(HistoricalTimeSeriesResolver.class, htsResolver);

    ComponentMap componentMap = ComponentMap.loadComponents(_url).with(comps);
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext =
        ServiceContext.of(componentMap.getComponents()).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);

    _exposureConfig = ConfigLink.resolvable(RemoteTestUtils.USD_GBP_FF_EXPOSURE, ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable(RemoteTestUtils.CURRENCY_MATRIX, CurrencyMatrix.class);
    MarketDataSnapshotSource snapshotSource = componentMap.getComponent(MarketDataSnapshotSource.class);
    SnapshotMarketDataFactory marketDataFactory = new SnapshotMarketDataFactory(snapshotSource);
    List<MarketDataBuilder> builders = MarketDataBuilders.standard(componentMap, "BLOOMBERG", _currencyMatrixLink);
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(marketDataFactory, builders);

    _functionRunner = new FunctionRunner(environmentFactory);
    _swapFunction = FunctionModel.build(InterestRateSwapFn.class, createConfig(), componentMap);
  }

  private FunctionModelConfig createConfig() {
    return
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", _exposureConfig)),
                function(
                    DefaultHistoricalMarketDataFn.class,
                    argument("currencyMatrix", _currencyMatrixLink)),
                function(
                    DefaultMarketDataFn.class,
                    argument("currencyMatrix", _currencyMatrixLink))),
            implementations(
                InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                CurrencyPairsFn.class, DefaultCurrencyPairsFn.class,
                InstrumentExposuresProvider.class, ConfigDBInstrumentExposuresProvider.class,
                InterestRateSwapCalculatorFactory.class, DiscountingInterestRateSwapCalculatorFactory.class,
                DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class,
                InterestRateSwapCalculator.class, DiscountingInterestRateSwapCalculator.class,
                FXMatrixFn.class, DefaultFXMatrixFn.class,
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                InterestRateSwapConverterFn.class, DefaultInterestRateSwapConverterFn.class,
                CurveLabellingFn.class, CurveDefinitionCurveLabellingFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                FixingsFn.class, DefaultFixingsFn.class,
                MarketDataFn.class, DefaultMarketDataFn.class));
  }

  @Test(enabled = false)
  public void testSwapPV() {
    final InterestRateSwapSecurity irs = (InterestRateSwapSecurity) SwapViewUtils.VANILLA_INPUTS.get(0);

    RemoteServer server = RemoteServer.create(_url);
    MarketDataSnapshotSource snapshotSource = server.getMarketDataSnapshotSource();
    ManageableMarketDataSnapshot snapshot = snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
                                                                     RemoteTestUtils.USD_GBP_SNAPSHOT,
                                                                     VersionCorrection.LATEST);

    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());

    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .marketDataSpecification(marketDataSpec)
            .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
            .build();

    Result result = _functionRunner.runFunction(calculationArguments, new Function<Environment, Result>() {
      @Override
      public Result apply(Environment env) {
        return _swapFunction.calculatePV(env, irs);
      }
    });
    assertSuccess(result);
  }
}

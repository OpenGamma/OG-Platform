/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesResolver;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.ConfigDbMarketExposureSelectorFn;
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
import com.opengamma.sesame.DefaultHistoricalTimeSeriesFn;
import com.opengamma.sesame.DiscountingMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ExposureFunctionsDiscountingMulticurveCombinerFn;
import com.opengamma.sesame.FXMatrixFn;
import com.opengamma.sesame.HistoricalTimeSeriesFn;
import com.opengamma.sesame.MarketExposureSelectorFn;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculator;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculator;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.SnapshotMarketDataSource;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests that runs locally with remote components
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class RemoteComponentSwapTest {

  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private InterestRateSwapFn _swapFunction;
  private Environment _environment = null;

  @BeforeClass
  public void setUp() {

    String property = System.getProperty("server.url");
    String url = property == null ? "http://localhost:8080/jax" : property;

    URI htsResolverUri = URI.create(url + "components/HistoricalTimeSeriesResolver/shared");
    HistoricalTimeSeriesResolver htsResolver = new RemoteHistoricalTimeSeriesResolver(htsResolverUri);
    Map<Class<?>, Object> comps = ImmutableMap.<Class<?>, Object>of(HistoricalTimeSeriesResolver.class, htsResolver);

    ComponentMap componentMap = ComponentMap.loadComponents(url).with(comps);
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext =
        ServiceContext.of(componentMap.getComponents()).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);

    MarketDataSnapshotSource component = componentMap.getComponent(MarketDataSnapshotSource.class);
    SnapshotMarketDataSource marketDataSource = new SnapshotMarketDataSource(component, UniqueId.of("DbSnp", "1000"));

    _environment = new SimpleEnvironment(DateUtils.getUTCDate(2014, 1, 22), marketDataSource);
    _exposureConfig = ConfigLink.resolvable("USD CSA Exposure Functions", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);
    _swapFunction = FunctionModel.build(InterestRateSwapFn.class, createConfig(), componentMap);

  }

  private FunctionModelConfig createConfig() {
    return config(
        arguments(
            function(ConfigDbMarketExposureSelectorFn.class,
                     argument("exposureConfig", _exposureConfig)),
            function(
                RootFinderConfiguration.class,
                argument("rootFinderAbsoluteTolerance", 1e-10),
                argument("rootFinderRelativeTolerance", 1e-10),
                argument("rootFinderMaxIterations", 1000)),
            function(DefaultCurveNodeConverterFn.class,
                     argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
            function(DefaultHistoricalMarketDataFn.class,
                     argument("dataSource", "BLOOMBERG"),
                     argument("currencyMatrix", _currencyMatrixLink)),
            function(DefaultMarketDataFn.class,
                     argument("dataSource", "BLOOMBERG"),
                     argument("currencyMatrix", _currencyMatrixLink)),
            function(
                DefaultHistoricalTimeSeriesFn.class,
                argument("resolutionKey", "DEFAULT_TSS"),
                argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
            function(
                DefaultDiscountingMulticurveBundleFn.class,
                argument("impliedCurveNames", StringSet.of()))),
        implementations(
            InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
            CurrencyPairsFn.class, DefaultCurrencyPairsFn.class,
            InstrumentExposuresProvider.class, ConfigDBInstrumentExposuresProvider.class,
            InterestRateSwapCalculatorFactory.class, DiscountingInterestRateSwapCalculatorFactory.class,
            InterestRateSwapCalculator.class, DiscountingInterestRateSwapCalculator.class,
            CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
            CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
            FXMatrixFn.class, DefaultFXMatrixFn.class,
            DiscountingMulticurveCombinerFn.class, ExposureFunctionsDiscountingMulticurveCombinerFn.class,
            CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
            DiscountingMulticurveBundleFn.class, DefaultDiscountingMulticurveBundleFn.class,
            DiscountingMulticurveBundleResolverFn.class, DefaultDiscountingMulticurveBundleResolverFn.class,
            CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
            CurveConstructionConfigurationSource.class, ConfigDBCurveConstructionConfigurationSource.class,
            HistoricalTimeSeriesFn.class, DefaultHistoricalTimeSeriesFn.class,
            HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
            MarketExposureSelectorFn.class, ConfigDbMarketExposureSelectorFn.class,
            MarketDataFn.class, DefaultMarketDataFn.class
        ));
  }

  @Test(enabled = true)
  public void testSwapPV() {

    InterestRateSwapSecurity irs = (InterestRateSwapSecurity) RemoteViewSwapUtils.VANILLA_INPUTS.get(0);
    Result result = _swapFunction.calculatePV(_environment, irs);
    assertThat(result.isSuccess(), is(true));

  }

}

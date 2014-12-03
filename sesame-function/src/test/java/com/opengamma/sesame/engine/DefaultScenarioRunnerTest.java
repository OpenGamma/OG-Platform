/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.sesame.CurveSelectorFn;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DirectExecutorService;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.cache.NoOpCacheInvalidator;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.AvailableOutputsImpl;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.function.scenarios.curvedata.FunctionTestUtils;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.ScenarioDataBuilder;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.sources.BondMockSources;
import com.opengamma.sesame.trade.BondTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

@Test(groups = TestGroup.UNIT)
public class DefaultScenarioRunnerTest {

  private static final String BUNDLE1 = "bundle1";
  private static final String BUNDLE2 = "bundle2";
  private static final ViewConfig CONFIG =
      configureView(
          "view name",
          config(
              implementations(
                  CurveSelectorFn.class, TestSelectorFn.class,
                  DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class,
                  TestFn.class, TestImpl.class)),
          column("col1", "Foo"));

  @Test
  public void oneScenario() {
    ViewFactory viewFactory = createViewFactory();
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(new EmptyMarketDataFactory());
    DefaultEngine engine = new DefaultEngine(viewFactory, environmentFactory);
    DefaultScenarioRunner scenarioRunner = new DefaultScenarioRunner(engine);
    ScenarioDataBuilder builder = new ScenarioDataBuilder();
    ZonedDateTime valuationTime = ZonedDateTime.now();
    ScenarioMarketDataEnvironment marketDataEnvironment =
        builder.addMulticurve("base", BUNDLE1, createMulticurve(Currency.USD, 1))
               .addMulticurve("base", BUNDLE2, createMulticurve(Currency.EUR, 2))
               .valuationTime("base", valuationTime)
               .build();
    List<?> trades = ImmutableList.of(createTrade());
    ScenarioResults scenarioResults = scenarioRunner.runScenario(CONFIG, marketDataEnvironment, trades);
    Map<String, Results> resultsMap = scenarioResults.getResults();
    assertEquals(1, resultsMap.size());
    Results results = resultsMap.get("base");
    assertNotNull(results);
    assertEquals(1, results.getRows().size());
    assertEquals(1, results.get(0).getItems().size());
    assertEquals("col1", results.getColumnNames().get(0));
    assertEquals(Pairs.of(1.0, 2.0), results.get(0, 0).getResult().getValue());
  }

  @Test
  public void multipleScenarios() {
    ViewFactory viewFactory = createViewFactory();
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(new EmptyMarketDataFactory());
    DefaultEngine engine = new DefaultEngine(viewFactory, environmentFactory);
    DefaultScenarioRunner scenarioRunner = new DefaultScenarioRunner(engine);
    ScenarioDataBuilder builder = new ScenarioDataBuilder();
    ZonedDateTime valuationTime = ZonedDateTime.now();
    ScenarioMarketDataEnvironment marketDataEnvironment =
        builder.addMulticurve("base", BUNDLE1, createMulticurve(Currency.USD, 1))
               .addMulticurve("base", BUNDLE2, createMulticurve(Currency.EUR, 2))
               .valuationTime("base", valuationTime)
               .addMulticurve("s1", BUNDLE1, createMulticurve(Currency.USD, 3))
               .addMulticurve("s1", BUNDLE2, createMulticurve(Currency.EUR, 4))
               .valuationTime("s1", valuationTime)
               .addMulticurve("s2", BUNDLE1, createMulticurve(Currency.USD, 5))
               .addMulticurve("s2", BUNDLE2, createMulticurve(Currency.EUR, 6))
               .valuationTime("s2", valuationTime)
               .build();
    List<?> trades = ImmutableList.of(createTrade());
    ScenarioResults scenarioResults = scenarioRunner.runScenario(CONFIG, marketDataEnvironment, trades);
    Map<String, Results> resultsMap = scenarioResults.getResults();
    assertEquals(3, resultsMap.size());
    Results baseResults = resultsMap.get("base");
    Results s1Results = resultsMap.get("s1");
    Results s2Results = resultsMap.get("s2");

    assertNotNull(baseResults);
    assertNotNull(s1Results);
    assertNotNull(s2Results);

    assertEquals(Pairs.of(1.0, 2.0), baseResults.get(0, 0).getResult().getValue());
    assertEquals(Pairs.of(3.0, 4.0), s1Results.get(0, 0).getResult().getValue());
    assertEquals(Pairs.of(5.0, 6.0), s2Results.get(0, 0).getResult().getValue());
  }

  private ViewFactory createViewFactory() {
    AvailableOutputs availableOutputs = new AvailableOutputsImpl(BondTrade.class);
    availableOutputs.register(TestFn.class);
    return new ViewFactory(new DirectExecutorService(),
                           ComponentMap.EMPTY,
                           availableOutputs,
                           new AvailableImplementationsImpl(),
                           FunctionModelConfig.EMPTY,
                           FunctionService.DEFAULT_SERVICES,
                           FunctionTestUtils.createCacheBuilder(),
                           new NoOpCacheInvalidator(),
                           Optional.<MetricRegistry>absent());
  }

  private static BondTrade createTrade() {
    SimpleTrade trade = new SimpleTrade();
    trade.setQuantity(BigDecimal.ONE);
    trade.setTradeDate(LocalDate.of(2011, 3, 8));
    SimpleSecurityLink securityLink = new SimpleSecurityLink();
    securityLink.setTarget(BondMockSources.GOVERNMENT_BOND_SECURITY);
    trade.setSecurityLink(securityLink);
    return new BondTrade(trade);
  }

  private static MulticurveBundle createMulticurve(Currency currency, double curveValue) {
    ConstantDoublesCurve curve = new ConstantDoublesCurve(curveValue);
    YieldCurve yieldCurve = new YieldCurve(currency.getCode() + " discounting", curve);
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> linkedMap = new LinkedHashMap<>();
    MulticurveProviderDiscount emptyProvider = new MulticurveProviderDiscount();
    MulticurveProviderDiscount provider = emptyProvider.withDiscountFactor(currency, yieldCurve);
    return new MulticurveBundle(provider, new CurveBuildingBlockBundle(linkedMap));
  }

  public interface TestFn {

    @Output("Foo")
    Result<Pair<Double, Double>> foo(Environment env, BondTrade trade);
  }

  public static class TestImpl implements TestFn {

    private final DiscountingMulticurveCombinerFn _curveFn;

    public TestImpl(DiscountingMulticurveCombinerFn curveFn) {
      _curveFn = curveFn;
    }

    @Override
    public Result<Pair<Double, Double>> foo(Environment env, BondTrade trade) {
      Result<MulticurveBundle> result = _curveFn.getMulticurveBundle(env, trade);

      if (!result.isSuccess()) {
        return Result.failure(result);
      }
      MulticurveBundle multicurve = result.getValue();
      YieldAndDiscountCurve curve1 = multicurve.getMulticurveProvider().getCurve("USD discounting");
      YieldAndDiscountCurve curve2 = multicurve.getMulticurveProvider().getCurve("EUR discounting");

      if (curve1 != null && curve2 != null) {
        return Result.success(Pairs.of(curve1.getInterestRate(0.0), curve2.getInterestRate(0.0)));
      } else {
        return Result.failure(FailureStatus.MISSING_DATA, "curve1: {}, curve2: {}", curve1, curve2);
      }
    }
  }

  public static class TestSelectorFn implements CurveSelectorFn {

    @Override
    public Set<String> getMulticurveNames(Trade trade) {
      return ImmutableSet.of(BUNDLE1, BUNDLE2);
    }
  }
}

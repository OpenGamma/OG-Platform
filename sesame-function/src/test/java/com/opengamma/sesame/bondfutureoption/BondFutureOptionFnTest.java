/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfutureoption;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.LookupIssuerProviderFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.bondfuture.BondFutureTestData;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.BondFutureOptionTrade;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test for bond future options using the black calculator.
 * Validated against BondFuturesOptionPremiumBlackExpStrikeE2ETest
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionFnTest {

  /** Tolerances */
  private static final double TOLERANCE_GREEKS = 1.0E-6;
  private static final double TOLERANCE_PV = 1.0E-2;

  /** Expected values validated against BondFuturesOptionPremiumBlackExpStrikeE2ETest */
  public static final double EXPECTED_PV = -81090395.9457;
  public static final double EXPECTED_DELTA = -0.56391967;
  public static final double EXPECTED_GAMMA = 23.70913668;
  public static final double EXPECTED_VEGA = 0.2109653;
  public static final double EXPECTED_THETA = -0.02435502;
  public static final BondFutureOptionTrade BOND_FUTURE_OPTION_TRADE = BondFutureTestData.createBondFutureOptionTrade();

  private BondFutureOptionFn _function;
  private FunctionRunner _functionRunner;

  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(BondFutureTestData.VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();
  private static MarketDataEnvironment ENV = BondFutureTestData.createMarketDataEnvironment();

  @BeforeClass
  public void setUp() {

    FunctionModelConfig config =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(BondFutureTestData.createExposureFunction())))),
            implementations(
                BlackBondFuturesProviderFn.class, BlackExpStrikeBondFuturesProviderFn.class,
                BondFutureOptionFn.class, DefaultBondFutureOptionFn.class,
                BondFutureOptionCalculatorFactory.class, BondFutureOptionBlackCalculatorFactory.class,
                FixingsFn.class, DefaultFixingsFn.class,
                IssuerProviderFn.class, LookupIssuerProviderFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class
            ));

    ImmutableMap<Class<?>, Object> components = BondFutureTestData.generateBaseComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
    ComponentMap componentMap = ComponentMap.of(components);
    _function = FunctionModel.build(BondFutureOptionFn.class,
                                    config,
                                    componentMap);
    EmptyMarketDataFactory dataFactory = new EmptyMarketDataFactory();
    ConfigLink<CurrencyMatrix> currencyMatrixLink = ConfigLink.resolved(componentMap.getComponent(CurrencyMatrix.class));
    List<MarketDataBuilder> builders = MarketDataBuilders.standard(componentMap, "dataSource", currencyMatrixLink);
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(dataFactory, builders);
    _functionRunner = new FunctionRunner(environmentFactory);
  }

  @Test
  public void testPresentValue() {
    Result<MultipleCurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<MultipleCurrencyAmount>>() {
          @Override
          public Result<MultipleCurrencyAmount> apply(Environment env) {
            return _function.calculatePV(env, BOND_FUTURE_OPTION_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    MultipleCurrencyAmount mca = result.getValue();
    assertThat(mca.getAmount(Currency.JPY), is(closeTo(EXPECTED_PV, TOLERANCE_PV)));
  }

  @Test
  public void testDelta() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _function.calculateDelta(env, BOND_FUTURE_OPTION_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    Double delta = result.getValue();
    assertThat(delta, is(closeTo(EXPECTED_DELTA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testGamma() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _function.calculateGamma(env, BOND_FUTURE_OPTION_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    Double gamma = result.getValue();
    assertThat(gamma, is(closeTo(EXPECTED_GAMMA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testVega() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _function.calculateVega(env, BOND_FUTURE_OPTION_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    Double vega = result.getValue();
    assertThat(vega, is(closeTo(EXPECTED_VEGA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testTheta() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _function.calculateTheta(env, BOND_FUTURE_OPTION_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    Double theta = result.getValue();
    assertThat(theta, is(closeTo(EXPECTED_THETA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testPV01() {
    Result<MultipleCurrencyMulticurveSensitivity> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment,Result<MultipleCurrencyMulticurveSensitivity>>() {
          @Override
          public Result<MultipleCurrencyMulticurveSensitivity> apply(Environment env) {
            return _function.calculatePV01(env, BOND_FUTURE_OPTION_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
  }



}

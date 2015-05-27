/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfuture;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.LookupIssuerProviderFn;
import com.opengamma.sesame.MarketExposureSelector;
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
import com.opengamma.sesame.trade.BondFutureTrade;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Tests for bond future functions using the discounting calculator.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureFnTest {

  /** Tolerance */
  private static final double TOLERANCE_PV = 1.0E-2;

  /** Expected values validated against BondFuturesOptionPremiumBlackExpStrikeE2ETest */
  public static final double EXPECTED_PV = 1.4622286005634212E8;
  public static final double EXPECTED_PRICE = 1.46222860 ;


  public static final BondFutureTrade BOND_FUTURE_TRADE = BondFutureTestData.createBondFutureTrade();

  private BondFutureFn _function;
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
                BondFutureFn.class, DefaultBondFutureFn.class,
                BondFutureCalculatorFactory.class, BondFutureDiscountingCalculatorFactory.class,
                IssuerProviderFn.class, LookupIssuerProviderFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class));

    ImmutableMap<Class<?>, Object> components = BondFutureTestData.generateBaseComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
    ComponentMap componentMap = ComponentMap.of(components);
    _function = FunctionModel.build(BondFutureFn.class,
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
            return _function.calculatePV(env, BOND_FUTURE_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    MultipleCurrencyAmount mca = result.getValue();
    assertThat(mca.getAmount(Currency.JPY), is(closeTo(EXPECTED_PV, TOLERANCE_PV)));
  }

  @Test
  public void testPrice() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _function.calculateSecurityModelPrice(env, BOND_FUTURE_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
    Double price = result.getValue();
    assertThat(price, is(closeTo(EXPECTED_PRICE, TOLERANCE_PV)));
  }

  @Test
  public void testPV01() {
    Result<ReferenceAmount<Pair<String, Currency>>> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment,Result<ReferenceAmount<Pair<String,Currency>>>>() {
          @Override
          public Result<ReferenceAmount<Pair<String, Currency>>> apply(Environment env) {
            return _function.calculatePV01(env, BOND_FUTURE_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
  }

  @Test
  public void testBucketedPV01() {
    Result<MultipleCurrencyParameterSensitivity> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment,Result<MultipleCurrencyParameterSensitivity>>() {
          @Override
          public Result<MultipleCurrencyParameterSensitivity> apply(Environment env) {
            return _function.calculateBucketedPV01(env, BOND_FUTURE_TRADE);
          }
        });
    assertThat(result.isSuccess(), is(true));
  }

}

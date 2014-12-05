/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios.marketdata;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.security.Security;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.MapMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.SecurityId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

@SuppressWarnings("unchecked")
@Test(groups = TestGroup.UNIT)
public class MarketDataShockDecoratorTest {

  private static final String SCHEME = "scheme";
  private static final String VALUE1 = "value1";
  private static final String VALUE2 = "value2";
  // TODO these need to be securities to conform to the new MarketDataFn API
  private static final Security SEC1 = createSecurity(SCHEME, VALUE1);
  private static final Security SEC2 = createSecurity(SCHEME, VALUE2);
  private static final FunctionModelConfig CONFIG =
      config(implementations(Fn.class, Impl.class,
                             MarketDataFn.class, DefaultMarketDataFn.class),
             arguments(function(DefaultMarketDataFn.class, argument("currencyMatrix", new SimpleCurrencyMatrix()))));
  private static final MarketDataMatcher MATCHER1 = MarketDataMatcher.idEquals(SCHEME, VALUE1);
  private static final MarketDataMatcher MATCHER2 = MarketDataMatcher.idEquals(SCHEME, VALUE2);
  private static final FunctionModelConfig DECORATED_CONFIG = CONFIG.decoratedWith(MarketDataShockDecorator.class);
  private static final MarketDataBundle MARKET_DATA_BUNDLE =
      new MapMarketDataBundle(new MarketDataEnvironmentBuilder()
                                  .add(SecurityId.of(SEC1.getExternalIdBundle()), 1.0)
                                  .add(SecurityId.of(SEC2.getExternalIdBundle()), 2.0)
                                  .valuationTime(ZonedDateTime.now())
                                  .build());
  private static final Fn FN = FunctionModel.build(Fn.class, DECORATED_CONFIG);
  private static final double DELTA = 1e-8;

  private static Security createSecurity(String idScheme, String idValue) {
    CashFlowSecurity security = new CashFlowSecurity(Currency.GBP, ZonedDateTime.now(), 123.45);
    security.setExternalIdBundle(ExternalIdBundle.of(idScheme, idValue));
    return security;
  }

  /**
   * apply a single shock to one piece of market data but not another
   */
  @SuppressWarnings("unchecked")
  @Test
  public void singleShock() {
    MarketDataShock shock = MarketDataShock.relativeShift(0.5, MATCHER1);
    FilteredScenarioDefinition scenarioDef = new FilteredScenarioDefinition(shock);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), MARKET_DATA_BUNDLE, scenarioDef);

    assertEquals(1.5, FN.foo(env, SEC1).getValue(), DELTA);
    assertEquals(2d, FN.foo(env, SEC2).getValue(), DELTA);
  }

  /**
   * apply a shock to one piece of data and a different shock to another
   */
  @Test
  public void multipleSeparateShocks() {
    MarketDataShock relativeShift = MarketDataShock.relativeShift(0.5, MATCHER1);
    MarketDataShock absoluteShift = MarketDataShock.absoluteShift(0.1, MATCHER2);
    FilteredScenarioDefinition scenarioDef = new FilteredScenarioDefinition(absoluteShift, relativeShift);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), MARKET_DATA_BUNDLE, scenarioDef);

    assertEquals(1.5, FN.foo(env, SEC1).getValue(), DELTA);
    assertEquals(2.1, FN.foo(env, SEC2).getValue(), DELTA);
  }

  /**
   * apply two shocks to the same piece of data
   */
  @Test
  public void multipleShocksOnSameData() {
    MarketDataShock absoluteShift = MarketDataShock.absoluteShift(0.1, MATCHER1);
    MarketDataShock relativeShift = MarketDataShock.relativeShift(0.5, MATCHER1);
    FilteredScenarioDefinition scenarioDef = new FilteredScenarioDefinition(absoluteShift, relativeShift);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), MARKET_DATA_BUNDLE, scenarioDef);

    assertEquals(1.65, FN.foo(env, SEC1).getValue(), DELTA);
    assertEquals(2d, FN.foo(env, SEC2).getValue(), DELTA);
  }


  /**
   * apply two shocks to the same piece of data
   */
  @Test
  public void multipleShocksOnSameDataReversed() {
    MarketDataShock relativeShift = MarketDataShock.relativeShift(0.5, MATCHER1);
    MarketDataShock absoluteShift = MarketDataShock.absoluteShift(0.1, MATCHER1);
    FilteredScenarioDefinition scenarioDef = new FilteredScenarioDefinition(relativeShift, absoluteShift);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), MARKET_DATA_BUNDLE, scenarioDef);

    assertEquals(1.6, FN.foo(env, SEC1).getValue(), DELTA);
    assertEquals(2d, FN.foo(env, SEC2).getValue(), DELTA);
  }

  /**
   * try (and fail) to shock a piece of market data that isn't a double
   */
  public void dataNotDouble() {
    MarketDataBundle marketDataBundle = new MapMarketDataBundle(
        new MarketDataEnvironmentBuilder()
            .add(SecurityId.of(SEC1.getExternalIdBundle()), "not a double")
            .add(SecurityId.of(SEC2.getExternalIdBundle()), 2.0)
            .valuationTime(ZonedDateTime.now())
            .build());
    MarketDataShock shock = MarketDataShock.relativeShift(0.5, MATCHER1);
    FilteredScenarioDefinition scenarioDef = new FilteredScenarioDefinition(shock);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), marketDataBundle, scenarioDef);

    assertFalse(FN.foo(env, SEC1).isSuccess());
    assertEquals(2d, FN.foo(env, SEC2).getValue(), DELTA);
  }

  public interface Fn {

    @Output("Foo")
    Result<Double> foo(Environment env, Security security);
  }

  public static class Impl implements Fn {

    private final MarketDataFn _marketDataFn;

    public Impl(MarketDataFn marketDataFn) {
      _marketDataFn = marketDataFn;
    }

    @Override
    public Result<Double> foo(Environment env, Security security) {
      return _marketDataFn.getMarketValue(env, security);
    }
  }
}

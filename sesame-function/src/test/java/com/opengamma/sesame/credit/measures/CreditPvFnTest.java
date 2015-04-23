/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.credit.CreditPricingSampleData;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the CDS PV
 */
@Test(groups = TestGroup.UNIT)
public class CreditPvFnTest {

  /* Expected PV validated external to OG */
  public static final double SINGLE_NAME_EXPECTED_PV = -36941.17725;
  public static final double INDEX_EXPECTED_PV = -20558.01483;
  public static final double PUF_EXPECTED_PV = -13472.22222;

  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private FunctionModelConfig _config;
  private ComponentMap _componentMap;
  private FunctionRunner _functionRunner;
  private DefaultCreditPvFn _function;
  private static final MarketDataEnvironment ENV = CreditPricingSampleData.getCreditMarketDataEnvironment(VALUATION_TIME);
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();

  @BeforeMethod
  public void setUpClass()  {
    _config = CreditPricingSampleData.createFunctionModelConfig();
    _componentMap = ComponentMap.of(CreditPricingSampleData.generateBaseComponents());
    _function = FunctionModel.build(DefaultCreditPvFn.class, _config, _componentMap);
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext =
        ServiceContext.of(_componentMap.getComponents()).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
    EmptyMarketDataFactory dataFactory = new EmptyMarketDataFactory();
    MarketDataEnvironmentFactory environmentFactory =
        new MarketDataEnvironmentFactory(dataFactory,
                                         MarketDataBuilders.creditCurve(),
                                         MarketDataBuilders.isdaYieldCurve());
    _functionRunner = new FunctionRunner(environmentFactory);
  }

  @Test
  public void testStandardCdsPV() {

    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _function.priceStandardCds(env, CreditPricingSampleData.createStandardCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    CurrencyAmount postCa = result.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(SINGLE_NAME_EXPECTED_PV, STD_TOLERANCE_PV)));

  }

  @Test
  public void testPUFStandardCdsPV() {

    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _function.priceStandardCds(env, CreditPricingSampleData.createPointsUpFrontStandardCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    CurrencyAmount postCa = result.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(PUF_EXPECTED_PV, STD_TOLERANCE_PV)));

  }

  @Test
  public void testLegacyCdsPV() {

    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _function.priceLegacyCds(env, CreditPricingSampleData.createLegacyCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    CurrencyAmount postCa = result.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(SINGLE_NAME_EXPECTED_PV, STD_TOLERANCE_PV)));

  }

  @Test
  public void testIndexCdsPV() {

    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _function.priceIndexCds(env, CreditPricingSampleData.createIndexCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    CurrencyAmount postCa = result.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(INDEX_EXPECTED_PV, STD_TOLERANCE_PV)));

  }

}

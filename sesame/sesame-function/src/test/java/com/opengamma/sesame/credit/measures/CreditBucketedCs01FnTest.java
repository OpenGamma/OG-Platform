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

import com.opengamma.financial.analytics.TenorLabelledMatrix1D;
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
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the CDS CS01
 */
@Test(groups = TestGroup.UNIT)
public class CreditBucketedCs01FnTest {

  /* Expected results validated external to OG */
  public static final double EXPECTED_CS01_5Y = 506.4119079;
  public static final double PUF_EXPECTED_CS01_5Y = 421.92896;
  public static final double EXPECTED_INDEX_CS01 = 504.84802;
  // risk concentrated around 4Y
  private static final double EXPECTED_MULTI_POINT_INDEX_CS01_2Y = 2.185144915407834;
  private static final double EXPECTED_MULTI_POINT_INDEX_CS01_3Y = 155.2472982766993;
  private static final double EXPECTED_MULTI_POINT_INDEX_CS01_5Y = 250.35807869708287;
  private static final double EXPECTED_MULTI_POINT_INDEX_CS01_10Y = 0.0;
  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private static final ZonedDateTime FUTURE_VALUATION_TIME = DateUtils.getUTCDate(2015, 10, 16);
  private FunctionModelConfig _config;
  private ComponentMap _componentMap;
  private FunctionRunner _functionRunner;
  private DefaultCreditBucketedCs01Fn _function;
  private static final MarketDataEnvironment ENV = CreditPricingSampleData.getCreditMarketDataEnvironment(VALUATION_TIME);
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();

  private static final MarketDataEnvironment FUTURE_ENV = CreditPricingSampleData.getCreditMarketDataEnvironment(FUTURE_VALUATION_TIME);
  private static final CalculationArguments FUTURE_ARGS =
      CalculationArguments.builder()
          .valuationTime(FUTURE_VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();

  @BeforeMethod
  public void setUpClass()  {
    _config = CreditPricingSampleData.createFunctionModelConfig();
    _componentMap = ComponentMap.of(CreditPricingSampleData.generateBaseComponents());
    _function = FunctionModel.build(DefaultCreditBucketedCs01Fn.class, _config, _componentMap);
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
  public void testStandardCdsCS01() {

    Result<TenorLabelledMatrix1D> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<TenorLabelledMatrix1D>>() {
          @Override
          public Result<TenorLabelledMatrix1D> apply(Environment env) {
            return _function.priceStandardCds(env, CreditPricingSampleData.createStandardCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[5], is(closeTo(EXPECTED_CS01_5Y, STD_TOLERANCE_PV)));

  }

  @Test
  public void testPUFStandardCdsCS01() {

    Result<TenorLabelledMatrix1D> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<TenorLabelledMatrix1D>>() {
          @Override
          public Result<TenorLabelledMatrix1D> apply(Environment env) {
            return _function.priceStandardCds(env, CreditPricingSampleData.createPointsUpFrontStandardCDSSecurity());
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[0], is(closeTo(PUF_EXPECTED_CS01_5Y, STD_TOLERANCE_PV)));

  }

  @Test
  public void testLegacyCdsCS01() {

    Result<TenorLabelledMatrix1D> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<TenorLabelledMatrix1D>>() {
          @Override
          public Result<TenorLabelledMatrix1D> apply(Environment env) {
            return _function.priceLegacyCds(env, CreditPricingSampleData.createLegacyCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[5], is(closeTo(EXPECTED_CS01_5Y, STD_TOLERANCE_PV)));

  }

  @Test
  public void testIndexCdsCS01() {

    Result<TenorLabelledMatrix1D> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<TenorLabelledMatrix1D>>() {
          @Override
          public Result<TenorLabelledMatrix1D> apply(Environment env) {
            return _function.priceIndexCds(env, CreditPricingSampleData.createIndexCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[0], is(closeTo(EXPECTED_INDEX_CS01, STD_TOLERANCE_PV)));
  }

  @Test
  public void testMultiPointIndexCdsCS01() {

    Result<TenorLabelledMatrix1D> result = _functionRunner.runFunction(
        FUTURE_ARGS, FUTURE_ENV,
        new Function<Environment, Result<TenorLabelledMatrix1D>>() {
          @Override
          public Result<TenorLabelledMatrix1D> apply(Environment env) {
            return _function.priceIndexCds(env, CreditPricingSampleData.createMultiPointIndexCDSSecurity());
          }
        });

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[0], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_2Y, STD_TOLERANCE_PV)));
    assertThat(result.getValue().getValues()[1], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_3Y, STD_TOLERANCE_PV)));
    assertThat(result.getValue().getValues()[2], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_5Y, STD_TOLERANCE_PV)));
    assertThat(result.getValue().getValues()[3], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_10Y, STD_TOLERANCE_PV)));
  }

}

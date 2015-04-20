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
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
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
  private static final Environment ENV =
      new SimpleEnvironment(VALUATION_TIME, CreditPricingSampleData.getCreditMarketDataBundle(VALUATION_TIME));
  private static final Environment FUTURE_ENV =
      new SimpleEnvironment(FUTURE_VALUATION_TIME, CreditPricingSampleData.getCreditMarketDataBundle(
          FUTURE_VALUATION_TIME));
  private FunctionModelConfig _config;
  private ComponentMap _componentMap;

  @BeforeMethod
  public void setUpClass()  {
    _config = CreditPricingSampleData.createFunctionModelConfig();
    _componentMap = ComponentMap.of(CreditPricingSampleData.generateBaseComponents());
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext =
        ServiceContext.of(_componentMap.getComponents()).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
  }

  @Test
  public void testStandardCdsCS01() {

    DefaultCreditBucketedCs01Fn function = FunctionModel.build(DefaultCreditBucketedCs01Fn.class, _config, _componentMap);
    Result<TenorLabelledMatrix1D> result = function.priceStandardCds(ENV, CreditPricingSampleData.createStandardCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[5], is(closeTo(EXPECTED_CS01_5Y, STD_TOLERANCE_PV)));

  }

  @Test
  public void testPUFStandardCdsCS01() {

    DefaultCreditBucketedCs01Fn function = FunctionModel.build(DefaultCreditBucketedCs01Fn.class, _config, _componentMap);
    Result<TenorLabelledMatrix1D> result = function.priceStandardCds(ENV, CreditPricingSampleData.createPointsUpFrontStandardCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[0], is(closeTo(PUF_EXPECTED_CS01_5Y, STD_TOLERANCE_PV)));

  }

  @Test
  public void testLegacyCdsCS01() {

    DefaultCreditBucketedCs01Fn function = FunctionModel.build(DefaultCreditBucketedCs01Fn.class, _config, _componentMap);
    Result<TenorLabelledMatrix1D> result = function.priceLegacyCds(ENV, CreditPricingSampleData.createLegacyCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[5], is(closeTo(EXPECTED_CS01_5Y, STD_TOLERANCE_PV)));

  }

  @Test
  public void testIndexCdsCS01() {

    DefaultCreditBucketedCs01Fn function = FunctionModel.build(DefaultCreditBucketedCs01Fn.class, _config, _componentMap);
    Result<TenorLabelledMatrix1D> result = function.priceIndexCds(ENV, CreditPricingSampleData.createIndexCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[0], is(closeTo(EXPECTED_INDEX_CS01, STD_TOLERANCE_PV)));
  }

  @Test
  public void testMultiPointIndexCdsCS01() {

    DefaultCreditBucketedCs01Fn function = FunctionModel.build(DefaultCreditBucketedCs01Fn.class, _config, _componentMap);
    Result<TenorLabelledMatrix1D> result = function.priceIndexCds(FUTURE_ENV, CreditPricingSampleData.createMultiPointIndexCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getValues()[0], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_2Y, STD_TOLERANCE_PV)));
    assertThat(result.getValue().getValues()[1], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_3Y, STD_TOLERANCE_PV)));
    assertThat(result.getValue().getValues()[2], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_5Y, STD_TOLERANCE_PV)));
    assertThat(result.getValue().getValues()[3], is(closeTo(EXPECTED_MULTI_POINT_INDEX_CS01_10Y, STD_TOLERANCE_PV)));
  }

}

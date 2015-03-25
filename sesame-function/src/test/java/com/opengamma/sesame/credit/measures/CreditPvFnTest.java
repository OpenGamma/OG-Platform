/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.not;
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
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.result.SuccessResult;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the CDS PV
 */
@Test(groups = TestGroup.UNIT)
public class CreditPvFnTest {

  /* Index factor matches the basket in the definition of th CDX security */
  public static final double INDEX_FACTOR = 0.97;
  /* Expected single name post IMM validated external to OG */
  public static final double POST_IMM_EXPECTED_PV = 103477.13641;
  public static final double PRE_IMM_EXPECTED_PV = 100738.28958;
  public static final double POST_IMM_EXPECTED_INDEX_PV = POST_IMM_EXPECTED_PV * INDEX_FACTOR;
  private static final double PRE_IMM_EXPECTED_INDEX_PV = 113302.64131;
  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final ZonedDateTime POST_IMM_VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private static final Environment POST_IMM_ENV = new SimpleEnvironment(POST_IMM_VALUATION_TIME,
                                                               MarketDataEnvironmentBuilder.empty().toBundle());
  private static final ZonedDateTime PRE_IMM_VALUATION_TIME = DateUtils.getUTCDate(2015, 1, 2);
  private static final Environment PRE_IMM_ENV = new SimpleEnvironment(PRE_IMM_VALUATION_TIME,
                                                                        MarketDataEnvironmentBuilder.empty().toBundle());
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
  public void testStandardCdsPV() {

    DefaultCreditPvFn function = FunctionModel.build(DefaultCreditPvFn.class, _config, _componentMap);

    Result<CurrencyAmount> postResult = function.priceStandardCds(POST_IMM_ENV,
                                                                  CreditPricingSampleData.createStandardCDSSecurity());
    assertThat(postResult.isSuccess(), is(true));
    CurrencyAmount postCa = postResult.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(POST_IMM_EXPECTED_PV, STD_TOLERANCE_PV)));

    Result<CurrencyAmount> preResult = function.priceStandardCds(PRE_IMM_ENV,
                                                                 CreditPricingSampleData.createStandardCDSSecurity());
    assertThat(preResult.isSuccess(), is(true));
    CurrencyAmount preCa = preResult.getValue();
    assertThat(preCa.getCurrency(), is(Currency.USD));
    assertThat(preCa.getAmount(), is(closeTo(PRE_IMM_EXPECTED_PV, STD_TOLERANCE_PV)));
  }

  @Test
  public void testLegacyCdsPV() {

    DefaultCreditPvFn function = FunctionModel.build(DefaultCreditPvFn.class, _config, _componentMap);

    Result<CurrencyAmount> postResult = function.priceLegacyCds(POST_IMM_ENV,
                                                                CreditPricingSampleData.createLegacyCDSSecurity());
    assertThat(postResult.isSuccess(), is(true));
    CurrencyAmount postCa = postResult.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(POST_IMM_EXPECTED_PV, STD_TOLERANCE_PV)));

    Result<CurrencyAmount> preResult = function.priceLegacyCds(PRE_IMM_ENV,
                                                               CreditPricingSampleData.createLegacyCDSSecurity());
    assertThat(preResult.isSuccess(), is(true));
    CurrencyAmount preCa = preResult.getValue();
    assertThat(preCa.getCurrency(), is(Currency.USD));
    assertThat(preCa.getAmount(), is(closeTo(PRE_IMM_EXPECTED_PV, STD_TOLERANCE_PV)));
  }

  @Test
  public void testIndexCdsPV() {

    DefaultCreditPvFn function = FunctionModel.build(DefaultCreditPvFn.class, _config, _componentMap);

    Result<CurrencyAmount> postResult = function.priceIndexCds(POST_IMM_ENV,
                                                               CreditPricingSampleData.createIndexCDSSecurity());
    assertThat(postResult.isSuccess(), is(true));
    CurrencyAmount postCa = postResult.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(POST_IMM_EXPECTED_INDEX_PV, STD_TOLERANCE_PV)));

    Result<CurrencyAmount> preResult = function.priceIndexCds(PRE_IMM_ENV,
                                                              CreditPricingSampleData.createIndexCDSSecurity());

    assertThat(preResult.isSuccess(), is(true));
    CurrencyAmount preCa = preResult.getValue();
    assertThat(preCa.getCurrency(), is(Currency.USD));
    assertThat(preCa.getAmount(), is(closeTo(PRE_IMM_EXPECTED_INDEX_PV, STD_TOLERANCE_PV)));

    // We are not expecting the present value of pre imm single name and index to correspond
    assertThat(preCa.getAmount(), not(closeTo(PRE_IMM_EXPECTED_PV * INDEX_FACTOR, STD_TOLERANCE_PV)));
  }

}

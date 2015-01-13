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
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the CDS CS01
 */
@Test(groups = TestGroup.UNIT)
public class CreditCs01FnTest {

  /* Expected results validated external to OG */
  public static final double EXPECTED_CS01 = 4884.4636;
  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private static final Environment ENV = new SimpleEnvironment(VALUATION_TIME,
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
  public void testStandardCdsCS01() {

    DefaultCreditCs01Fn function = FunctionModel.build(DefaultCreditCs01Fn.class, _config, _componentMap);
    Result<CurrencyAmount> result = function.priceStandardCds(ENV, CreditPricingSampleData.createStandardCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    CurrencyAmount ca = result.getValue();
    assertThat(ca.getCurrency(), is(Currency.USD));
    assertThat(ca.getAmount(), is(closeTo(EXPECTED_CS01, STD_TOLERANCE_PV)));
  }

  @Test
  public void testLegacyCdsCS01() {

    DefaultCreditCs01Fn function = FunctionModel.build(DefaultCreditCs01Fn.class, _config, _componentMap);
    Result<CurrencyAmount> result = function.priceLegacyCds(ENV, CreditPricingSampleData.createLegacyCDSSecurity());

    assertThat(result.isSuccess(), is(true));
    CurrencyAmount ca = result.getValue();
    assertThat(ca.getCurrency(), is(Currency.USD));
    assertThat(ca.getAmount(), is(closeTo(EXPECTED_CS01, STD_TOLERANCE_PV)));
  }

}

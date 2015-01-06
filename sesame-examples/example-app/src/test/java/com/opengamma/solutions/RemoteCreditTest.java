/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.net.URI;
import java.util.List;

import org.hamcrest.core.Is;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Objects;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.RemoteEngine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.util.RemoteViewCreditUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests run against a remote server
 * Input: Credit Default Swap, Snapshot Market Data
 * Output: Present Value, CS01
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteCreditTest {

  private static final double EXPECTED_PV = 103477.13641;
  public static final double EXPECTED_CS01 = 4884.4636;
  private static final double STD_TOLERANCE = 1.0E-3;
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);

  private Results _results;

  @BeforeClass
  public void setUp() {
    String url = Objects.firstNonNull(System.getProperty("server.url"), RemoteTestUtils.LOCALHOST);

    Engine engine = new RemoteEngine(URI.create(url));
    MarketDataSpecification marketDataSpec = EmptyMarketDataSpec.INSTANCE;

    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .valuationTime(VALUATION_TIME)
            .marketDataSpecification(marketDataSpec)
            .build();

    // don't want to provide any data, let the server source it
    MarketDataEnvironment marketDataEnvironment = MarketDataEnvironmentBuilder.empty();
    ViewConfig viewConfig = RemoteViewCreditUtils.createViewConfig("Sample Credit Curve", "Sample Yield Curve");
    List<Object> trades = RemoteViewCreditUtils.INPUTS;

    _results = engine.runView(viewConfig, calculationArguments, marketDataEnvironment, trades);
  }


  @Test
  public void testStandardCdsPV() {

    Result result = _results.get(0, 0).getResult();
    assertThat(result.isSuccess(), Is.is(true));
    CurrencyAmount ca = (CurrencyAmount) result.getValue();
    assertThat(ca.getCurrency(), is(Currency.USD));
    assertThat(ca.getAmount(), is(closeTo(EXPECTED_PV, STD_TOLERANCE)));
  }

  @Test
  public void testLegacyCdsPV() {

    Result result = _results.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    CurrencyAmount ca = (CurrencyAmount) result.getValue();
    assertThat(ca.getCurrency(), is(Currency.USD));
    assertThat(ca.getAmount(), is(closeTo(EXPECTED_PV, STD_TOLERANCE)));
  }

  @Test
  public void testStandardCdsCS01() {

    Result result = _results.get(0, 1).getResult();
    assertThat(result.isSuccess(), Is.is(true));
    CurrencyAmount ca = (CurrencyAmount) result.getValue();
    assertThat(ca.getCurrency(), Is.is(Currency.USD));
    assertThat(ca.getAmount(), Is.is(closeTo(EXPECTED_CS01, STD_TOLERANCE)));
  }

  @Test
  public void testLegacyCdsCS01() {

    Result result = _results.get(1, 1).getResult();
    assertThat(result.isSuccess(), Is.is(true));
    CurrencyAmount ca = (CurrencyAmount) result.getValue();
    assertThat(ca.getCurrency(), Is.is(Currency.USD));
    assertThat(ca.getAmount(), Is.is(closeTo(EXPECTED_CS01, STD_TOLERANCE)));
  }


}

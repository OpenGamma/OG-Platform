/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.net.URI;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Objects;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.RemoteEngine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.util.RemoteViewFraUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests run against a remote server
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteFraTest {

  private static final double STD_TOLERANCE_PV = 1.0E-3;

  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private Results _results;

  @BeforeClass
  public void setUp() {
    String url = Objects.firstNonNull(System.getProperty("server.url"), RemoteTestUtils.LOCALHOST);

    RemoteServer server = RemoteServer.create(url);
    MarketDataSnapshotSource snapshotSource = server.getMarketDataSnapshotSource();
    ManageableMarketDataSnapshot snapshot = snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
                                                                     RemoteTestUtils.USD_GBP_SNAPSHOT,
                                                                     VersionCorrection.LATEST);

    Engine engine = new RemoteEngine(URI.create(url));
    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());

    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
            .marketDataSpecification(marketDataSpec)
            .build();

    _exposureConfig = ConfigLink.resolvable(RemoteTestUtils.USD_GBP_FF_EXPOSURE, ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable(RemoteTestUtils.CURRENCY_MATRIX, CurrencyMatrix.class);

    // don't want to provide any data, let the server source it
    MarketDataEnvironment marketDataEnvironment = MarketDataEnvironmentBuilder.empty();
    ViewConfig viewConfig = createViewConfig();
    List<Object> trades = RemoteViewFraUtils.INPUTS;

    _results = engine.runView(viewConfig, calculationArguments, marketDataEnvironment, trades);
  }

  private ViewConfig createViewConfig() {
    return
        configureView(
            "FRA Remote view",
            RemoteViewFraUtils.createFraViewColumn(
                OutputNames.PRESENT_VALUE,
                _exposureConfig,
                _currencyMatrixLink));
  }

  @Test(enabled = true)
  public void testForwardRateAgreementPV() {

    Result result = _results.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(21750.76254296188, STD_TOLERANCE_PV)));
  }
}

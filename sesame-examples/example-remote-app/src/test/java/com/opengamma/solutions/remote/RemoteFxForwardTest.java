package com.opengamma.solutions.remote;


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
import com.opengamma.sesame.engine.RemoteViewRunner;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.engine.ViewRunner;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.util.FxForwardViewUtils;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Test(groups = TestGroup.INTEGRATION)
public class RemoteFxForwardTest {


  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;

  private List<Object> _inputs = FxForwardViewUtils.FX_SECURITY_INPUTS;
  //Note: FX Forward Present Value function currently works on security not trade

  private ViewRunner _viewRunner;
  private CalculationArguments _calculationArguments;
  private MarketDataEnvironment _marketDataEnvironment;
  private RemoteServer _remoteServer;
  private ViewConfig _viewConfig;


  @BeforeClass
  public void setUp() {
    String url = Objects.firstNonNull(System.getProperty("server.url"), RemoteTestUtils.LOCALHOST);

    _viewRunner = new RemoteViewRunner(URI.create(url));

    _remoteServer = RemoteServer.create(url);

    MarketDataSnapshotSource snapshotSource = _remoteServer.getMarketDataSnapshotSource();
    ManageableMarketDataSnapshot snapshot = snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
        RemoteTestUtils.USD_GBP_SNAPSHOT,
        VersionCorrection.LATEST);

    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());

    _calculationArguments =
        CalculationArguments.builder()
            .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
            .marketDataSpecification(marketDataSpec)
            .configVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now()))
            .build();

    _marketDataEnvironment = MarketDataEnvironmentBuilder.empty();

    _exposureConfig = ConfigLink.resolvable("USD-GBP-FF-1", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BBG-Matrix", CurrencyMatrix.class);

    _viewConfig = FxForwardViewUtils.createViewConfig(_exposureConfig, _currencyMatrixLink);

  }


  @Test(enabled = true)
  public void testSingleFxForwardPVExecution() {
    // TODO check with analytics
    Results results = _viewRunner.runView(_viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
    Result result = results.get(0, OutputNames.FX_PRESENT_VALUE).getResult();
    System.out.println(result.getValue());
    assertThat(result.isSuccess(), is(true));

  }



}

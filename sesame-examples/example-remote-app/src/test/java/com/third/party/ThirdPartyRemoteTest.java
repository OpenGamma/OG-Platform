package com.third.party;

import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.RemoteViewRunner;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.engine.ViewRunner;
import com.opengamma.sesame.irs.DefaultInterestRateSwapConverterFn;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapConverterFn;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.remote.RemoteTestUtils;
import com.opengamma.solutions.util.SwapViewUtils;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import java.net.URI;
import java.util.List;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests that a view can be run against a remote server.
 * The tests cover the validation of a successful PV result
 * and a the curve bundle used to price the swap.
 */

@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class ThirdPartyRemoteTest {

  private static final String URL = "http://localhost:8080/jax";
  private static final String CURVE_RESULT = "Curve Bundle";
  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private ConfigLink<CurveConstructionConfiguration> _curveConstructionConfiguration;
  /* A single Fixed vs Libor 3m Swap ManageableSecurity list */
  private List<Object> _inputs = SwapViewUtils.VANILLA_TRADES;
  private ViewRunner _viewRunner;
  private CalculationArguments _calculationArguments;
  private MarketDataEnvironment _marketDataEnvironment;
  private RemoteServer _remoteServer;
  private ViewConfig _viewConfig;


  @BeforeClass
  public void setUp() {

    _viewRunner = new RemoteViewRunner(URI.create(URL));

    _remoteServer = RemoteServer.create(URL);

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
    _curveConstructionConfiguration = ConfigLink.resolvable("USD TO GBP CSA USD Curve Construction Configuration",
        CurveConstructionConfiguration.class);

    _viewConfig = createViewConfig();

  }

  @Test(enabled = true)
  public void testSingleSwapPVExecution() {

    Results results = _viewRunner.runView(_viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
    Result result = results.get(0, OutputNames.PRESENT_VALUE).getResult();
    assertThat(result.isSuccess(), is(true));

  }

  @Test(enabled = false)
  public void testSingleSwapReceiveLegCashFlowsExecution() {

    Results results = _viewRunner.runView(_viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
    Result result = results.get(0, OutputNames.RECEIVE_LEG_CASH_FLOWS).getResult();
    assertThat(result.isSuccess(), is(true));

  }

  @Test(enabled = false)
  public void testSingleSwapPayLegCashFlowsExecution() {

    Results results = _viewRunner.runView(_viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
    Result result = results.get(0, OutputNames.PAY_LEG_CASH_FLOWS).getResult();
    assertThat(result.isSuccess(), is(true));
  }

  @Test(enabled = false)
  public void testSingleSwapBucketedPV01Execution() {

    Results results = _viewRunner.runView(_viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
    Result result = results.get(0, OutputNames.BUCKETED_PV01).getResult();
    assertThat(result.isSuccess(), is(true));

  }

  @Test(enabled = false)
  public void testSingleSwapPV01Execution() {

    Results results = _viewRunner.runView(_viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
    Result result = results.get(0, OutputNames.PV01).getResult();
    assertThat(result.isSuccess(), is(true));

  }

  /* Output specific view configuration for interest rate swaps */

  private ViewConfig createViewConfig() {
    return
        configureView(
            "IRS Remote view",
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", _exposureConfig)),
                    function(
                        DefaultHistoricalMarketDataFn.class,
                        argument("currencyMatrix", _currencyMatrixLink))),
                implementations(
                    CurveSelector.class, MarketExposureSelector.class,
                    DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class,
                    InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
                    InterestRateSwapConverterFn.class, DefaultInterestRateSwapConverterFn.class,
                    InterestRateSwapCalculatorFactory.class, ThirdPartyInterestRateSwapCalculatorFactory.class)),
            column(OutputNames.PRESENT_VALUE),
            column(OutputNames.BUCKETED_PV01),
            column(OutputNames.PAY_LEG_CASH_FLOWS),
            column(OutputNames.RECEIVE_LEG_CASH_FLOWS),
            column(OutputNames.PV01));
  }


}
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
import com.opengamma.sesame.*;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.RemoteEngine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.irs.*;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.solutions.remote.RemoteTestUtils;
import com.opengamma.solutions.util.SwapViewUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.Period;

import java.net.URI;
import java.util.List;

import static com.opengamma.sesame.config.ConfigBuilder.*;
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
    private Engine _engine;
    private CalculationArguments _calculationArguments;
    private MarketDataEnvironment _marketDataEnvironment;
    private RemoteServer _remoteServer;
    private List<InterestRateSwapTrade> _inputTrades;


    @BeforeClass
    public void setUp() {

      _engine = new RemoteEngine(URI.create(URL));

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

    }

    @Test(enabled = true)
    public void testSingleSwapPVExecution() {


      ViewConfig viewConfig = createViewConfig();

      Results results = _engine.runView(viewConfig,_calculationArguments,_marketDataEnvironment,_inputs);
      Result result = results.get(0,OutputNames.PRESENT_VALUE).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = true)
    public void testSingleSwapReceiveLegCashFlowsExecution() {

      ViewConfig viewConfig = createViewConfig();

      Results results = _engine.runView(viewConfig,_calculationArguments,_marketDataEnvironment,_inputs);
      Result result = results.get(0,OutputNames.RECEIVE_LEG_CASH_FLOWS).getResult();
      assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testSingleSwapPayLegCashFlowsExecution() {

      ViewConfig viewConfig = createViewConfig();

      Results results = _engine.runView(viewConfig, _calculationArguments, _marketDataEnvironment, _inputs);
      Result result = results.get(0,OutputNames.PAY_LEG_CASH_FLOWS).getResult();
      assertThat(result.isSuccess(), is(true));
    }

    @Test(enabled = false)
    public void testSingleSwapBucketedPV01Execution() {

      ViewConfig viewConfig = createViewConfig();

      Results results = _engine.runView(viewConfig,_calculationArguments,_marketDataEnvironment,_inputs);
      Result result = results.get(0,OutputNames.BUCKETED_PV01).getResult();
      assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testSingleSwapPV01Execution() {

      ViewConfig viewConfig = createViewConfig();

      Results results = _engine.runView(viewConfig,_calculationArguments,_marketDataEnvironment,_inputs);
      Result result = results.get(0,OutputNames.PV01).getResult();
      assertThat(result.isSuccess(), is(true));

    }
//
//    @Test(enabled = false)
//    public void testCurveBundleExecution() {
//
//        FunctionServerRequest<IndividualCycleOptions> request =
//                FunctionServerRequest.<IndividualCycleOptions>builder()
//                        .viewConfig(createCurveBundleConfig())
//                        .cycleOptions(_cycleOptions)
//                        .build();
//
//        Results results = _functionServer.executeSingleCycle(request);
//        Result result = results.getNonPortfolioResults().get(CURVE_RESULT).getResult();
//        assertThat(result.isSuccess(), is(true));
//
//    }

    /* Output specific view configuration for interest rate swaps */
    private ViewConfig createViewConfig() {
      return
          configureView(
              "IRS Remote view",
              createThirdPartyInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                  _exposureConfig,
                  _currencyMatrixLink),
              createThirdPartyInterestRateSwapViewColumn(OutputNames.BUCKETED_PV01,
                  _exposureConfig,
                  _currencyMatrixLink),
              createThirdPartyInterestRateSwapViewColumn(OutputNames.PAY_LEG_CASH_FLOWS,
                  _exposureConfig,
                  _currencyMatrixLink),
              createThirdPartyInterestRateSwapViewColumn(OutputNames.RECEIVE_LEG_CASH_FLOWS,
                  _exposureConfig,
                  _currencyMatrixLink),
              createThirdPartyInterestRateSwapViewColumn(OutputNames.PV01,
                  _exposureConfig,
                  _currencyMatrixLink));
    }


  public static ViewColumn createThirdPartyInterestRateSwapViewColumn(String output,
                                                                      ConfigLink<ExposureFunctions> exposureConfig,
                                                                      ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    ArgumentChecker.notNull(output, "output");
    ArgumentChecker.notNull(exposureConfig, "exposureConfig");
    ArgumentChecker.notNull(currencyMatrixLink, "currencyMatrixLink");

    return
        column(
            output,
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", exposureConfig)),
                    function(
                        DefaultHistoricalMarketDataFn.class,
                        argument("currencyMatrix", currencyMatrixLink))),
                implementations(
                    CurveSelector.class, MarketExposureSelector.class,
                    DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class,
                    InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
                    InterestRateSwapConverterFn.class, DefaultInterestRateSwapConverterFn.class,
                    InterestRateSwapCalculatorFactory.class, ThirdPartyInterestRateSwapCalculatorFactory.class)));
                    //InterestRateSwapCalculatorFactory.class, DiscountingInterestRateSwapCalculatorFactory.class)));
  }

    /* A non portfolio output view configuration to capture the build curves */
    private ViewConfig createCurveBundleConfig() {

        return configureView(
                "Curve Bundle only",
                nonPortfolioOutput(
                        CURVE_RESULT,
                        output(OutputNames.DISCOUNTING_MULTICURVE_BUNDLE,
                                config(
                                        arguments(
                                                function(
                                                        RootFinderConfiguration.class,
                                                        argument("rootFinderAbsoluteTolerance", 1e-9),
                                                        argument("rootFinderRelativeTolerance", 1e-9),
                                                        argument("rootFinderMaxIterations", 1000)),
                                                function(DefaultCurveNodeConverterFn.class,
                                                        argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                                                function(DefaultHistoricalMarketDataFn.class,
                                                        argument("dataSource", "BLOOMBERG"),
                                                        argument("currencyMatrix", _currencyMatrixLink)),
                                                function(DefaultMarketDataFn.class,
                                                        argument("dataSource", "BLOOMBERG"),
                                                        argument("currencyMatrix", _currencyMatrixLink)),
                                                function(
                                                        DefaultHistoricalTimeSeriesFn.class,
                                                        argument("resolutionKey", "DEFAULT_TSS"),
                                                        argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
                                                function(
                                                        DefaultDiscountingMulticurveBundleResolverFn.class,
                                                        argument("curveConfig", _curveConstructionConfiguration)),
                                                function(
                                                        DefaultDiscountingMulticurveBundleFn.class,
                                                        argument("impliedCurveNames", StringSet.of())))))
                ));
    }

}

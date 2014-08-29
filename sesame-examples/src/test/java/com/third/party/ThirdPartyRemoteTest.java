package com.third.party;

import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.*;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.sesame.*;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.server.FunctionServer;
import com.opengamma.sesame.server.FunctionServerRequest;
import com.opengamma.sesame.server.IndividualCycleOptions;
import com.opengamma.sesame.server.RemoteFunctionServer;
import com.opengamma.solutions.RemoteViewUtils;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.opengamma.sesame.config.ConfigBuilder.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests that a view can be run against a remote server.
 * The tests cover the validation of a successful PV result
 * and a the curve bundle used to price the swap.
 */

@Test(groups = TestGroup.INTEGRATION, enabled = false)
public class ThirdPartyRemoteTest {

    private static final String URL = "http://localhost:8080/jax";
    private static final String CURVE_RESULT = "Curve Bundle";
    private FunctionServer _functionServer;
    private IndividualCycleOptions _cycleOptions;
    private ConfigLink<ExposureFunctions> _exposureConfig;
    private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
    private ConfigLink<CurveConstructionConfiguration> _curveConstructionConfiguration;
    private List<ManageableSecurity> _inputs = new ArrayList<>();


    @BeforeClass
    public void setUp() {

        /* Create a RemoteFunctionServer to executes view requests RESTfully.*/
        _functionServer = new RemoteFunctionServer(URI.create(URL));

        /* Single cycle options containing the market data specification and valuation time */
        _cycleOptions = IndividualCycleOptions.builder()
                .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
                .marketDataSpec(LiveMarketDataSpecification.of("Bloomberg"))
                .build();

        /* Configuration links matching the curve exposure function, currency matrix and curve bundle
           as named on the remote server. These are needed as specific arguments in the creation
           of the ViewConfig. */
        _exposureConfig = ConfigLink.resolvable("USD CSA Exposure Functions", ExposureFunctions.class);
        _currencyMatrixLink = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);
        _curveConstructionConfiguration = ConfigLink.resolvable("USD TO GBP CSA USD Curve Construction Configuration",
                CurveConstructionConfiguration.class);

        /* Add a single Fixed vs Libor 3m Swap to the ManageableSecurity list */
        _inputs.add(RemoteViewUtils.createVanillaFixedVsLiborSwap());
    }

    @Test(enabled = false)
    public void testSingleSwapPVExecution() {

        /* Building the output specific request, based on a the view config, the single cycle options
           and the List<ManageableSecurity> containing a single swap */
        FunctionServerRequest<IndividualCycleOptions> request =
                FunctionServerRequest.<IndividualCycleOptions>builder()
                        .viewConfig(createViewConfig(OutputNames.PRESENT_VALUE))
                        .inputs(_inputs)
                        .cycleOptions(_cycleOptions)
                        .build();

        /* Execute the engine cycle and extract the result */
        Results results = _functionServer.executeSingleCycle(request);
        Result result = results.get(0,0).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testSingleSwapReceiveLegCashFlowsExecution() {

        FunctionServerRequest<IndividualCycleOptions> request =
                FunctionServerRequest.<IndividualCycleOptions>builder()
                        .viewConfig(createViewConfig(OutputNames.RECEIVE_LEG_CASH_FLOWS))
                        .inputs(_inputs)
                        .cycleOptions(_cycleOptions)
                        .build();

        Results results = _functionServer.executeSingleCycle(request);
        Result result = results.get(0,0).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testSingleSwapPayLegCashFlowsExecution() {

        FunctionServerRequest<IndividualCycleOptions> request =
                FunctionServerRequest.<IndividualCycleOptions>builder()
                        .viewConfig(createViewConfig(OutputNames.PAY_LEG_CASH_FLOWS))
                        .inputs(_inputs)
                        .cycleOptions(_cycleOptions)
                        .build();

        Results results = _functionServer.executeSingleCycle(request);
        Result result = results.get(0,0).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testSingleSwapBucketedPV01Execution() {

        FunctionServerRequest<IndividualCycleOptions> request =
                FunctionServerRequest.<IndividualCycleOptions>builder()
                        .viewConfig(createViewConfig(OutputNames.BUCKETED_PV01))
                        .inputs(_inputs)
                        .cycleOptions(_cycleOptions)
                        .build();

        Results results = _functionServer.executeSingleCycle(request);
        Result result = results.get(0,0).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testSingleSwapPV01Execution() {

        FunctionServerRequest<IndividualCycleOptions> request =
                FunctionServerRequest.<IndividualCycleOptions>builder()
                        .viewConfig(createViewConfig(OutputNames.PV01))
                        .inputs(_inputs)
                        .cycleOptions(_cycleOptions)
                        .build();

        Results results = _functionServer.executeSingleCycle(request);
        Result result = results.get(0,0).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    @Test(enabled = false)
    public void testCurveBundleExecution() {

        FunctionServerRequest<IndividualCycleOptions> request =
                FunctionServerRequest.<IndividualCycleOptions>builder()
                        .viewConfig(createCurveBundleConfig())
                        .cycleOptions(_cycleOptions)
                        .build();

        Results results = _functionServer.executeSingleCycle(request);
        Result result = results.getNonPortfolioResults().get(CURVE_RESULT).getResult();
        assertThat(result.isSuccess(), is(true));

    }

    /* Output specific view configuration for interest rate swaps */
    private ViewConfig createViewConfig(String output) {

        return
                configureView(
                        "IRS Remote view",
                        column(OutputNames.PRESENT_VALUE,
                                config(
                                        arguments(
                                                function(ConfigDbMarketExposureSelectorFn.class, argument("exposureConfig", _exposureConfig)),
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
                                                        DefaultDiscountingMulticurveBundleFn.class,
                                                        argument("impliedCurveNames", StringSet.of()))),
                                        implementations(
                                                InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
                                                InterestRateSwapCalculatorFactory.class, ThirdPartyInterestRateSwapCalculatorFactory.class)
                                ),
                                output(output, InterestRateSwapSecurity.class)
                        )
                );
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

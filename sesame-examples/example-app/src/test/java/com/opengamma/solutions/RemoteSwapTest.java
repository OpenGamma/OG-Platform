/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.UniqueId;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.server.FunctionServer;
import com.opengamma.sesame.server.FunctionServerRequest;
import com.opengamma.sesame.server.IndividualCycleOptions;
import com.opengamma.sesame.server.RemoteFunctionServer;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Integration tests run against a remote server
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class RemoteSwapTest {

  private static final String URL = "http://localhost:8080/jax";
  private FunctionServer _functionServer;
  private IndividualCycleOptions _cycleOptions;
  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private Results _vanillaResults;
  private Results _spreadResults;
  private Results _fixingResults;
  private Results _compoundingResults;
  private Results _stubResults;

  private static final double STD_TOLERANCE_PV = 1.0E-3;

  @BeforeClass
  public void setUp() {

    _functionServer = new RemoteFunctionServer(URI.create(URL));
    _cycleOptions = IndividualCycleOptions.builder()
        .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
        .marketDataSpec(UserMarketDataSpecification.of(UniqueId.of("DbSnp", "1039")))
        .build();

    _exposureConfig = ConfigLink.resolvable("USD CSA Exposure Functions", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);

    FunctionServerRequest<IndividualCycleOptions> vanillaRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.VANILLA_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _vanillaResults = _functionServer.executeSingleCycle(vanillaRequest);

    FunctionServerRequest<IndividualCycleOptions> spreadRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.SPREAD_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _spreadResults = _functionServer.executeSingleCycle(spreadRequest);

    FunctionServerRequest<IndividualCycleOptions> fixingRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.FIXING_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _fixingResults = _functionServer.executeSingleCycle(fixingRequest);

    FunctionServerRequest<IndividualCycleOptions> compoundingRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.COMPOUNDING_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _compoundingResults = _functionServer.executeSingleCycle(compoundingRequest);

    FunctionServerRequest<IndividualCycleOptions> stubRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.STUB_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _stubResults = _functionServer.executeSingleCycle(stubRequest);

  }

  private ViewConfig createViewConfig() {

    return
        configureView(
            "IRS Remote view",
            RemoteViewSwapUtils.createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                _exposureConfig,
                _currencyMatrixLink),
            RemoteViewSwapUtils.createInterestRateSwapViewColumn(OutputNames.BUCKETED_PV01,
                _exposureConfig,
                _currencyMatrixLink)
        );
  }

  /* Vanilla */
  @Test(enabled = true)
  public void testVanillaFixedVsLiborSwapPV() {

    Result result = _vanillaResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6065111.8810, STD_TOLERANCE_PV)));

  }

  /* Compounding */
  @Test(enabled = true)
  public void testCompoundingFixedVsONSwapPV() {

    Result result = _compoundingResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-5969.7908, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testCompoundingFFAAVsLiborSwapPV() {

    Result result = _compoundingResults.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-1296763.1943162475, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testCompoundingLiborVsLiborSwapPV() {

    //TODO PLAT-6743
    Result result = _compoundingResults.get(2, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-838654.7882962525, STD_TOLERANCE_PV)));

  }

  /* Spread */
  @Test(enabled = true)
  public void testSpreadLiborVsLiborSwapPV() {

    //TODO PLAT-6743
    Result result = _spreadResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(70277.66355337575, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testSpreadFFAAVsLiborSwapPV() {

    //TODO PLAT-6741
    Result result = _spreadResults.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(150891.19165888615, STD_TOLERANCE_PV)));

  }

  /* Fixing */
  @Test(enabled = true)
  public void testFixingFixedVsLiborSwapPV() {

    Result result = _fixingResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(3193775.0940362737, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testFixingFixedVsONSwapPV() {

    Result result = _fixingResults.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-5569.499485016839, STD_TOLERANCE_PV)));

  }

  /* Stubs */
  @Test(enabled = true)
  public void testFixedVsLibor3mStub3MSwapPV() {

    Result result = _stubResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-180869.2122, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testFixedVsLibor3mStub1MSwapPV() {

    Result result = _stubResults.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-259294.50118048675, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testFixedVsLibor6mStub3MSwapPV() {

    Result result = _stubResults.get(2, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-319533.7849, STD_TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void testFixedVsLibor6mStub4MSwapPV() {

    Result result = _stubResults.get(3, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-405631.5512, STD_TOLERANCE_PV)));

  }

}

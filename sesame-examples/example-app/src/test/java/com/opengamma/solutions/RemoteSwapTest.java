/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.net.URI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.UniqueId;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.ResultRow;
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
  private Results _xccyResults;
  private Results _feesResults;
  private Results _singleLegResults;
  private Results _zeroCouponResults;

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

    FunctionServerRequest<IndividualCycleOptions> xccyRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.XCCY_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _xccyResults = _functionServer.executeSingleCycle(xccyRequest);

    FunctionServerRequest<IndividualCycleOptions> feesRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.FEES_INPUT)
            .cycleOptions(_cycleOptions)
            .build();

    _feesResults = _functionServer.executeSingleCycle(feesRequest);

    FunctionServerRequest<IndividualCycleOptions> singleLegRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.SINGLE_LEG_INPUT)
            .cycleOptions(_cycleOptions)
            .build();

    _singleLegResults = _functionServer.executeSingleCycle(singleLegRequest);

    FunctionServerRequest<IndividualCycleOptions> zeroCouponRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewSwapUtils.ZERO_COUPON_COMPOUNDING_INPUT)
            .cycleOptions(_cycleOptions)
            .build();

    _zeroCouponResults = _functionServer.executeSingleCycle(zeroCouponRequest);


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
                                                                 _currencyMatrixLink),
            RemoteViewSwapUtils.createInterestRateSwapViewColumn(OutputNames.PAY_LEG_CASH_FLOWS,
                                                                 _exposureConfig,
                                                                 _currencyMatrixLink),
            RemoteViewSwapUtils.createInterestRateSwapViewColumn(OutputNames.RECEIVE_LEG_CASH_FLOWS,
                                                                 _exposureConfig,
                                                                 _currencyMatrixLink)
        );
  }

  /* Single Leg - start */

  @Test(enabled = true)
  public void testSingleLegSwapPV() {

    Result fixedResult = _singleLegResults.get(0, 0).getResult();
    assertThat(fixedResult.isSuccess(), is(true));
    assertThat(fixedResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount fixedMca = (MultipleCurrencyAmount) fixedResult.getValue();

    Result floatResult = _singleLegResults.get(1, 0).getResult();
    assertThat(floatResult.isSuccess(), is(true));
    assertThat(floatResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount floatMca = (MultipleCurrencyAmount) floatResult.getValue();

    // assert that the two single leg swaps parts equal the whole
    Double combined = fixedMca.getCurrencyAmount(Currency.USD).getAmount() +
        floatMca.getCurrencyAmount(Currency.USD).getAmount();
    assertThat(combined, is(closeTo(6065111.8810, STD_TOLERANCE_PV)));

  }

  /* Single Leg - end */

  @Test(enabled = true)
  public void testZeroCouponSwapPV() {

    Result fixedResult = _zeroCouponResults.get(0, 0).getResult();
    assertThat(fixedResult.isSuccess(), is(true));
    assertThat(fixedResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount amount = (MultipleCurrencyAmount) fixedResult.getValue();
    // TODO - this value has not been derived from an equivalent analytics test
    assertThat(amount.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6598909.63457769, STD_TOLERANCE_PV)));
  }


  /* Fees - start */

  @Test(enabled = true)
  public void testFeesFixedVsLiborSwapPV() {

    Result result = _feesResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6064112.3389, STD_TOLERANCE_PV)));

  }

  /* Fees - end */

  /* Vanilla - start */
  @Test(enabled = true)
  public void testVanillaFixedVsLiborSwapPV() {

    Result result = _vanillaResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6065111.8810, STD_TOLERANCE_PV)));

  }

  /* Vanilla - end */

  /* Compounding -start */

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

  /* Compounding -start */

  /* Spread - start */

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

    //TODO PLAT-6794
    Result result = _spreadResults.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(150128.4091, STD_TOLERANCE_PV)));

  }

  /* Spread - end */

  /* Fixing - start */

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

  /* Fixing - end */

  /* Stubs - start */

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

  @Test(enabled = true)
  public void testFixedVsLibor3mLongStartStub6MSwapPV() {

    //TODO PLAT-6777
    Result result = _stubResults.get(4, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));

  }

  @Test(enabled = true)
  public void testFixedVsLibor6mShortEndStub2MSwapPV() {

    //TODO PLAT-6777
    Result result = _stubResults.get(5, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));

  }

  /* Stubs - end */

  /* XCCY - start */

  @Test(enabled = true)
  public void testLiborUS3mVsLiborBP3mSwapPV() {

    //TODO PLAT-6782
    Result result = _xccyResults.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));

  }

  @Test(enabled = true)
  public void testFixedUSVsLiborBP3mSwapPV() {

    //TODO PLAT-6782
    Result result = _xccyResults.get(1, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));

  }

  /* XCCY - end */

  @Test(enabled = true)
  public void testBuckedPV01() {

    for (ResultRow result : _vanillaResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _spreadResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _fixingResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _compoundingResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _xccyResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _stubResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _feesResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _singleLegResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _zeroCouponResults.getRows()) {
      assertThat(result.get(1).getResult().isSuccess(), is(true));
    }

  }

  @Test(enabled = true)
  public void testPayLegCashFlows() {

    for (ResultRow result : _vanillaResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _spreadResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _fixingResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _compoundingResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _xccyResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _stubResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _feesResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _zeroCouponResults.getRows()) {
      assertThat(result.get(2).getResult().isSuccess(), is(true));
    }
    //TODO PLAT-6796 _singleLegResults

  }

  @Test(enabled = true)
  public void testReceiveLegCashFlows() {

    for (ResultRow result : _vanillaResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _spreadResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _fixingResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _compoundingResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _xccyResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _stubResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _feesResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    for (ResultRow result : _zeroCouponResults.getRows()) {
      assertThat(result.get(3).getResult().isSuccess(), is(true));
    }
    //TODO PLAT-6796 _singleLegResults

  }


}

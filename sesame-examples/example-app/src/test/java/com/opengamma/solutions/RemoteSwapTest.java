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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.RemoteEngine;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests run against a remote server
 * Input: Various flavours of Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value / Bucketed PV01 / Cash flows
 */

@Test(groups = TestGroup.INTEGRATION)
public class RemoteSwapTest {

  private static final double STD_TOLERANCE_PV = 1.0E-3;

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
  private Results _iborCompoundingResults;
  private Results _notionalExchangeResults;

  @BeforeClass
  public void setUp() {
    String property = System.getProperty("server.url");
    String url = property == null ? "http://localhost:8080/jax/" : property;

    RemoteMarketDataSnapshotSource snapshotSource =
        new RemoteMarketDataSnapshotSource(URI.create(url + "components/MarketDataSnapshotSource/default/"));
    ManageableMarketDataSnapshot snapshot = snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
                                                                     "USD_GBP_XCcy_Integration",
                                                                     VersionCorrection.LATEST);

    Engine engine = new RemoteEngine(URI.create(url));
    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());
    CalculationArguments args =
        CalculationArguments.builder()
            .marketDataSpecification(marketDataSpec)
            .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
            .build();

    _exposureConfig = ConfigLink.resolvable("USD-GBP-FF-1", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BBG-Matrix", CurrencyMatrix.class);

    ViewConfig viewConfig = createViewConfig();
    // don't want to provide any data, let the server source it
    MarketDataEnvironment env = MarketDataEnvironmentBuilder.empty();

    _vanillaResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.VANILLA_INPUTS);
    _spreadResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.SPREAD_INPUTS);
    _fixingResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.FIXING_INPUTS);
    _compoundingResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.COMPOUNDING_INPUTS);
    _stubResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.STUB_INPUTS);
    _xccyResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.XCCY_INPUTS);
    _feesResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.FEES_INPUT);
    _singleLegResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.SINGLE_LEG_INPUT);
    _zeroCouponResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.ZERO_COUPON_COMPOUNDING_INPUT);
    _iborCompoundingResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.IBOR_COMPOUNDING_INPUT);
    _notionalExchangeResults = engine.runView(viewConfig, args, env, RemoteViewSwapUtils.NOTIONAL_EXCHANGE_INPUT);
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
                                                                 _currencyMatrixLink));
  }

  /* Single Leg - start */

  @Test
  public void testSingleLegSwapPV() {
    Result fixedResult = _singleLegResults.get(0, 0).getResult();
    assertSuccess(fixedResult);
    assertThat(fixedResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount fixedMca = (MultipleCurrencyAmount) fixedResult.getValue();

    Result floatResult = _singleLegResults.get(1, 0).getResult();
    assertSuccess(floatResult);
    assertThat(floatResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount floatMca = (MultipleCurrencyAmount) floatResult.getValue();

    // assert that the two single leg swaps parts equal the whole
    Double combined = fixedMca.getCurrencyAmount(Currency.USD).getAmount() +
        floatMca.getCurrencyAmount(Currency.USD).getAmount();
    assertThat(combined, is(closeTo(6072234.4631, STD_TOLERANCE_PV)));
  }

  /* Single Leg - end */

  /* Zero Coupon - start */

  @Test
  public void testZeroCouponSwapPV() {
    Result fixedResult = _zeroCouponResults.get(0, 0).getResult();
    assertSuccess(fixedResult);
    assertThat(fixedResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount amount = (MultipleCurrencyAmount) fixedResult.getValue();
    assertThat(amount.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6606079.576346258, STD_TOLERANCE_PV)));
  }

  /* Zero Coupon - end */

  /* Ibor Compounding - start */

  @Test
  public void testIborCompoundingSwapPV() {
    Result fixedResult = _iborCompoundingResults.get(0, 0).getResult();
    assertSuccess(fixedResult);
    assertThat(fixedResult.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    //MultipleCurrencyAmount amount = (MultipleCurrencyAmount) fixedResult.getValue();
    // TODO - this value has not been derived from an equivalent analytics test
    //assertThat(amount.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6598909.63457769, STD_TOLERANCE_PV)));
  }

  /* Ibor Compounding - end */


  /* Fees - start */

  @Test
  public void testFeesFixedVsLiborSwapPV() {
    // Note: not tested v Analytics
    Result result = _feesResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6071234.9209, STD_TOLERANCE_PV)));
  }

  /* Fees - end */

  /* Vanilla - start */

  @Test
  public void testVanillaFixedVsLiborSwapPV() {
    Result result = _vanillaResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(6072234.4631, STD_TOLERANCE_PV)));
  }

  /* Vanilla - end */

  /* Compounding -start */

  @Test
  public void testCompoundingFixedVsONSwapPV() {
    Result result = _compoundingResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-5969.7908, STD_TOLERANCE_PV)));
  }

  @Test
  public void testCompoundingFFAAVsLiborSwapPV() {
    Result result = _compoundingResults.get(1, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-1304207.7900, STD_TOLERANCE_PV)));
  }

  @Test
  public void testCompoundingLiborVsLiborSwapPV() {
    //TODO PLAT-6743 + Check with analytics
    Result result = _compoundingResults.get(2, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-828863.7088161744, STD_TOLERANCE_PV)));
  }

  /* Compounding -start */

  /* Spread - start */

  @Test
  public void testSpreadLiborVsLiborSwapPV() {
    //TODO PLAT-6743 + check with Analytics
    Result result = _spreadResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(80166.8297495842, STD_TOLERANCE_PV)));
  }

  @Test
  public void testSpreadFFAAVsLiborSwapPV() {
    //TODO PLAT-6794
    Result result = _spreadResults.get(1, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(142681.6754, STD_TOLERANCE_PV)));

  }

  /* Spread - end */

  /* Fixing - start */

  @Test
  public void testFixingFixedVsLiborSwapPV() {
    Result result = _fixingResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(3194260.3186, STD_TOLERANCE_PV)));
  }

  @Test
  public void testFixingFixedVsONSwapPV() {
    Result result = _fixingResults.get(1, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-5569.499485016839, STD_TOLERANCE_PV)));
  }

  /* Fixing - end */

  /* Stubs - start */

  @Test
  public void testFixedVsLibor3mStub3MSwapPV() {
    Result result = _stubResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-181665.9361, STD_TOLERANCE_PV)));
  }

  @Test
  public void testFixedVsLibor3mStub1MSwapPV() {
    Result result = _stubResults.get(1, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-262948.9316, STD_TOLERANCE_PV)));
  }

  @Test
  public void testFixedVsLibor6mStub3MSwapPV() {
    Result result = _stubResults.get(2, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-318570.8721, 10*STD_TOLERANCE_PV))); // <- ???
  }

  @Test
  public void testFixedVsLibor6mStub4MSwapPV() {
    Result result = _stubResults.get(3, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-406168.2802, 10*STD_TOLERANCE_PV))); // <- ???
  }

  @Test
  public void testFixedVsLibor3mLongStartStub6MSwapPV() {
    //TODO PLAT-6777
    Result result = _stubResults.get(4, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
  }

  @Test
  public void testFixedVsLibor6mShortEndStub2MSwapPV() {
    //TODO PLAT-6777
    Result result = _stubResults.get(5, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
  }
  /* Stubs - end */

  /* XCCY - start */

  @Test
  public void testLiborUS3mVsLiborBP3mSwapPV() {
    //TODO PLAT-6782
    Result result = _xccyResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(20382165.8257, STD_TOLERANCE_PV)));
    assertThat(mca.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(-8303201.9931, 50.0)));
  }

  @Test
  public void testFixedUSVsLiborBP3mSwapPV() {
    //TODO PLAT-6782
    Result result = _xccyResults.get(1, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-19913853.4812, STD_TOLERANCE_PV)));
    assertThat(mca.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(8303201.9931, 50.0)));
  }

  /* XCCY - end */

  /* Notional Exchange - start */

  @Test
  public void testInitialNotionalExchangeSwapPV() {
    //TODO PLAT-6807
    Result result = _notionalExchangeResults.get(0, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(80085298.7823, STD_TOLERANCE_PV)));
    assertThat(mca.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(-53295535.6962, STD_TOLERANCE_PV)));
  }

  @Test
  public void testFinalNotionalExchangeSwapPV() {
    //TODO PLAT-6807
    Result result = _notionalExchangeResults.get(1, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-107119236.1726, STD_TOLERANCE_PV)));
    assertThat(mca.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(62611433.5861, STD_TOLERANCE_PV)));
  }

  @Test
  public void testInitialFinalNotionalExchangeSwapPV() {
    Result result = _notionalExchangeResults.get(2, 0).getResult();
    assertSuccess(result);
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-7120083.9091, STD_TOLERANCE_PV)));
    assertThat(mca.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(1012655.4392, 1.0)));
  }

  /* Notional Exchange - end */

  @Test
  public void testBuckedPV01() {
    // TODO: test results value
    for (ResultRow result : _vanillaResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _spreadResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _fixingResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _compoundingResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _xccyResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _stubResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _feesResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _singleLegResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _zeroCouponResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _iborCompoundingResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
    for (ResultRow result : _notionalExchangeResults.getRows()) {
      assertSuccess(result.get(1).getResult());
    }
  }

  @Test
  public void testPayLegCashFlows() {
    for (ResultRow result : _vanillaResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _spreadResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _fixingResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _compoundingResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _xccyResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _stubResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _feesResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _zeroCouponResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _iborCompoundingResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _notionalExchangeResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
    for (ResultRow result : _singleLegResults.getRows()) {
      assertSuccess(result.get(2).getResult());
    }
  }

  @Test
  public void testReceiveLegCashFlows() {
    for (ResultRow result : _vanillaResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _spreadResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _fixingResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _compoundingResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _xccyResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _stubResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _feesResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _zeroCouponResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _iborCompoundingResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _notionalExchangeResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
    for (ResultRow result : _singleLegResults.getRows()) {
      assertSuccess(result.get(3).getResult());
    }
  }
}

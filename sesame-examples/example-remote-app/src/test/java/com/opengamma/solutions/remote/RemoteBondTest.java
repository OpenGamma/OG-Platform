/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
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
import com.opengamma.solutions.util.BondViewUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Integration tests run against a remote server
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION)
public class RemoteBondTest {

  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PRICE = 1.0E-6;
  private static final double TOLERANCE_RATE = 1.0E-6;

  private ConfigLink<ExposureFunctions> _exposureConfigOis;
  private ConfigLink<ExposureFunctions> _exposureConfigUkGovt;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private Results _bondResults;

  @BeforeClass
  public void setUp() {
    String url = Objects.firstNonNull(System.getProperty("server.url"), RemoteTestUtils.LOCALHOST);

    RemoteServer server = RemoteServer.create(url);
    MarketDataSnapshotSource snapshotSource = server.getMarketDataSnapshotSource();
    ManageableMarketDataSnapshot snapshot = snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
                                                                     "GBP_Bond_Integration",
                                                                     VersionCorrection.LATEST);

    ViewRunner viewRunner = new RemoteViewRunner(URI.create(url));
    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());

    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .valuationTime(DateUtils.getUTCDate(2014, 7, 11))
            .marketDataSpecification(marketDataSpec)
            .build();

    _exposureConfigOis = ConfigLink.resolvable(RemoteTestUtils.OIS_EXPOSURE, ExposureFunctions.class);
    _exposureConfigUkGovt = ConfigLink.resolvable(RemoteTestUtils.UK_GOV_EXPOSURE, ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable(RemoteTestUtils.CURRENCY_MATRIX, CurrencyMatrix.class);


    // don't want to provide any data, let the server source it
    MarketDataEnvironment marketDataEnvironment = MarketDataEnvironmentBuilder.empty();
    List<Object> trades = BondViewUtils.BOND_INPUTS;
    ViewConfig viewConfig = createViewConfig();

    _bondResults = viewRunner.runView(viewConfig, calculationArguments, marketDataEnvironment, trades);
  }

  private ViewConfig createViewConfig() {

    return
        configureView(
            "Bond Remote view",
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                               OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 0
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.PRESENT_VALUE_CURVES,
                                               OutputNames.PRESENT_VALUE_CURVES,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 1
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.PRESENT_VALUE_YIELD,
                                               OutputNames.PRESENT_VALUE_YIELD,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 2
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.CLEAN_PRICE_MARKET,
                                               OutputNames.CLEAN_PRICE_MARKET,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 3
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.CLEAN_PRICE_CURVES,
                                               OutputNames.CLEAN_PRICE_CURVES,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 4
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.CLEAN_PRICE_YIELD,
                                               OutputNames.CLEAN_PRICE_YIELD,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 5
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.YIELD_TO_MATURITY_CLEAN_PRICE,
                                               OutputNames.YIELD_TO_MATURITY_CLEAN_PRICE,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 6
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.YIELD_TO_MATURITY_CURVES,
                                               OutputNames.YIELD_TO_MATURITY_CURVES,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 7
            BondViewUtils.createBondViewColumn("OIS " + OutputNames.YIELD_TO_MATURITY_MARKET,
                                               OutputNames.YIELD_TO_MATURITY_MARKET,
                                               _exposureConfigOis,
                                               _currencyMatrixLink), // 8
            BondViewUtils.createBondViewColumn("Govt " + OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                               OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                               _exposureConfigUkGovt,
                                               _currencyMatrixLink), // 9
            BondViewUtils.createBondViewColumn("Govt " + OutputNames.PRESENT_VALUE_CURVES,
                                               OutputNames.PRESENT_VALUE_CURVES,
                                               _exposureConfigUkGovt,
                                               _currencyMatrixLink), // 10
            BondViewUtils.createBondViewColumn("Govt " + OutputNames.PRESENT_VALUE_YIELD,
                                               OutputNames.PRESENT_VALUE_YIELD,
                                               _exposureConfigUkGovt,
                                               _currencyMatrixLink) // 11
        );
  }

  @Test(enabled = true)
  public void bondSpotPv() {
    
    double pvFromCleanPrice = 4095717.0907;
    Result<?> resultPvPriceOis = _bondResults.get(0, 0).getResult(); // From clean price with OIS curve
    assertSuccess(resultPvPriceOis);
    assertThat(resultPvPriceOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceOis = (MultipleCurrencyAmount) resultPvPriceOis.getValue();
    assertThat(mcaPvPriceOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    Result resultPvPriceGovt = _bondResults.get(0, 9).getResult(); // From clean price with Govt curve
    assertSuccess(resultPvPriceGovt);
    assertThat(resultPvPriceGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceGovt = (MultipleCurrencyAmount) resultPvPriceGovt.getValue();
    assertThat(mcaPvPriceGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    
    Result resultPvCurve = _bondResults.get(0, 1).getResult();
    assertSuccess(resultPvCurve);
    assertThat(resultPvCurve.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvCurve = (MultipleCurrencyAmount) resultPvCurve.getValue();
    assertThat(mcaPvCurve.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4079490.1191, TOLERANCE_PV)));

    double pvFromYield = 4100927.5796;
    Result resultPvYieldOis = _bondResults.get(0, 2).getResult();
    assertSuccess(resultPvYieldOis);
    assertThat(resultPvYieldOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldOis = (MultipleCurrencyAmount) resultPvYieldOis.getValue();
    assertThat(mcaPvYieldOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));
    Result resultPvYieldGovt = _bondResults.get(0, 11).getResult();
    assertSuccess(resultPvYieldGovt);
    assertThat(resultPvYieldGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldGovt = (MultipleCurrencyAmount) resultPvYieldGovt.getValue();
    assertThat(mcaPvYieldGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void bondSettledPv() {
    
    double pvFromCleanPrice = 14080385.3611;
    Result resultPvPriceOis = _bondResults.get(1, 0).getResult(); // From clean price with OIS curve
    assertSuccess(resultPvPriceOis);
    assertThat(resultPvPriceOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceOis = (MultipleCurrencyAmount) resultPvPriceOis.getValue();
    assertThat(mcaPvPriceOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    Result resultPvPriceGovt = _bondResults.get(1, 9).getResult(); // From clean price with Govt curve
    assertSuccess(resultPvPriceGovt);
    assertThat(resultPvPriceGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceGovt = (MultipleCurrencyAmount) resultPvPriceGovt.getValue();
    assertThat(mcaPvPriceGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    
    Result resultPvCurve = _bondResults.get(1, 1).getResult();
    assertSuccess(resultPvCurve);
    assertThat(resultPvCurve.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvCurve = (MultipleCurrencyAmount) resultPvCurve.getValue();
    assertThat(mcaPvCurve.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(14064158.3895, TOLERANCE_PV)));

    double pvFromYield = 14085595.8500;
    Result resultPvYieldOis = _bondResults.get(1, 2).getResult();
    assertSuccess(resultPvYieldOis);
    assertThat(resultPvYieldOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldOis = (MultipleCurrencyAmount) resultPvYieldOis.getValue();
    assertThat(mcaPvYieldOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));
    Result resultPvYieldGovt = _bondResults.get(1, 11).getResult();
    assertSuccess(resultPvYieldGovt);
    assertThat(resultPvYieldGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldGovt = (MultipleCurrencyAmount) resultPvYieldGovt.getValue();
    assertThat(mcaPvYieldGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void bondFwdPv() {
    
    double pvFromCleanPrice = 4077089.9463;
    Result resultPvPriceOis = _bondResults.get(2, 0).getResult(); // From clean price with OIS curve
    assertSuccess(resultPvPriceOis);
    assertThat(resultPvPriceOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceOis = (MultipleCurrencyAmount) resultPvPriceOis.getValue();
    assertThat(mcaPvPriceOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    Result resultPvPriceGovt = _bondResults.get(2, 9).getResult(); // From clean price with Govt curve
    assertSuccess(resultPvPriceGovt);
    assertThat(resultPvPriceGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceGovt = (MultipleCurrencyAmount) resultPvPriceGovt.getValue();
    assertThat(mcaPvPriceGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    
    Result resultPvCurve = _bondResults.get(2, 1).getResult();
    assertSuccess(resultPvCurve);
    assertThat(resultPvCurve.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvCurve = (MultipleCurrencyAmount) resultPvCurve.getValue();
    assertThat(mcaPvCurve.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4060862.9747, TOLERANCE_PV)));

    double pvFromYield = 4082300.4352;
    Result resultPvYieldOis = _bondResults.get(2, 2).getResult();
    assertSuccess(resultPvYieldOis);
    assertThat(resultPvYieldOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldOis = (MultipleCurrencyAmount) resultPvYieldOis.getValue();
    assertThat(mcaPvYieldOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));
    Result resultPvYieldGovt = _bondResults.get(2, 11).getResult();
    assertSuccess(resultPvYieldGovt);
    assertThat(resultPvYieldGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldGovt = (MultipleCurrencyAmount) resultPvYieldGovt.getValue();
    assertThat(mcaPvYieldGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));

  }
  
  @Test(enabled = true)
  public void testBondPrice() {

    double marketPrice = 1.40;
    double priceFromCurvesExpected = 1.39837725;
    double priceFromYieldExpected = 1.4005210670;
    
    Result resultPriceMarket = _bondResults.get(0, 3).getResult();
    assertSuccess(resultPriceMarket);
    assertThat(resultPriceMarket.getValue(), is(instanceOf(Double.class)));
    Double priceMarket = (Double) resultPriceMarket.getValue();
    assertThat(priceMarket, is(closeTo(marketPrice, TOLERANCE_PRICE)));
    
    Result resultPriceCurve = _bondResults.get(0, 4).getResult();
    assertSuccess(resultPriceCurve);
    assertThat(resultPriceCurve.getValue(), is(instanceOf(Double.class)));
    Double priceCurve = (Double) resultPriceCurve.getValue();
    assertThat(priceCurve, is(closeTo(priceFromCurvesExpected, TOLERANCE_PRICE)));
    
    Result resultPriceYield = _bondResults.get(0, 5).getResult();
    assertSuccess(resultPriceYield);
    assertThat(resultPriceYield.getValue(), is(instanceOf(Double.class)));
    Double priceYield = (Double) resultPriceYield.getValue();
    assertThat(priceYield, is(closeTo(priceFromYieldExpected, TOLERANCE_PRICE)));
    
  }
  
  @Test(enabled = true)
  public void testBondYield() {

    double yieldFromPriceExpected = 0.0180659508;
    double yieldFromCurvesExpected = 0.0182715311;
    double marketYield = 0.018;
    
    Result resultYieldPrice = _bondResults.get(0, 6).getResult();
    assertSuccess(resultYieldPrice);
    assertThat(resultYieldPrice.getValue(), is(instanceOf(Double.class)));
    Double yieldPrice = (Double) resultYieldPrice.getValue();
    assertThat(yieldPrice, is(closeTo(yieldFromPriceExpected, TOLERANCE_RATE)));
    
    Result resultYieldCurve = _bondResults.get(0, 7).getResult();
    assertSuccess(resultYieldCurve);
    assertThat(resultYieldCurve.getValue(), is(instanceOf(Double.class)));
    Double yieldCurve = (Double) resultYieldCurve.getValue();
    assertThat(yieldCurve, is(closeTo(yieldFromCurvesExpected, TOLERANCE_RATE)));
    
    Result resultYieldMarket = _bondResults.get(0, 8).getResult();
    assertSuccess(resultYieldMarket);
    assertThat(resultYieldMarket.getValue(), is(instanceOf(Double.class)));
    Double yieldMarket = (Double) resultYieldMarket.getValue();
    assertThat(yieldMarket, is(closeTo(marketYield, TOLERANCE_RATE)));
    
  }

  @Test(enabled = true)
  public void bondPvGovtCurve() {
    
    Result resultPvSpot = _bondResults.get(0, 10).getResult();
    assertSuccess(resultPvSpot);
    assertThat(resultPvSpot.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvSpot = (MultipleCurrencyAmount) resultPvSpot.getValue();
    assertThat(mcaPvSpot.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4045194.6254, TOLERANCE_PV)));
    
    Result resultPvPast = _bondResults.get(1, 10).getResult();
    assertSuccess(resultPvPast);
    assertThat(resultPvPast.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPast = (MultipleCurrencyAmount) resultPvPast.getValue();
    assertThat(mcaPvPast.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(14029862.8957, TOLERANCE_PV)));
    
    Result resultPvFwd = _bondResults.get(2, 10).getResult();
    assertSuccess(resultPvFwd);
    assertThat(resultPvFwd.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvFwd = (MultipleCurrencyAmount) resultPvFwd.getValue();
    assertThat(mcaPvFwd.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4026567.4809, TOLERANCE_PV)));
    
  }
}

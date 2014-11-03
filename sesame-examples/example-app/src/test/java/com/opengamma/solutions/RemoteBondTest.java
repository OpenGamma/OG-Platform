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

import com.opengamma.component.tool.ToolContextUtils;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.security.SecurityMaster;
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

/**
 * Integration tests run against a remote server
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class RemoteBondTest {

  private FunctionServer _functionServer;
  private IndividualCycleOptions _cycleOptions;
  private ConfigLink<ExposureFunctions> _exposureConfigOis;
  private ConfigLink<ExposureFunctions> _exposureConfigUkGovt;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private Results _bondResults;

  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PRICE = 1.0E-6;
  private static final double TOLERANCE_RATE = 1.0E-6;

  @BeforeClass
  public void setUp() {

    String property = System.getProperty("server.url");
    String url = property == null ? "http://localhost:8080/jax" : property;

    _functionServer = new RemoteFunctionServer(URI.create(url));
    _cycleOptions = IndividualCycleOptions.builder()
        .valuationTime(DateUtils.getUTCDate(2014, 7, 11))
        .marketDataSpec(UserMarketDataSpecification.of(UniqueId.of("DbSnp", "1001")))
        .build();

    _exposureConfigOis = ConfigLink.resolvable("GBP_SO_DSCONISCCY-OIS", ExposureFunctions.class);
    _exposureConfigUkGovt = ConfigLink.resolvable("GBP_SO_DSCON-OIS_ISCCY-UKGVT", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BBG-Matrix", CurrencyMatrix.class);



    FunctionServerRequest<IndividualCycleOptions> bondRequest =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewBondUtils.BOND_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _bondResults = _functionServer.executeSingleCycle(bondRequest);

  }

  private ViewConfig createViewConfig() {

    return
        configureView(
            "Bond Remote view",
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.PRESENT_VALUE_CLEAN_PRICE, 
                                                     OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 0
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.PRESENT_VALUE_CURVES,
                                                     OutputNames.PRESENT_VALUE_CURVES,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 1
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.PRESENT_VALUE_YIELD,
                                                     OutputNames.PRESENT_VALUE_YIELD,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 2
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.CLEAN_PRICE_MARKET,
                                                     OutputNames.CLEAN_PRICE_MARKET,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 3
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.CLEAN_PRICE_CURVES,
                                                     OutputNames.CLEAN_PRICE_CURVES,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 4
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.CLEAN_PRICE_YIELD,
                                                     OutputNames.CLEAN_PRICE_YIELD,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 5
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.YIELD_TO_MATURITY_CLEAN_PRICE,
                                                     OutputNames.YIELD_TO_MATURITY_CLEAN_PRICE,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 6
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.YIELD_TO_MATURITY_CURVES,
                                                     OutputNames.YIELD_TO_MATURITY_CURVES,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 7
            RemoteViewBondUtils.createBondViewColumn("OIS " + OutputNames.YIELD_TO_MATURITY_MARKET,
                                                     OutputNames.YIELD_TO_MATURITY_MARKET,
                                                     _exposureConfigOis,
                                                     _currencyMatrixLink), // 8
            RemoteViewBondUtils.createBondViewColumn("Govt " + OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                                     OutputNames.PRESENT_VALUE_CLEAN_PRICE,
                                                     _exposureConfigUkGovt,
                                                     _currencyMatrixLink), // 9
            RemoteViewBondUtils.createBondViewColumn("Govt " + OutputNames.PRESENT_VALUE_CURVES,
                                                     OutputNames.PRESENT_VALUE_CURVES,
                                                     _exposureConfigUkGovt,
                                                     _currencyMatrixLink), // 10
            RemoteViewBondUtils.createBondViewColumn("Govt " + OutputNames.PRESENT_VALUE_YIELD,
                                                     OutputNames.PRESENT_VALUE_YIELD,
                                                     _exposureConfigUkGovt,
                                                     _currencyMatrixLink) // 11
        );
  }

  @Test(enabled = true)
  public void bondSpotPv() {
    
    double pvFromCleanPrice = 4095717.0907;
    Result<?> resultPvPriceOis = _bondResults.get(0, 0).getResult(); // From clean price with OIS curve
    assertThat(resultPvPriceOis.isSuccess(), is(true));
    assertThat(resultPvPriceOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceOis = (MultipleCurrencyAmount) resultPvPriceOis.getValue();
    assertThat(mcaPvPriceOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    Result resultPvPriceGovt = _bondResults.get(0, 9).getResult(); // From clean price with Govt curve
    assertThat(resultPvPriceGovt.isSuccess(), is(true));
    assertThat(resultPvPriceGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceGovt = (MultipleCurrencyAmount) resultPvPriceGovt.getValue();
    assertThat(mcaPvPriceGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    
    Result resultPvCurve = _bondResults.get(0, 1).getResult();
    assertThat(resultPvCurve.isSuccess(), is(true));
    assertThat(resultPvCurve.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvCurve = (MultipleCurrencyAmount) resultPvCurve.getValue();
    assertThat(mcaPvCurve.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4079490.1191, TOLERANCE_PV)));

    double pvFromYield = 4100927.5796;
    Result resultPvYieldOis = _bondResults.get(0, 2).getResult();
    assertThat(resultPvYieldOis.isSuccess(), is(true));
    assertThat(resultPvYieldOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldOis = (MultipleCurrencyAmount) resultPvYieldOis.getValue();
    assertThat(mcaPvYieldOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));
    Result resultPvYieldGovt = _bondResults.get(0, 11).getResult();
    assertThat(resultPvYieldGovt.isSuccess(), is(true));
    assertThat(resultPvYieldGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldGovt = (MultipleCurrencyAmount) resultPvYieldGovt.getValue();
    assertThat(mcaPvYieldGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void bondSettledPv() {
    
    double pvFromCleanPrice = 14080385.3611;
    Result resultPvPriceOis = _bondResults.get(1, 0).getResult(); // From clean price with OIS curve
    assertThat(resultPvPriceOis.isSuccess(), is(true));
    assertThat(resultPvPriceOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceOis = (MultipleCurrencyAmount) resultPvPriceOis.getValue();
    assertThat(mcaPvPriceOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    Result resultPvPriceGovt = _bondResults.get(1, 9).getResult(); // From clean price with Govt curve
    assertThat(resultPvPriceGovt.isSuccess(), is(true));
    assertThat(resultPvPriceGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceGovt = (MultipleCurrencyAmount) resultPvPriceGovt.getValue();
    assertThat(mcaPvPriceGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    
    Result resultPvCurve = _bondResults.get(1, 1).getResult();
    assertThat(resultPvCurve.isSuccess(), is(true));
    assertThat(resultPvCurve.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvCurve = (MultipleCurrencyAmount) resultPvCurve.getValue();
    assertThat(mcaPvCurve.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(14064158.3895, TOLERANCE_PV)));

    double pvFromYield = 14085595.8500;
    Result resultPvYieldOis = _bondResults.get(1, 2).getResult();
    assertThat(resultPvYieldOis.isSuccess(), is(true));
    assertThat(resultPvYieldOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldOis = (MultipleCurrencyAmount) resultPvYieldOis.getValue();
    assertThat(mcaPvYieldOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));
    Result resultPvYieldGovt = _bondResults.get(1, 11).getResult();
    assertThat(resultPvYieldGovt.isSuccess(), is(true));
    assertThat(resultPvYieldGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldGovt = (MultipleCurrencyAmount) resultPvYieldGovt.getValue();
    assertThat(mcaPvYieldGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));

  }

  @Test(enabled = true)
  public void bondFwdPv() {
    
    double pvFromCleanPrice = 4077089.9463;
    Result resultPvPriceOis = _bondResults.get(2, 0).getResult(); // From clean price with OIS curve
    assertThat(resultPvPriceOis.isSuccess(), is(true));
    assertThat(resultPvPriceOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceOis = (MultipleCurrencyAmount) resultPvPriceOis.getValue();
    assertThat(mcaPvPriceOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    Result resultPvPriceGovt = _bondResults.get(2, 9).getResult(); // From clean price with Govt curve
    assertThat(resultPvPriceGovt.isSuccess(), is(true));
    assertThat(resultPvPriceGovt.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPriceGovt = (MultipleCurrencyAmount) resultPvPriceGovt.getValue();
    assertThat(mcaPvPriceGovt.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromCleanPrice, TOLERANCE_PV)));
    
    Result resultPvCurve = _bondResults.get(2, 1).getResult();
    assertThat(resultPvCurve.isSuccess(), is(true));
    assertThat(resultPvCurve.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvCurve = (MultipleCurrencyAmount) resultPvCurve.getValue();
    assertThat(mcaPvCurve.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4060862.9747, TOLERANCE_PV)));

    double pvFromYield = 4082300.4352;
    Result resultPvYieldOis = _bondResults.get(2, 2).getResult();
    assertThat(resultPvYieldOis.isSuccess(), is(true));
    assertThat(resultPvYieldOis.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvYieldOis = (MultipleCurrencyAmount) resultPvYieldOis.getValue();
    assertThat(mcaPvYieldOis.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(pvFromYield, TOLERANCE_PV)));
    Result resultPvYieldGovt = _bondResults.get(2, 11).getResult();
    assertThat(resultPvYieldGovt.isSuccess(), is(true));
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
    assertThat(resultPriceMarket.isSuccess(), is(true));
    assertThat(resultPriceMarket.getValue(), is(instanceOf(Double.class)));
    Double priceMarket = (Double) resultPriceMarket.getValue();
    assertThat(priceMarket, is(closeTo(marketPrice, TOLERANCE_PRICE)));
    
    Result resultPriceCurve = _bondResults.get(0, 4).getResult();
    assertThat(resultPriceCurve.isSuccess(), is(true));
    assertThat(resultPriceCurve.getValue(), is(instanceOf(Double.class)));
    Double priceCurve = (Double) resultPriceCurve.getValue();
    assertThat(priceCurve, is(closeTo(priceFromCurvesExpected, TOLERANCE_PRICE)));
    
    Result resultPriceYield = _bondResults.get(0, 5).getResult();
    assertThat(resultPriceYield.isSuccess(), is(true));
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
    assertThat(resultYieldPrice.isSuccess(), is(true));
    assertThat(resultYieldPrice.getValue(), is(instanceOf(Double.class)));
    Double yieldPrice = (Double) resultYieldPrice.getValue();
    assertThat(yieldPrice, is(closeTo(yieldFromPriceExpected, TOLERANCE_RATE)));
    
    Result resultYieldCurve = _bondResults.get(0, 7).getResult();
    assertThat(resultYieldCurve.isSuccess(), is(true));
    assertThat(resultYieldCurve.getValue(), is(instanceOf(Double.class)));
    Double yieldCurve = (Double) resultYieldCurve.getValue();
    assertThat(yieldCurve, is(closeTo(yieldFromCurvesExpected, TOLERANCE_RATE)));
    
    Result resultYieldMarket = _bondResults.get(0, 8).getResult();
    assertThat(resultYieldMarket.isSuccess(), is(true));
    assertThat(resultYieldMarket.getValue(), is(instanceOf(Double.class)));
    Double yieldMarket = (Double) resultYieldMarket.getValue();
    assertThat(yieldMarket, is(closeTo(marketYield, TOLERANCE_RATE)));
    
  }

  @Test(enabled = true)
  public void bondPvGovtCurve() {
    
    Result resultPvSpot = _bondResults.get(0, 10).getResult();
    assertThat(resultPvSpot.isSuccess(), is(true));
    assertThat(resultPvSpot.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvSpot = (MultipleCurrencyAmount) resultPvSpot.getValue();
    assertThat(mcaPvSpot.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4045194.6254, TOLERANCE_PV)));
    
    Result resultPvPast = _bondResults.get(1, 10).getResult();
    assertThat(resultPvPast.isSuccess(), is(true));
    assertThat(resultPvPast.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvPast = (MultipleCurrencyAmount) resultPvPast.getValue();
    assertThat(mcaPvPast.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(14029862.8957, TOLERANCE_PV)));
    
    Result resultPvFwd = _bondResults.get(2, 10).getResult();
    assertThat(resultPvFwd.isSuccess(), is(true));
    assertThat(resultPvFwd.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mcaPvFwd = (MultipleCurrencyAmount) resultPvFwd.getValue();
    assertThat(mcaPvFwd.getCurrencyAmount(Currency.GBP).getAmount(), is(closeTo(4026567.4809, TOLERANCE_PV)));
    
  }

}

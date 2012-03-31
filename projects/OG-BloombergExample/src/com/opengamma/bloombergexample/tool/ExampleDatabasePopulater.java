/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.tool;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.component.BloombergTimeSeriesUpdateTool;
import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.bbg.loader.BloombergSecurityLoader;
import com.opengamma.bbg.tool.BloombergToolContext;
import com.opengamma.bloombergexample.generator.PortfolioGeneratorTool;
import com.opengamma.bloombergexample.loader.*;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.volatility.surface.FXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;

/**
 * Single class that populates the database with data for running the example server.
 * <p>
 * It is designed to run against the HSQLDB example database.  
 */
public class ExampleDatabasePopulater extends AbstractExampleTool {

  public static final String EXAMPLE_FX_PORTFOLIO = "Example FX Portfolio";

  /**
   * The currencies.
   */
  private static final Set<Currency> s_currencies = Sets.newHashSet(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.AUD, Currency.CAD);
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabasePopulater.class);

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleDatabasePopulater().initAndRun(args);
    System.exit(0);
  }

  private Set<ExternalId> _curveNodesExternalIds;

  private Set<ExternalId> _initialRateExternalIds;

  private Set<ExternalIdBundle> _futuresExternalIds;

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected void doRun() {
    Set<ExternalId> eurUsdId = ImmutableSet.of(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "EURUSD Curncy"));

    loadCurveNodeHistoricalData();
    loadFutures(_futuresExternalIds);
    loadTimeSeriesRating();
    loadEquityPortfolio();
    loadHistoricalData(_curveNodesExternalIds, _initialRateExternalIds, eurUsdId);
    loadVolSurfaceData();
    loadSwapPortfolio();
    loadFXPortfolio();
    loadMultiCurrencySwapPortfolio();
    //loadLiborRawSecurities();
    //loadMixedPortfolio();
    loadEquityOptionPortfolio();
    loadViews();       
  }
  
  private void loadEquityOptionPortfolio(){
    DemoEquityOptionCollarPortfolioLoader loader = new DemoEquityOptionCollarPortfolioLoader();    
    loader.setNumOptions(2);
    loader.setNumMembers(8);
    loader.setNumContracts(new BigDecimal(500));
    loader.run(getToolContext());    
  }

  private void loadCurveNodeHistoricalData() {
    CurveNodeHistoricalDataLoader curveNodeHistoricalDataLoader = new CurveNodeHistoricalDataLoader();
    System.out.println("Loading curve node historical data");
    curveNodeHistoricalDataLoader.run(getToolContext());
    _curveNodesExternalIds = curveNodeHistoricalDataLoader.getCurveNodesExternalIds();
    _initialRateExternalIds = curveNodeHistoricalDataLoader.getInitialRateExternalIds();
    _futuresExternalIds = curveNodeHistoricalDataLoader.getFuturesExternalIds();
    System.out.println("Finished");
  }

  private void loadVolSurfaceData() {
    FXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(getToolContext().getConfigMaster());
  }

  private void loadMixedPortfolio() {
    ExampleMixedPortfolioLoader mixedPortfolioLoader = new ExampleMixedPortfolioLoader();
    System.out.println("Creating example mixed portfolio");
    mixedPortfolioLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadTimeSeriesRating() {
    ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
    System.out.println("Creating Timeseries configuration");
    timeSeriesRatingLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadFutures(Set<ExternalIdBundle> identifiers) {
    SecurityMaster secMaster = getToolContext().getSecurityMaster();
    ReferenceDataProvider referenceDataProvider = ((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider();
    ExchangeDataProvider exchangeDataProvider = new DefaultExchangeDataProvider();
    BloombergBulkSecurityLoader bulkSecurityLoader = new BloombergBulkSecurityLoader(referenceDataProvider, exchangeDataProvider);
    BloombergSecurityLoader securityLoader = new BloombergSecurityLoader(secMaster, bulkSecurityLoader);
    securityLoader.loadSecurity(identifiers);
  }

  private void loadHistoricalData(Set<ExternalId>... externalIdSets) {
    if (!(getToolContext() instanceof BloombergToolContext)) {
      throw new OpenGammaRuntimeException("The " + BloombergTimeSeriesUpdateTool.class.getSimpleName() +
        " requires a tool context which implements " + BloombergToolContext.class.getName());
    }
    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(
      getToolContext().getHistoricalTimeSeriesMaster(),
      ((BloombergToolContext) getToolContext()).getBloombergHistoricalTimeSeriesSource(),
      new BloombergIdentifierProvider(((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider()));
    loader.setReload(true);

    Collection<EquitySecurity> securities = readEquitySecurities();
    for (final EquitySecurity security : securities) {
      loader.addTimeSeries(new HashSet<ExternalId>() {{
        add(security.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_TICKER));
      }}, "CMPL", "PX_LAST", null, null);
    }
    for (Set<ExternalId> externalIds : externalIdSets) {
      if (externalIds.size() > 0) {
        loader.addTimeSeries(externalIds, "CMPL", "PX_LAST", null, null);
      }
    }
    System.out.println("Finished");
  }

  private void loadEquityPortfolio() {
    ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
    System.out.println("Creating example equity portfolio");
    equityLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadSwapPortfolio() {
    ExampleSwapPortfolioLoader swapLoader = new ExampleSwapPortfolioLoader();
    System.out.println("Creating example swap portfolio");
    swapLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadMultiCurrencySwapPortfolio() {
    ExampleMultiCurrencySwapPortfolioLoader multiCurrSwapLoader = new ExampleMultiCurrencySwapPortfolioLoader();
    System.out.println("Creating example multi currency swap portfolio");
    multiCurrSwapLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadFXPortfolio() {
    PortfolioGeneratorTool generator = new PortfolioGeneratorTool();
    System.out.println("Creating FX portfolio");
    generator.run(getToolContext(), "Example FX Portfolio", "EuroDollarFX", true);
    System.out.println("Finished");
  }

  private void loadLiborRawSecurities() {
    System.out.println("Creating libor raw securities");
    PortfolioLoaderHelper.persistLiborRawSecurities(getAllCurrencies(), getToolContext());
    System.out.println("Finished");
  }

  private void loadViews() {
    ExampleViewsPopulater populator = new ExampleViewsPopulater();
    System.out.println("Creating example view definitions");
    populator.run(getToolContext());
    System.out.println("Finished");
  }

  private static Set<Currency> getAllCurrencies() {
    return s_currencies;
  }

}

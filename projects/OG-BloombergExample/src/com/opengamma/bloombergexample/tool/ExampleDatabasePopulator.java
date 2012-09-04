/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.tool;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.bbg.loader.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.bbg.loader.BloombergSecurityLoader;
import com.opengamma.bloombergexample.generator.BloombergExamplePortfolioGeneratorTool;
import com.opengamma.bloombergexample.loader.CurveNodeHistoricalDataLoader;
import com.opengamma.bloombergexample.loader.DemoEquityOptionCollarPortfolioLoader;
import com.opengamma.bloombergexample.loader.ExampleEquityPortfolioLoader;
import com.opengamma.bloombergexample.loader.ExampleMultiCurrencySwapPortfolioLoader;
import com.opengamma.bloombergexample.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.bloombergexample.loader.ExampleViewsPopulator;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.FXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.money.Currency;

/**
 * Single class that populates the database with data for running the example server.
 * <p>
 * It is designed to run against the HSQLDB example database.  
 */
@Scriptable
public class ExampleDatabasePopulator extends AbstractTool<IntegrationToolContext> {

  /**
   * Example configuration for tools.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:toolcontext/toolcontext-bloombergexample.properties";

  /**
   * The name of the generated example FX portfolio.
   */
  public static final String FX_PORTFOLIO_NAME = "FX Portfolio";
  
  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "Multi-currency Swap Portfolio";
  
  /**
   * The currencies.
   */
  private static final Currency[] s_currencies = new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.AUD, Currency.CAD};
  

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleDatabasePopulator().initAndRun(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null, IntegrationToolContext.class);
    System.exit(0);
  }

  private Set<ExternalId> _curveNodesExternalIds;

  private Set<ExternalId> _initialRateExternalIds;

  private Set<ExternalIdBundle> _futuresExternalIds;

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected void doRun() {
    Set<ExternalId> eurUsdId = ImmutableSet.of(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "EURUSD Curncy"));

    loadCurveNodeHistoricalData();
    loadFutures(_futuresExternalIds);
    loadTimeSeriesRating();
    loadEquityPortfolio();
    loadHistoricalData(_curveNodesExternalIds, _initialRateExternalIds, eurUsdId);
    loadVolSurfaceData();
    loadFXPortfolio();
    loadMultiCurrencySwapPortfolio();
    loadEquityOptionPortfolio();
    loadViews();       
  }
  
  private BloombergExamplePortfolioGeneratorTool portfolioGeneratorTool() {
    final BloombergExamplePortfolioGeneratorTool tool = new BloombergExamplePortfolioGeneratorTool();
    tool.setCounterPartyGenerator(new StaticNameGenerator(AbstractPortfolioGeneratorTool.DEFAULT_COUNTER_PARTY));
    return tool;
  }

  private void loadEquityOptionPortfolio() {
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

  private void loadTimeSeriesRating() {
    ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
    System.out.println("Creating Timeseries configuration");
    timeSeriesRatingLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadFutures(Set<ExternalIdBundle> identifiers) {
    SecurityMaster secMaster = getToolContext().getSecurityMaster();
    ReferenceDataProvider referenceDataProvider = getToolContext().getBloombergReferenceDataProvider();
    ExchangeDataProvider exchangeDataProvider = new DefaultExchangeDataProvider();
    BloombergBulkSecurityLoader bulkSecurityLoader = new BloombergBulkSecurityLoader(referenceDataProvider, exchangeDataProvider);
    BloombergSecurityLoader securityLoader = new BloombergSecurityLoader(secMaster, bulkSecurityLoader);
    securityLoader.loadSecurity(identifiers);
  }

  private void loadHistoricalData(Set<ExternalId>... externalIdSets) {
    BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(
      getToolContext().getHistoricalTimeSeriesMaster(),
      getToolContext().getBloombergHistoricalTimeSeriesSource(),
      new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));

    Collection<EquitySecurity> securities = readEquitySecurities();
    for (final EquitySecurity security : securities) {
      loader.addTimeSeries(ImmutableSet.of(security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER)), "CMPL", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
    }
    for (Set<ExternalId> externalIds : externalIdSets) {
      if (externalIds.size() > 0) {
        loader.addTimeSeries(externalIds, "CMPL", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
      }
    }
    System.out.println("Finished");
  }

  private Collection<EquitySecurity> readEquitySecurities() {
    List<EquitySecurity> result = Lists.newArrayList();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType(EquitySecurity.SECURITY_TYPE);
    SecuritySearchResult searchResult = securityMaster.search(request);
    List<SecurityDocument> documents = searchResult.getDocuments();
    for (SecurityDocument securityDocument : documents) {
      result.add((EquitySecurity) securityDocument.getSecurity());
    }
    return result;
  }

  private void loadEquityPortfolio() {
    ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
    System.out.println("Creating example equity portfolio");
    equityLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadMultiCurrencySwapPortfolio() {
    ExampleMultiCurrencySwapPortfolioLoader multiCurrSwapLoader = new ExampleMultiCurrencySwapPortfolioLoader();
    System.out.println("Creating example multi currency swap portfolio");
    multiCurrSwapLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadFXPortfolio() {
    System.out.println("Creating FX portfolio");
    portfolioGeneratorTool().run(getToolContext(), FX_PORTFOLIO_NAME, "EuroDollarFX", true, s_currencies);
    System.out.println("Finished");
  }

  private void loadViews() {
    ExampleViewsPopulator populator = new ExampleViewsPopulator();
    System.out.println("Creating example view definitions");
    populator.run(getToolContext());
    System.out.println("Finished");
  }
  
}

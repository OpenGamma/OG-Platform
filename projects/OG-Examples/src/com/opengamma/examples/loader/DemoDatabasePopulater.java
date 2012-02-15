/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.google.common.collect.Sets;
import com.opengamma.examples.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.util.money.Currency;

/**
 * Example code to create a demo portfolio and view
 * <p>
 * It is designed to run against the HSQLDB example database.  
 * It should be possible to run this class with no extra command line parameters.
 */
public class DemoDatabasePopulater {

  private static final LocalMastersUtils s_localMastersUtils = LocalMastersUtils.INSTANCE;
  
  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(DemoDatabasePopulater.class);
  
  private static final Set<Currency> s_currencies = Sets.newHashSet(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.AUD, Currency.CAD);

  //-------------------------------------------------------------------------
  /**
   * Sets up and loads the context.
   * <p>
   * This loader requires a Spring configuration file that defines the security,
   * position and portfolio masters, together with an instance of this bean
   * under the name "demoEquityPortfolioLoader".
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    try {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset(); 
      URL logbackResource = ClassLoader.getSystemResource("com/opengamma/examples/server/logback.xml");
      configurator.doConfigure(logbackResource);     
      new DemoDatabasePopulater().run();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      s_localMastersUtils.tearDown();
    }
    System.exit(0);
  }

  public void run() {
    
    loadTimeSeriesRating();
    
    loadSimulatedHistoricalData();
    
    loadEquityPortfolioAndSecurity();
    
    loadSwapPortfolio();
    
    loadMultiCurrencySwapPortfolio();
    
    loadLiborRawSecurities();
    
    loadViews();
  }

  private void loadViews() {
    DemoViewsPopulater populator = new DemoViewsPopulater();
    populator.setLoaderContext(s_localMastersUtils.getLoaderContext());
    System.out.println("Creating demo view definition");
    populator.persistViewDefinitions();
    System.out.println("Finished");
  }

  private void loadLiborRawSecurities() {
    System.out.println("Creating libor raw securities");
    PortfolioLoaderHelper.persistLiborRawSecurities(getAllCurrencies(), s_localMastersUtils.getLoaderContext());
    System.out.println("Finished");
  }

  private void loadMultiCurrencySwapPortfolio() {
    DemoMultiCurrencySwapPortfolioLoader multiCurrSwapLoader = new DemoMultiCurrencySwapPortfolioLoader();
    multiCurrSwapLoader.setLoaderContext(s_localMastersUtils.getLoaderContext());
    System.out.println("Creating example multi currency swap portfolio");
    multiCurrSwapLoader.createPortfolio();
    System.out.println("Finished");
  }

  private DemoSwapPortfolioLoader loadSwapPortfolio() {
    DemoSwapPortfolioLoader swapLoader = new DemoSwapPortfolioLoader();
    swapLoader.setLoaderContext(s_localMastersUtils.getLoaderContext());
    System.out.println("Creating example swap portfolio");
    swapLoader.createExamplePortfolio();
    System.out.println("Finished");
    return swapLoader;
  }

  private void loadEquityPortfolioAndSecurity() {
    DemoEquityPortfolioAndSecurityLoader equityLoader = new DemoEquityPortfolioAndSecurityLoader();
    equityLoader.setLoaderContext(s_localMastersUtils.getLoaderContext());
    System.out.println("Creating example equity portfolio");
    equityLoader.createExamplePortfolio();
    System.out.println("Finished");
  }

  private void loadSimulatedHistoricalData() {
    SimulatedHistoricalDataGenerator historicalDataGenerator = new SimulatedHistoricalDataGenerator(s_localMastersUtils.getLoaderContext().getHistoricalTimeSeriesMaster());
    System.out.println("Creating simulated historical timeseries");
    historicalDataGenerator.run();
    System.out.println("Finished");
  }

  private void loadTimeSeriesRating() {
    TimeSeriesRatingLoader timeSeriesRatingLoader = new TimeSeriesRatingLoader();
    timeSeriesRatingLoader.setLoaderContext(s_localMastersUtils.getLoaderContext());
    System.out.println("Creating Timeseries configuration");
    timeSeriesRatingLoader.saveHistoricalTimeSeriesRatings();
    System.out.println("Finished");
  }

  private static Set<Currency> getAllCurrencies() {    
    return s_currencies;
  }
  
}

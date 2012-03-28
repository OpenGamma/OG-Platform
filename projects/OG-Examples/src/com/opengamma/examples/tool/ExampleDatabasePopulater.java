/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.examples.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.loader.ExampleMixedPortfolioLoader;
import com.opengamma.examples.loader.ExampleMultiCurrencySwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleSwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.loader.ExampleViewsPopulater;
import com.opengamma.examples.loader.PortfolioLoaderHelper;
import com.opengamma.util.money.Currency;

/**
 * Single class that populates the database with data for running the example server.
 * <p>
 * It is designed to run against the HSQLDB example database.  
 */
public class ExampleDatabasePopulater extends AbstractExampleTool {

  /**
   * The currencies.
   */
  private static final Set<Currency> s_currencies = Sets.newHashSet(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.AUD, Currency.CAD);

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

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    
    loadTimeSeriesRating();
    
    loadSimulatedHistoricalData();
    
    loadEquityPortfolio();
    
    loadSwapPortfolio();
    
    loadMultiCurrencySwapPortfolio();
    
    loadLiborRawSecurities();
    
    loadMixedPortfolio();
    
    loadViews();
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

  private void loadSimulatedHistoricalData() {
    ExampleHistoricalDataGeneratorTool historicalDataGenerator = new ExampleHistoricalDataGeneratorTool();
    System.out.println("Creating simulated historical timeseries");
    historicalDataGenerator.run(getToolContext());
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

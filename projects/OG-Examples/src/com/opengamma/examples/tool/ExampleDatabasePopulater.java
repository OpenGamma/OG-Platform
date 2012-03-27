/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.examples.generator.PortfolioGeneratorTool;
import com.opengamma.examples.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.loader.ExampleMixedPortfolioLoader;
import com.opengamma.examples.loader.ExampleSwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.loader.ExampleViewsPopulater;
import com.opengamma.examples.loader.PortfolioLoaderHelper;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
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
   * Main method to run the tool. No arguments are needed.
   * 
   * @param args the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    new ExampleDatabasePopulater().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    loadTimeSeriesRating();
    loadYieldCurves();
    loadSimulatedHistoricalData();
    loadEquityPortfolio();
    loadSwapPortfolio();
    loadMultiCurrencySwapPortfolio();
    loadBondPortfolio();
    loadCapFloorCMSSpreadPortfolio();
    loadCapFloorPortfolio();
    loadCashPortfolio();
    loadEquityOptionPortfolio();
    loadFRAPortfolio();
    loadLiborRawSecurities();
    loadMixedPortfolio();
    loadViews();
  }

  private static final class Log {

    private final String _str;

    private Log(final String str) {
      System.out.println(str);
      _str = str;
    }

    private void done() {
      System.out.println(_str + " - finished");
    }

    private void fail(final RuntimeException e) {
      System.err.println(_str + " - failed - " + e.getMessage());
      throw e;
    }

  }

  private void loadMixedPortfolio() {
    final Log log = new Log("Creating example mixed portfolio");
    try {
      ExampleMixedPortfolioLoader mixedPortfolioLoader = new ExampleMixedPortfolioLoader();
      mixedPortfolioLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadTimeSeriesRating() {
    final Log log = new Log("Creating Timeseries configuration");
    try {
      ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
      timeSeriesRatingLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadYieldCurves() {
    final Log log = new Log("Creating yield curve definitions");
    try {
      YieldCurveConfigPopulator.populateCurveConfigMaster(getToolContext().getConfigMaster());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSimulatedHistoricalData() {
    final Log log = new Log("Creating simulated historical timeseries");
    try {
      final ExampleHistoricalDataGeneratorTool historicalDataGenerator = new ExampleHistoricalDataGeneratorTool();
      historicalDataGenerator.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityPortfolio() {
    final Log log = new Log("Creating example equity portfolio");
    try {
      final ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
      equityLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSwapPortfolio() {
    final Log log = new Log("Creating example swap portfolio");
    try {
      final ExampleSwapPortfolioLoader swapLoader = new ExampleSwapPortfolioLoader();
      swapLoader.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadMultiCurrencySwapPortfolio() {
    final Log log = new Log("Creating example multi currency swap portfolio");
    try {
      (new PortfolioGeneratorTool()).run(getToolContext(), "Example MultiCurrency Swap Portfolio", "Swap", true);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadBondPortfolio() {
    final Log log = new Log("Creating example bond portfolio");
    try {
      // TODO: load from CSV file
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCapFloorCMSSpreadPortfolio() {
    final Log log = new Log("Creating example cap/floor CMS spread portfolio");
    try {
      (new PortfolioGeneratorTool()).run(getToolContext(), "Example Cap/Floor CMS Spread Portfolio", "CapFloorCMSSpread", true);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCapFloorPortfolio() {
    final Log log = new Log("Creating example cap/floor portfolio");
    try {
      (new PortfolioGeneratorTool()).run(getToolContext(), "Example Cap/Floor Portfolio", "CapFloor", true);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCashPortfolio() {
    final Log log = new Log("Creating example cash portfolio");
    try {
      (new PortfolioGeneratorTool()).run(getToolContext(), "Example Cash Portfolio", "Cash", true);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Creating example equity option portfolio");
    try {
      // TODO: load from CSV file
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFRAPortfolio() {
    final Log log = new Log("Creating example FRA portfolio");
    try {
      (new PortfolioGeneratorTool()).run(getToolContext(), "Example FRA Portfolio", "FRA", true);
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadLiborRawSecurities() {
    final Log log = new Log("Creating libor raw securities");
    try {
      PortfolioLoaderHelper.persistLiborRawSecurities(getAllCurrencies(), getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final ExampleViewsPopulater populator = new ExampleViewsPopulater();
      populator.run(getToolContext());
      log.done();
    } catch (RuntimeException t) {
      log.fail(t);
    }
  }

  private static Set<Currency> getAllCurrencies() {
    return s_currencies;
  }

}

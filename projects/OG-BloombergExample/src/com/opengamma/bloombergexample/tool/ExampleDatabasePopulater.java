/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.tool;

import java.util.*;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.component.BloombergTimeSeriesUpdateTool;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.bbg.tool.BloombergToolContext;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.bloombergexample.loader.*;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
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

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {

    loadTimeSeriesRating();

    loadEquityPortfolio();

    loadHistoricalData();

    //loadSwapPortfolio();

    //loadMultiCurrencySwapPortfolio();

    //loadLiborRawSecurities();

    //loadMixedPortfolio();

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

  private void loadHistoricalData() {
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
          }}, "CMPL", "PX_LAST", LocalDate.now().minusDays(14), LocalDate.now()); 
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

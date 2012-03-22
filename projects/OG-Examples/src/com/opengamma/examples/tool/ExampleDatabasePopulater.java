/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.examples.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.examples.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.loader.ExampleMixedPortfolioLoader;
import com.opengamma.examples.loader.ExampleMultiCurrencySwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleSwapPortfolioLoader;
import com.opengamma.examples.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.loader.ExampleViewsPopulater;
import com.opengamma.examples.loader.PortfolioLoaderHelper;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

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

    loadCurveAndSurfaceDefinitions();
    
    loadDefaultVolatilityCubeDefinition();
    
    loadTimeSeriesRating();
    
    loadSimulatedHistoricalData();
    
    loadEquityPortfolio();
    
    loadSwapPortfolio();
    
    loadMultiCurrencySwapPortfolio();
    
    loadLiborRawSecurities();
    
    loadMixedPortfolio();
    
    loadViews();
  }

  private void loadCurveAndSurfaceDefinitions() {
    ExampleCurveAndSurfaceDefinitionLoader curveLoader = new ExampleCurveAndSurfaceDefinitionLoader();
    System.out.println("Creating curve and surface definitions");
    curveLoader.run(getToolContext());
    System.out.println("Finished");
  }

  private void loadDefaultVolatilityCubeDefinition() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    
    ConfigDocument<VolatilityCubeDefinition> doc = new ConfigDocument<VolatilityCubeDefinition>(VolatilityCubeDefinition.class);
    doc.setName("SECONDARY_USD");
    doc.setValue(createDefaultDefinition());
    System.out.println("Populating vol cube defn " + doc.getName());
    ConfigMasterUtils.storeByName(configMaster, doc);
    
    VolatilityCubeConfigPopulator.populateVolatilityCubeConfigMaster(configMaster);
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
  
  private static VolatilityCubeDefinition createDefaultDefinition() {
    
    VolatilityCubeDefinition volatilityCubeDefinition = new VolatilityCubeDefinition();
    volatilityCubeDefinition.setSwapTenors(Lists.newArrayList(Tenor.ofMonths(3), Tenor.ofYears(1), Tenor.ofYears(2),
        Tenor.ofYears(5), Tenor.ofYears(10), Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(30)));
    volatilityCubeDefinition.setOptionExpiries(Lists.newArrayList(Tenor.ofMonths(3), Tenor.ofMonths(6),
        Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(4), Tenor.ofYears(5), Tenor.ofYears(10), Tenor.ofYears(15),
        Tenor.ofYears(20)));
    
    int[] values = new int[] {0, 20, 25, 50, 70, 75, 100, 200, 5 };
    List<Double> relativeStrikes = new ArrayList<Double>(values.length * 2 - 1);
    for (int value : values) {
      relativeStrikes.add(Double.valueOf(value));
      if (value != 0) {
        relativeStrikes.add(Double.valueOf(-value));
      }
    }
    Collections.sort(relativeStrikes);
    
    volatilityCubeDefinition.setRelativeStrikes(relativeStrikes);
    return volatilityCubeDefinition;
  }
  
}

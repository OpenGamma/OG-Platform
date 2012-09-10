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

import com.opengamma.component.tool.AbstractTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.examples.generator.SyntheticPortfolioGeneratorTool;
import com.opengamma.examples.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.examples.loader.ExampleCurveConfigurationLoader;
import com.opengamma.examples.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.loader.ExampleViewsPopulator;
import com.opengamma.examples.loader.PortfolioLoaderHelper;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeConfigPopulator;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Single class that populates the database with data for running the example server.
 * <p>
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleDatabasePopulator extends AbstractTool<ToolContext> {

  /**
   * The properties file.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:toolcontext/toolcontext-example.properties";
  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "Multi-currency Swap Portfolio";
  /**
   * The name of Cap/Floor portfolio
   */
  public static final String CAP_FLOOR_PORTFOLIO_NAME = "Cap/Floor Portfolio";
  /**
   * The name of the AUD swap portfolio
   */
  public static final String AUD_SWAP_PORFOLIO_NAME = "AUD Swap Portfolio";
  /**
   * The name of the swaption portfolio
   */
  public static final String SWAPTION_PORTFOLIO_NAME = "Swap / Swaption Portfolio";
  /**
   * The name of the mixed CMS portfolio
   */
  public static final String MIXED_CMS_PORTFOLIO_NAME = "Mixed CM Portfolio";
  /**
   * The name of a vanilla FX option portfolio
   */
  public static final String VANILLA_FX_OPTION_PORTFOLIO_NAME = "Vanilla FX Option Portfolio";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabasePopulator.class);
  /**
   * The currencies.
   */
  private static final Set<Currency> s_currencies = Sets.newHashSet(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.CAD);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   * 
   * @param args the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    try {
      new ExampleDatabasePopulator().initAndRun(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null, ToolContext.class);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {

    loadCurveAndSurfaceDefinitions();
    loadCurveCalculationConfigurations();
    loadDefaultVolatilityCubeDefinition();
    loadTimeSeriesRating();
    loadYieldCurves();
    loadSimulatedHistoricalData();
    loadMultiCurrencySwapPortfolio();
    loadAUDSwapPortfolio();
    loadSwaptionParityPortfolio();
    loadMixedCMPortfolio();
    loadVanillaFXOptionPortfolio();
    loadEquityPortfolio();
    loadEquityOptionPortfolio();
    loadBondPortfolio();
    loadLiborRawSecurities();
    loadViews();
  }

  private void loadCurveAndSurfaceDefinitions() {
    final ExampleCurveAndSurfaceDefinitionLoader curveLoader = new ExampleCurveAndSurfaceDefinitionLoader();
    s_logger.info("Creating curve and surface definitions");
    curveLoader.run(getToolContext());
    s_logger.info("Finished");
  }

  private void loadCurveCalculationConfigurations() {
    final ExampleCurveConfigurationLoader curveConfigLoader = new ExampleCurveConfigurationLoader();
    s_logger.info("Creating curve calculation configurations");
    curveConfigLoader.run(getToolContext());
    s_logger.info("Finished");
  }

  private void loadDefaultVolatilityCubeDefinition() {
    final ToolContext toolContext = getToolContext();
    final ConfigMaster configMaster = toolContext.getConfigMaster();

    final ConfigDocument<VolatilityCubeDefinition> doc = new ConfigDocument<VolatilityCubeDefinition>(VolatilityCubeDefinition.class);
    doc.setName("SECONDARY_USD");
    doc.setValue(createDefaultDefinition());
    s_logger.info("Populating vol cube defn " + doc.getName());
    ConfigMasterUtils.storeByName(configMaster, doc);

    VolatilityCubeConfigPopulator.populateVolatilityCubeConfigMaster(configMaster);
  }

  private static final class Log {

    private final String _str;

    private Log(final String str) {
      s_logger.info(str);
      _str = str;
    }

    private void done() {
      s_logger.info(_str + " - finished");
    }

    private void fail(final RuntimeException e) {
      System.err.println(_str + " - failed - " + e.getMessage());
      throw e;
    }

  }

  private void loadTimeSeriesRating() {
    final Log log = new Log("Creating Timeseries configuration");
    try {
      final ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
      timeSeriesRatingLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadYieldCurves() {
    final Log log = new Log("Creating yield curve definitions");
    try {
      YieldCurveConfigPopulator.populateSyntheticCurveConfigMaster(getToolContext().getConfigMaster());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSimulatedHistoricalData() {
    final Log log = new Log("Creating simulated historical timeseries");
    try {
      final ExampleHistoricalDataGeneratorTool historicalDataGenerator = new ExampleHistoricalDataGeneratorTool();
      historicalDataGenerator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityPortfolio() {
    final Log log = new Log("Creating example equity portfolio");
    try {
      final ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
      equityLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private SyntheticPortfolioGeneratorTool portfolioGeneratorTool() {
    final SyntheticPortfolioGeneratorTool tool = new SyntheticPortfolioGeneratorTool();
    tool.setCounterPartyGenerator(new StaticNameGenerator(AbstractPortfolioGeneratorTool.DEFAULT_COUNTER_PARTY));
    return tool;
  }

  private void loadMultiCurrencySwapPortfolio() {
    final Log log = new Log("Creating example multi currency swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAP_PORTFOLIO_NAME, "Swap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadAUDSwapPortfolio() {
    final Log log = new Log("Creating example AUD swap portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), AUD_SWAP_PORFOLIO_NAME, "AUDSwap", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadSwaptionParityPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), SWAPTION_PORTFOLIO_NAME, "SwaptionParity", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadMixedCMPortfolio() {
    final Log log = new Log("Creating example mixed CM portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MIXED_CMS_PORTFOLIO_NAME, "MixedCM", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadVanillaFXOptionPortfolio() {
    final Log log = new Log("Creating example vanilla FX option portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), VANILLA_FX_OPTION_PORTFOLIO_NAME, "VanillaFXOption", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadBondPortfolio() {
    final Log log = new Log("Creating example bond portfolio");
    try {
      // TODO: load from CSV file
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Creating example equity option portfolio");
    try {
      // TODO: load from CSV file
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadLiborRawSecurities() {
    final Log log = new Log("Creating libor raw securities");
    try {
      PortfolioLoaderHelper.persistLiborRawSecurities(getAllCurrencies(), getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final ExampleViewsPopulator populator = new ExampleViewsPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private static Set<Currency> getAllCurrencies() {
    return s_currencies;
  }

  private static VolatilityCubeDefinition createDefaultDefinition() {

    final VolatilityCubeDefinition volatilityCubeDefinition = new VolatilityCubeDefinition();
    volatilityCubeDefinition.setSwapTenors(Lists.newArrayList(Tenor.ofMonths(3), Tenor.ofYears(1), Tenor.ofYears(2),
        Tenor.ofYears(5), Tenor.ofYears(10), Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(30)));
    volatilityCubeDefinition.setOptionExpiries(Lists.newArrayList(Tenor.ofMonths(3), Tenor.ofMonths(6),
        Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(4), Tenor.ofYears(5), Tenor.ofYears(10), Tenor.ofYears(15),
        Tenor.ofYears(20)));

    final int[] values = new int[] {0, 20, 25, 50, 70, 75, 100, 200, 5 };
    final List<Double> relativeStrikes = new ArrayList<Double>(values.length * 2 - 1);
    for (final int value : values) {
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

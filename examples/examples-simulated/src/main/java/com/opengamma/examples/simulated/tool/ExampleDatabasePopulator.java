/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.examples.simulated.generator.SyntheticPortfolioGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleCurrencyConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.examples.simulated.loader.ExampleCurveConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.examples.simulated.loader.ExampleIborIndexLoader;
import com.opengamma.examples.simulated.loader.ExampleExchangeLoader;
import com.opengamma.examples.simulated.loader.ExampleFXImpliedCurveConfigurationLoader;
import com.opengamma.examples.simulated.loader.ExampleFunctionConfigurationPopulator;
import com.opengamma.examples.simulated.loader.ExampleHistoricalDataGeneratorTool;
import com.opengamma.examples.simulated.loader.ExampleHolidayLoader;
import com.opengamma.examples.simulated.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.examples.simulated.loader.ExampleViewsPopulator;
import com.opengamma.examples.simulated.loader.PortfolioLoaderHelper;
import com.opengamma.financial.convention.initializer.DefaultConventionMasterInitializer;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.tool.portfolio.PortfolioLoader;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;

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
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:/toolcontext/toolcontext-examplessimulated.properties";
  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "Swap Portfolio";
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
  public static final String MIXED_CMS_PORTFOLIO_NAME = "Constant Maturity Swap Portfolio";
  /**
   * The name of a vanilla FX option portfolio
   */
  public static final String VANILLA_FX_OPTION_PORTFOLIO_NAME = "Vanilla FX Option Portfolio";
  /**
   * The name of a EUR fixed income portfolio
   */
  public static final String EUR_SWAP_PORTFOLIO_NAME = "EUR Fixed Income Portfolio";
  /**
   * The name of a mixed currency swaption portfolio
   */
  public static final String MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME = "Swaption Portfolio";
  /**
   * The name of a FX forward portfolio.
   */
  public static final String FX_FORWARD_PORTFOLIO_NAME = "FX Forward Portfolio";
  /**
   * Equity options portfolio
   */
  public static final String EQUITY_OPTION_PORTFOLIO_NAME = "Equity Option Portfolio";
  /**
   * Futures portfolio
   */
  public static final String FUTURE_PORTFOLIO_NAME = "Futures Portfolio";
  /**
   * The name of an ER future portfolio.
   */
  public static final String ER_PORTFOLIO_NAME = "ER Portfolio";
  /**
   * The name of a US Government bond portfolio.
   */
  public static final String US_GOVERNMENT_BOND_PORTFOLIO_NAME = "Government Bonds";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabasePopulator.class);
  /**
   * The currencies.
   */
  private static final Set<Currency> s_currencies = Sets.newHashSet(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.CAD);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    s_logger.info("Populating example database");
    new ExampleDatabasePopulator().invokeAndTerminate(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    loadExchanges();
    loadHolidays();
    loadConventions();
    loadCurrencyConfiguration();
    loadCurveAndSurfaceDefinitions();
    loadCurveCalculationConfigurations();
    loadTimeSeriesRating();
    loadSimulatedHistoricalData();
    loadMultiCurrencySwapPortfolio();
    loadIborIndex();
    loadAUDSwapPortfolio();
    loadSwaptionParityPortfolio();
    loadMixedCMPortfolio();
    loadVanillaFXOptionPortfolio();
    loadEquityPortfolio();
    loadEquityOptionPortfolio();
    loadFuturePortfolio();
    loadBondPortfolio();
    loadLiborRawSecurities();
    loadSwaptionPortfolio();
    loadEURFixedIncomePortfolio();
    loadFXForwardPortfolio();
    loadERFuturePortfolio();
    loadFXImpliedCurveCalculationConfigurations();
    loadViews();
    loadFunctionConfigurations();
  }

  private void loadFunctionConfigurations() {
    final Log log = new Log("Creating function configuration definitions");
    try {
      final ExampleFunctionConfigurationPopulator populator = new ExampleFunctionConfigurationPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  /**
   * Logging helper. All stages must go through this. When run as part of the Windows install, the logger is customized to recognize messages formatted in this fashion and route them towards the
   * progress indicators.
   */
  private static final class Log {
    /** The string */
    private final String _str;

    private Log(final String str) {
      s_logger.info("{}", str);
      _str = str;
    }

    private void done() {
      s_logger.debug("{} - finished", _str);
    }

    private void fail(final RuntimeException e) {
      s_logger.error("{} - failed - {}", _str, e.getMessage());
      throw e;
    }

  }

  private void loadConventions() {
    final Log log = new Log("Creating convention data");
    try {
      final ConventionMaster master = getToolContext().getConventionMaster();
      DefaultConventionMasterInitializer.INSTANCE.init(master);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurrencyConfiguration() {
    final Log log = new Log("Creating FX definitions");
    try {
      final ExampleCurrencyConfigurationLoader currencyLoader = new ExampleCurrencyConfigurationLoader();
      currencyLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurveAndSurfaceDefinitions() {
    final Log log = new Log("Creating curve and surface definitions");
    try {
      final ExampleCurveAndSurfaceDefinitionLoader curveLoader = new ExampleCurveAndSurfaceDefinitionLoader();
      curveLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurveCalculationConfigurations() {
    final Log log = new Log("Creating curve calculation configurations");
    try {
      final ExampleCurveConfigurationLoader curveConfigLoader = new ExampleCurveConfigurationLoader();
      curveConfigLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFXImpliedCurveCalculationConfigurations() {
    final Log log = new Log("Creating FX-implied curve calculation configurations");
    try {
      final ExampleFXImpliedCurveConfigurationLoader curveConfigLoader = new ExampleFXImpliedCurveConfigurationLoader();
      curveConfigLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
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

  private void loadIborIndex() {
    final Log log = new Log("Creating ibor index configuration");
    try {
      final ExampleIborIndexLoader iborIndexLoader = new ExampleIborIndexLoader();
      iborIndexLoader.run(getToolContext());
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

  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Creating example equity option portfolio");
    try {
      final URL resource = ExampleEquityPortfolioLoader.class.getResource("equityOptions.zip");
      final String file = unpackJar(resource);
      final PortfolioLoader equityOptionLoader = new PortfolioLoader(getToolContext(), EQUITY_OPTION_PORTFOLIO_NAME, null,
          file, true,
          true, true, false, true, false, null);
      equityOptionLoader.execute();
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFuturePortfolio() {
    final Log log = new Log("Creating example future portfolio");
    try {
      final URL resource = ExampleEquityPortfolioLoader.class.getResource("futures.zip");
      final String file = unpackJar(resource);
      final PortfolioLoader futureLoader = new PortfolioLoader(getToolContext(), FUTURE_PORTFOLIO_NAME, null,
          file, true, true, true, false, true, false, null);
      futureLoader.execute();
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

  private void loadSwaptionPortfolio() {
    final Log log = new Log("Creating example swaption portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME, "Swaption", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEURFixedIncomePortfolio() {
    final Log log = new Log("Creating example EUR fixed income portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), EUR_SWAP_PORTFOLIO_NAME, "EURFixedIncome", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFXForwardPortfolio() {
    final Log log = new Log("Creating example FX forward portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), FX_FORWARD_PORTFOLIO_NAME, "FxForward", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadERFuturePortfolio() {
    final Log log = new Log("Creating example ER future portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), ER_PORTFOLIO_NAME, "ERFutureForCurve", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadBondPortfolio() {
    final Log log = new Log("Creating example bond portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), US_GOVERNMENT_BOND_PORTFOLIO_NAME, "Bond", true, null);
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

  private void loadExchanges() {
    final Log log = new Log("Creating exchange data");
    try {
      final ExampleExchangeLoader loader = new ExampleExchangeLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadHolidays() {
    final Log log = new Log("Creating holiday data");
    try {
      final ExampleHolidayLoader loader = new ExampleHolidayLoader();
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private static Set<Currency> getAllCurrencies() {
    return s_currencies;
  }

  //-------------------------------------------------------------------------
  // workaround for poor handling of resources, see PLAT-3919
  private static String unpackJar(final URL resource) {
    String file = resource.getPath();
    if (file.contains(".jar!/")) {
      s_logger.info("Unpacking zip file located within a jar file: {}", resource);
      String jarFileName = StringUtils.substringBefore(file, "!/");
      if (jarFileName.startsWith("file:/")) {
        jarFileName = jarFileName.substring(5);
        if (SystemUtils.IS_OS_WINDOWS) {
          jarFileName = StringUtils.stripStart(jarFileName, "/");
        }
      } else if (jarFileName.startsWith("file:/")) {
        jarFileName = jarFileName.substring(6);
      }
      jarFileName = StringUtils.replace(jarFileName, "%20", " ");
      String innerFileName = StringUtils.substringAfter(file, "!/");
      innerFileName = StringUtils.replace(innerFileName, "%20", " ");
      s_logger.info("Unpacking zip file found jar file: {}", jarFileName);
      s_logger.info("Unpacking zip file found zip file: {}", innerFileName);
      try (JarFile jar = new JarFile(jarFileName)) {
        final JarEntry jarEntry = jar.getJarEntry(innerFileName);
        try (InputStream in = jar.getInputStream(jarEntry)) {
          final File tempFile = File.createTempFile("simulated-examples-database-populator-", ".zip");
          tempFile.deleteOnExit();
          try (OutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
          }
          file = tempFile.getCanonicalPath();
        }
      } catch (final IOException ex) {
        throw new OpenGammaRuntimeException("Unable to open file within jar file: " + resource, ex);
      }
      s_logger.debug("Unpacking zip file extracted to: {}", file);
    }
    return file;
  }

}

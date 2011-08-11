/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.net.URL;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.google.common.collect.Sets;
import com.opengamma.examples.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;

/**
 * Example code to create a demo portfolio and view
 * <p>
 * It is designed to run against the HSQLDB example database.  
 * It should be possible to run this class with no extra command line parameters.
 */
public class DemoDatabasePopulater {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(DemoDatabasePopulater.class);

  /**
   * The context.
   */
  @SuppressWarnings("unused")
  private LoaderContext _loaderContext;

  public void setLoaderContext(LoaderContext loaderContext) {
    _loaderContext = loaderContext;
  }
  
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
      
      // Set the run mode to EXAMPLE so we use the HSQLDB example database.
      PlatformConfigUtils.configureSystemProperties(RunMode.EXAMPLE);
      System.out.println("Starting connections");
      AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("demoPortfolioLoader.xml");
      appContext.start();
      
      try {
        TimeSeriesRatingLoader tsConfigLoader = appContext.getBean("timeSeriesRatingLoader", TimeSeriesRatingLoader.class);
        System.out.println("Creating Timeseries configuration");
        tsConfigLoader.saveHistoricalTimeSeriesRatings();
        System.out.println("Finished");
        
        SimulatedHistoricalDataGenerator historicalDataGenerator = appContext.getBean("simulatedHistoricalDataGenerator", SimulatedHistoricalDataGenerator.class);
        System.out.println("Creating simulated historical timeseries");
        historicalDataGenerator.run();
        System.out.println("Finished");
        
        DemoEquityPortfolioAndSecurityLoader equityLoader = appContext.getBean("demoEquityPortfolioAndSecurityLoader", DemoEquityPortfolioAndSecurityLoader.class);
        System.out.println("Creating example equity portfolio");
        equityLoader.createExamplePortfolio();
        System.out.println("Finished");
        
        DemoSwapPortfolioLoader swapLoader = appContext.getBean("demoSwapPortfolioLoader", DemoSwapPortfolioLoader.class);
        System.out.println("Creating example swap portfolio");
        swapLoader.createExamplePortfolio();
        System.out.println("Finished");
        
        DemoMultiCurrencySwapPortfolioLoader multiCurrSwapLoader = appContext.getBean("demoMultiCurrencySwapPortfolioLoader", DemoMultiCurrencySwapPortfolioLoader.class);
        System.out.println("Creating example multi currency swap portfolio");
        multiCurrSwapLoader.createPortfolio();
        System.out.println("Finished");
        
        System.out.println("Creating libor raw securities");
        PortfolioLoaderHelper.persistLiborRawSecurities(Sets.newHashSet(Arrays.asList(DemoMultiCurrencySwapPortfolioLoader.s_currencies)), swapLoader.getLoaderContext());
        System.out.println("Finished");
        
        DemoViewsPopulater populator = appContext.getBean("demoViewsPopulater", DemoViewsPopulater.class);
        System.out.println("Creating demo view definition");
        populator.persistViewDefinitions();
        
      } finally {
        appContext.close();
      }
      System.out.println("Finished");
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }
  
  public static void populateYieldCurveConfig(ConfigMaster configMaster) {
    YieldCurveConfigPopulator.populateCurveConfigMaster(configMaster);
  }

}

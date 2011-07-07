/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;
import com.opengamma.util.money.Currency;

/**
 * Example code to create a demo portfolio and view
 * <p>
 * It is designed to run against the HSQLDB example database.  
 * It should be possible to run this class with no extra command line parameters.
 */
public class DemoDatabasePopulater {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DemoDatabasePopulater.class);

  /**
   * The context.
   */
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
      configurator.doConfigure("src/com/opengamma/examples/server/logback.xml");
      
      // Set the run mode to EXAMPLE so we use the HSQLDB example database.
      PlatformConfigUtils.configureSystemProperties(RunMode.EXAMPLE);
      System.out.println("Starting connections");
      AbstractApplicationContext appContext = new ClassPathXmlApplicationContext("demoPortfolioLoader.xml");
      appContext.start();
      
      try {
        SelfContainedEquityPortfolioAndSecurityLoader loader = (SelfContainedEquityPortfolioAndSecurityLoader) appContext.getBean("selfContainedEquityPortfolioAndSecurityLoader");
        System.out.println("Creating example equity portfolio");
        loader.createExamplePortfolio();
        System.out.println("Finished");
        
        DemoViewsPopulater populator = (DemoViewsPopulater) appContext.getBean("demoViewsPopulater");
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

}

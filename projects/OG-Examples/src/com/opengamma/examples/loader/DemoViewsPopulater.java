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
 * Example code to create a set of demo views
 * <p>
 * It is designed to run against the HSQLDB example database.  
 * It should be possible to run this class with no extra command line parameters.
 */
public class DemoViewsPopulater {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DemoViewsPopulater.class);

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Self Contained Equity Portfolio";

  /**
   * The context.
   */
  private LoaderContext _loaderContext;

  public void setLoaderContext(LoaderContext loaderContext) {
    _loaderContext = loaderContext;
  }
  
  private UniqueIdentifier getPortfolioId(String portfolioName) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult searchResult = _loaderContext.getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio" + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }
  
  private ViewDefinition makeEquityViewDefinition(String portfolioName) {
    UniqueIdentifier portfolioId = getPortfolioId(portfolioName);
    ViewDefinition equityViewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    equityViewDefinition.setDefaultCurrency(Currency.USD);
    equityViewDefinition.setMaxFullCalculationPeriod(30000L);
    equityViewDefinition.setMinFullCalculationPeriod(500L);
    equityViewDefinition.addPortfolioRequirement("Default", "EQUITY", "FairValue", ValueProperties.none());
    return equityViewDefinition;
  }
  
  public void persistViewDefinitions() {
    ViewDefinition equityViewDef = makeEquityViewDefinition(PORTFOLIO_NAME);
    ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    configDocument.setName(equityViewDef.getName());
    configDocument.setValue(equityViewDef);
    ConfigMasterUtils.storeByName(_loaderContext.getConfigMaster(), configDocument);
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
        DemoViewsPopulater populator = (DemoViewsPopulater) appContext.getBean("demoViewsPopulater");
        System.out.println("Loading data");
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

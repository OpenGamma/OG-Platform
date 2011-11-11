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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueId;
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
   * The context.
   */
  private LoaderContext _loaderContext;

  public void setLoaderContext(LoaderContext loaderContext) {
    _loaderContext = loaderContext;
  }

  private UniqueId getPortfolioId(String portfolioName) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult searchResult = _loaderContext.getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio" + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  public void persistViewDefinitions() {
    createEquityViewDefinition();
    createSwapViewDefinition();
    createMultiCurrencySwapViewDefinition();
  }

  private ViewDefinition makeEquityViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition equityViewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    equityViewDefinition.setDefaultCurrency(Currency.USD);
    equityViewDefinition.setMaxFullCalculationPeriod(30000L);
    equityViewDefinition.setMinFullCalculationPeriod(500L);
    equityViewDefinition.setMinDeltaCalculationPeriod(500L);
    equityViewDefinition.setMaxDeltaCalculationPeriod(30000L);
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.CAPM_BETA, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.HISTORICAL_VAR, ValueProperties.none());
//    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.JENSENS_ALPHA, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.SHARPE_RATIO, ValueProperties.none());
//    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.TOTAL_RISK_ALPHA, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.TREYNOR_RATIO, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.WEIGHT, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement("Default", EquitySecurity.SECURITY_TYPE, ValueRequirementNames.PNL, ValueProperties.none());
    return equityViewDefinition;
  }

  private void createSwapViewDefinition() {
    saveViewDefinition(makeSwapViewDefinition(DemoSwapPortfolioLoader.PORTFOLIO_NAME));
  }

  private void createEquityViewDefinition() {
    saveViewDefinition(makeEquityViewDefinition(DemoEquityPortfolioAndSecurityLoader.PORTFOLIO_NAME));
  }

  private ViewDefinition makeSwapViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, "Default");
    ValueProperties defaultProperties = ValueProperties.with("ForwardCurve", "SECONDARY").with("FundingCurve", "SECONDARY").with("Currency", "USD").get();
    defaultCalc.setDefaultProperties(defaultProperties);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PAR_RATE);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    defaultCalc.addSpecificRequirement(new ValueRequirement(
        ValueRequirementNames.YIELD_CURVE,
        ComputationTargetType.PRIMITIVE,
        UniqueId.of("CurrencyISO", "USD"),
        ValueProperties.with("Curve", "SECONDARY").get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalc);

    return viewDefinition;
  }

  private void createMultiCurrencySwapViewDefinition() {
    saveViewDefinition(getMultiCurrencySwapViewDefinition());
  }

  private ViewDefinition getMultiCurrencySwapViewDefinition() {
    UniqueId portfolioId = getPortfolioId(DemoMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME);
    ViewDefinition viewDefinition = new ViewDefinition(DemoMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, "PortfolioCurrency");
    ValueProperties defaultProperties = ValueProperties.with("ForwardCurve", "SECONDARY").with("FundingCurve", "SECONDARY").with("Currency", "USD").get();
    defaultCalc.setDefaultProperties(defaultProperties);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    for (Currency ccy : DemoMultiCurrencySwapPortfolioLoader.s_currencies) {
      defaultCalc.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.YIELD_CURVE,
          ComputationTargetType.PRIMITIVE, UniqueId.of("CurrencyISO", ccy.getCode()), ValueProperties.with("Curve", "SECONDARY").get()));
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalc);

    ViewCalculationConfiguration nativeCurrencyCalc = new ViewCalculationConfiguration(viewDefinition, "NativeCurrency");
    nativeCurrencyCalc.setDefaultProperties(ValueProperties.with("ForwardCurve", "SECONDARY").with("FundingCurve", "SECONDARY").get());
    nativeCurrencyCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01);
    nativeCurrencyCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    nativeCurrencyCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    viewDefinition.addViewCalculationConfiguration(nativeCurrencyCalc);

    return viewDefinition;
  }

  private void saveViewDefinition(ViewDefinition viewDefinition) {
    ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    configDocument.setName(viewDefinition.getName());
    configDocument.setValue(viewDefinition);
    ConfigMasterUtils.storeByName(_loaderContext.getConfigMaster(), configDocument);
  }

  //-------------------------------------------------------------------------
  /**
   * Sets up and loads the context.
   * <p>
   * This loader requires a Spring configuration file that defines the security,
   * position and portfolio masters, together with an instance of this bean
   * under the name "demoViewsPopulater".
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
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

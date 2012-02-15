/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.examples.tool.AbstractTool;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.money.Currency;

/**
 * Example code to create a set of example views.
 * <p>
 * It is designed to run against the HSQLDB example database.  
 */
public class ExampleViewsPopulater extends AbstractTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleViewsPopulater.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    if (init()) {
      new ExampleViewsPopulater().run();
    }
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    createEquityViewDefinition();
    createSwapViewDefinition();
    createMultiCurrencySwapViewDefinition();
  }

  private UniqueId getPortfolioId(String portfolioName) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult searchResult = getToolContext().getDbPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio" + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
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
    saveViewDefinition(makeSwapViewDefinition(ExampleSwapPortfolioLoader.PORTFOLIO_NAME));
  }

  private void createEquityViewDefinition() {
    saveViewDefinition(makeEquityViewDefinition(ExampleEquityPortfolioAndSecurityLoader.PORTFOLIO_NAME));
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
    UniqueId portfolioId = getPortfolioId(ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME);
    ViewDefinition viewDefinition = new ViewDefinition(ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME + " View", portfolioId, UserPrincipal.getTestUser());
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
    for (Currency ccy : ExampleMultiCurrencySwapPortfolioLoader.s_currencies) {
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
    ConfigMasterUtils.storeByName(getToolContext().getDbConfigMaster(), configDocument);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bloombergexample.tool.ExampleDatabasePopulator;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.money.Currency;

/**
 * Example code to create a set of example views.
 * <p>
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleViewsPopulator extends AbstractTool<IntegrationToolContext> {

  private static final String FORWARD_3M = "FORWARD_3M";
  private static final String FORWARD_6M = "FORWARD_6M";
  private static final String FUNDING = "FUNDING";
  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleViewsPopulator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool. No arguments are needed.
   * 
   * @param args the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    new ExampleViewsPopulator().initAndRun(args, IntegrationToolContext.class);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
    storeViewDefinition(getFXViewDefinition(ExampleDatabasePopulator.FX_PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDefinition(ExampleMultiCurrencySwapPortfolioLoader.PORTFOLIO_NAME));
    storeViewDefinition(getEquityOptionViewDefinition(DemoEquityOptionCollarPortfolioLoader.PORTFOLIO_NAME));
  }

  private ViewDefinition getEquityOptionViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    ViewDefinition equityViewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    equityViewDefinition.setDefaultCurrency(Currency.USD);
    equityViewDefinition.setMaxFullCalculationPeriod(30000L);
    equityViewDefinition.setMinFullCalculationPeriod(500L);
    equityViewDefinition.setMinDeltaCalculationPeriod(500L);
    equityViewDefinition.setMaxDeltaCalculationPeriod(30000L);
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquityOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquityOptionSecurity.SECURITY_TYPE, ValueRequirementNames.HISTORICAL_VAR, ValueProperties.none());

    return equityViewDefinition;
  }

  private ViewDefinition getEquityViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    ViewDefinition equityViewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    equityViewDefinition.setDefaultCurrency(Currency.USD);
    equityViewDefinition.setMaxFullCalculationPeriod(30000L);
    equityViewDefinition.setMinFullCalculationPeriod(500L);
    equityViewDefinition.setMinDeltaCalculationPeriod(500L);
    equityViewDefinition.setMaxDeltaCalculationPeriod(30000L);
    equityViewDefinition.addPortfolioRequirement(DEFAULT_CALC_CONFIG, EquitySecurity.SECURITY_TYPE, ValueRequirementNames.FAIR_VALUE, ValueProperties.none());
    return equityViewDefinition;
  }

  private UniqueId getPortfolioId(String portfolioName) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio" + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  private ViewDefinition getMultiCurrencySwapViewDefinition(final String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, "Default");
    ValueProperties defaultProperties = ValueProperties.with("ForwardCurve", "DEFAULT").with("FundingCurve", "DEFAULT").with("Currency", "USD").get();
    defaultCalc.setDefaultProperties(defaultProperties);

    ValueProperties fundingCurveSpecificProperties = ValueProperties.with("Curve", FUNDING).get();
    ValueProperties forwardCurve3MSpecificProperties = ValueProperties.with("Curve", FORWARD_3M).get();
    ValueProperties forwardCurve6MSpecificProperties = ValueProperties.with("Curve", FORWARD_6M).get();

    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, forwardCurve3MSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PV01, forwardCurve6MSpecificProperties);

    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, fundingCurveSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, forwardCurve3MSpecificProperties);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, forwardCurve6MSpecificProperties);
    for (Currency ccy : ExampleMultiCurrencySwapPortfolioLoader.s_currencies) {
      defaultCalc.addSpecificRequirement(new ValueRequirement(
          ValueRequirementNames.YIELD_CURVE,
          ComputationTargetSpecification.of(ccy),
          ValueProperties.with("Curve", FUNDING).get()));
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalc);

    return viewDefinition;
  }

  private ViewDefinition getFXViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    defaultCalc.addPortfolioRequirementName(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VALUE_VEGA);
    defaultCalc.addPortfolioRequirementName(FXBarrierOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VALUE_VEGA);

    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private void storeViewDefinition(ViewDefinition viewDefinition) {
    ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    configDocument.setName(viewDefinition.getName());
    configDocument.setValue(viewDefinition);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), configDocument);
  }

}

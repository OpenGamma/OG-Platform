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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.examples.tool.AbstractExampleTool;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
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
public class ExampleViewsPopulater extends AbstractExampleTool {

  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleViewsPopulater.class);
  
  private static final Currency[] s_currencies = new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.NZD, Currency.DKK};

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    new ExampleViewsPopulater().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    createEquityViewDefinition();
    createSwapViewDefinition();
    createMultiCurrencySwapViewDefinition();
    createMixedPortfolioViewDefinition();
  }

  private void createEquityViewDefinition() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
  }

  private void createSwapViewDefinition() {
    storeViewDefinition(getSwapViewDefinition(ExampleSwapPortfolioLoader.PORTFOLIO_NAME));
  }

  private void createMultiCurrencySwapViewDefinition() {
    storeViewDefinition(getMultiCurrencySwapViewDefinition());
  }

  private void createMixedPortfolioViewDefinition() {
    storeViewDefinition(getMultiAssetPortfolioViewDefinition());
  }

  private ViewDefinition getMultiAssetPortfolioViewDefinition() {
    UniqueId portfolioId = getPortfolioId(ExampleMultiAssetPortfolioLoader.PORTFOLIO_NAME);
    ViewDefinition viewDefinition = new ViewDefinition(ExampleMultiAssetPortfolioLoader.PORTFOLIO_NAME + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    
    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties secondaryCurveProp = ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").get();
    /*
    final ValueProperties fundingProp = ValueProperties.with(ValuePropertyNames.CURVE, FUNDING).get();
    final ValueProperties forward3MProp = ValueProperties.with(ValuePropertyNames.CURVE, FORWARD_3M).get();
    final ValueProperties forward6MProp = ValueProperties.with(ValuePropertyNames.CURVE, FORWARD_6M).get();
    */
    /*
    //BOND
    defaultCalc.addPortfolioRequirementName(BondSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.GBP.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get()); */
    //CAP-FLOOR
    defaultCalc.addPortfolioRequirementName(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
        secondaryCurveProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        secondaryCurveProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(CapFloorSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_CUBE,
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "SABRNoExtrapolation").with("InstrumentType", "SWAPTION_CUBE").get());
    /*
    //EQUITY VARIANCE SWAP
    defaultCalc.addPortfolioRequirementName(EquityVarianceSwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(EquityVarianceSwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(EquityVarianceSwapSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX, 
        ValueProperties.with("InstrumentType", "EQUITY_OPTION").get());
    //FRA
    defaultCalc.addPortfolioRequirementName(FRASecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FRASecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FRASecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //FUTURE
    defaultCalc.addPortfolioRequirementName(FutureSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, fundingProp);*/
    //FX_FORWARD
    defaultCalc.addPortfolioRequirementName(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        secondaryCurveProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.CHF.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        secondaryCurveProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.EUR.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        secondaryCurveProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    //FX_OPTION
    defaultCalc.addPortfolioRequirementName(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
//    defaultCalc.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE, 
//        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "BlackMethod").get());
    defaultCalc.addPortfolioRequirement(
        FXOptionSecurity.SECURITY_TYPE,
        ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        secondaryCurveProp.copy().with(ValuePropertyNames.CALCULATION_METHOD, "BlackMethod").with(ValuePropertyNames.CURVE_CURRENCY, Currency.EUR.getCode())
            .withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(
        FXOptionSecurity.SECURITY_TYPE,
        ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        secondaryCurveProp.copy().with(ValuePropertyNames.CALCULATION_METHOD, "BlackMethod").with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode())
            .withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX, 
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "BlackMethod").with("InstrumentType", "FX_VANILLA_OPTION").get());
    /*
    //IRFUTURE_OPTION
    defaultCalc.addPortfolioRequirementName(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_MATRIX, 
        ValueProperties.with("InstrumentType", "IR_FUTURE_OPTION").get());
    defaultCalc.addPortfolioRequirement(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        forward3MProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    defaultCalc.addPortfolioRequirement(IRFutureOptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
        fundingProp.copy().with(ValuePropertyNames.CURVE_CURRENCY, Currency.USD.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
        */
    //SWAP
    defaultCalc.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_CUBE, 
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "SABRNoExtrapolation").with("InstrumentType", "SWAPTION_CUBE").get());
    defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").get());
    for (Currency ccy : ExampleMultiAssetPortfolioLoader.s_currencies) {
      defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
          ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    }

    //SWAPTION
    defaultCalc.addPortfolioRequirementName(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    defaultCalc.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.VEGA_QUOTE_CUBE, 
        ValueProperties.with(ValuePropertyNames.CALCULATION_METHOD, "SABRNoExtrapolation").with("InstrumentType", "SWAPTION_CUBE").get());
    defaultCalc.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").get());
    for (Currency ccy : ExampleMultiAssetPortfolioLoader.s_currencies) {
      defaultCalc.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
          ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).withOptional(ValuePropertyNames.CURVE_CURRENCY).get());
    }
    
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private ViewDefinition getEquityViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxFullCalculationPeriod(30000L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMaxDeltaCalculationPeriod(30000L);
    
    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalc, EquitySecurity.SECURITY_TYPE, new String[] {ValueRequirementNames.FAIR_VALUE, ValueRequirementNames.CAPM_BETA, ValueRequirementNames.HISTORICAL_VAR,
      ValueRequirementNames.SHARPE_RATIO, ValueRequirementNames.TREYNOR_RATIO, ValueRequirementNames.JENSENS_ALPHA, ValueRequirementNames.TOTAL_RISK_ALPHA});
    defaultCalc.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, ValueRequirementNames.PNL, 
        ValueProperties.with(ValuePropertyNames.CURRENCY, Currency.USD.getCode()).get());
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType, final String[] valueRequirementNames) {
    for (String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirementName(securityType, valueRequirementName);
    }
  }
  
  private void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType, String[] valueRequirementNames, final ValueProperties valueProperties) {
    for (String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirement(securityType, valueRequirementName, valueProperties);
    }
  }

  private UniqueId getPortfolioId(String portfolioName) {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio " + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  private ViewDefinition getSwapViewDefinition(String portfolioName) {
    UniqueId portfolioId = getPortfolioId(portfolioName);
    ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalc, SwapSecurity.SECURITY_TYPE, new String[] {ValueRequirementNames.PAR_RATE, ValueRequirementNames.PRESENT_VALUE});
    addValueRequirements(defaultCalc, SwapSecurity.SECURITY_TYPE, new String[] {ValueRequirementNames.PV01, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, 
      ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES}, ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").get());
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDefinition() {
    UniqueId portfolioId = getPortfolioId("MultiCurrency Swap Portfolio");
    ViewDefinition viewDefinition = new ViewDefinition("MultiCurrency Swap View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.PRESENT_VALUE);
    addValueRequirements(defaultCalConfig, SwapSecurity.SECURITY_TYPE, new String[] {ValueRequirementNames.PV01}, ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").get());
    for (Currency ccy : s_currencies) {
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, 
          ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).get());
      
      defaultCalConfig.addSpecificRequirement(new ValueRequirement(
          ValueRequirementNames.YIELD_CURVE,
          ComputationTargetType.PRIMITIVE,
          UniqueId.of("CurrencyISO", ccy.getCode()),
          ValueProperties.with(ValuePropertyNames.CURVE, "SECONDARY").get()));
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private void storeViewDefinition(ViewDefinition viewDefinition) {
    ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    configDocument.setName(viewDefinition.getName());
    configDocument.setValue(viewDefinition);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), configDocument);
  }

}

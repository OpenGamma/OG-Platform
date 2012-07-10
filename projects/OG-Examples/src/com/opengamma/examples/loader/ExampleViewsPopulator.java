/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_JACOBIAN;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.examples.tool.ExampleDatabasePopulator.AUD_SWAP_PORFOLIO_NAME;
import static com.opengamma.examples.tool.ExampleDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME;

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
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueId;
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
public class ExampleViewsPopulator extends AbstractExampleTool {

  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleViewsPopulator.class);

  private static final Currency[] s_swapCurrencies = new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF};
  private static final String[] s_curveConfigNames = new String[] {"DefaultTwoCurveUSDConfig", "DefaultTwoCurveGBPConfig", "DefaultTwoCurveEURConfig",
    "DefaultTwoCurveJPYConfig", "DefaultTwoCurveCHFConfig"};

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleViewsPopulator().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDeprecatedDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getAUDBasisSwapView1Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getAUDBasisSwapView2Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getTempDefinition(AUD_SWAP_PORFOLIO_NAME));
  }

  private ViewDefinition getEquityViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName);
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxFullCalculationPeriod(30000L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMaxDeltaCalculationPeriod(30000L);

    final ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalc, EquitySecurity.SECURITY_TYPE,
        new String[] {
        ValueRequirementNames.FAIR_VALUE,
        ValueRequirementNames.CAPM_BETA,
        ValueRequirementNames.HISTORICAL_VAR,
        ValueRequirementNames.SHARPE_RATIO,
        ValueRequirementNames.TREYNOR_RATIO,
        ValueRequirementNames.JENSENS_ALPHA,
        ValueRequirementNames.TOTAL_RISK_ALPHA});
    defaultCalc.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, ValueRequirementNames.PNL, ValueProperties.with(ValuePropertyNames.CURRENCY, Currency.USD.getCode()).get());
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDeprecatedDefinition(final String swapPortfolioName) {
    final UniqueId portfolioId = getPortfolioId(swapPortfolioName);
    final ViewDefinition viewDefinition = new ViewDefinition("Multi-currency Swap View (Deprecated)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);

    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties defaultProperties = ValueProperties.with("FundingCurve", "SECONDARY").get();
    defaultCalConfig.setDefaultProperties(defaultProperties);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    addValueRequirements(defaultCalConfig, SwapSecurity.SECURITY_TYPE, new String[] {PV01}, ValueProperties.with(CURVE, "SECONDARY").get());
    for (final Currency ccy : s_swapCurrencies) {
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
          ValueProperties.with(CURVE, "SECONDARY").with(CURVE_CURRENCY, ccy.getCode()).get());
      defaultCalConfig.addSpecificRequirement(
          new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, ccy.getCode()), ValueProperties.with(CURVE, "SECONDARY").get()));
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName);
    final ViewDefinition viewDefinition = new ViewDefinition("Multi-currency Swap View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURRENCY, "USD").get());
    for (int i = 0; i < s_swapCurrencies.length; i++) {
      final String ccy = s_swapCurrencies[i].getCode();
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
          ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccy).get());
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
          ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccy).get());
      defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, ccy),
          ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      if (s_swapCurrencies[i].getCode().equals("USD")) {
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
            ValueProperties.with(CURVE, "Forward3M").with(CURVE_CURRENCY, ccy).get());
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
            ValueProperties.with(CURVE, "Forward3M").with(CURVE_CURRENCY, ccy).get());
        defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, ccy),
            ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      } else {
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
            ValueProperties.with(CURVE, "Forward6M").with(CURVE_CURRENCY, ccy).get());
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
            ValueProperties.with(CURVE, "Forward6M").with(ValuePropertyNames.CURVE_CURRENCY, ccy).get());
        defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, ccy),
            ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      }
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getAUDBasisSwapView1Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName);
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Basis Swap (1)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String threeCurveConfig = "DefaultThreeCurveAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
    //        ValueProperties.with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
    //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
    //        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
    //        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
    //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
    //        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
    //        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getAUDBasisSwapView2Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName);
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Basis Swap (2)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String threeCurveConfig = "ForwardFromDiscountingAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, threeCurveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getTempDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName);
    final ViewDefinition viewDefinition = new ViewDefinition("Test", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, "AUD"),
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, "DefaultThreeCurveAUDConfig").get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, "AUD"),
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, "DefaultThreeCurveAUDConfig").get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetType.PRIMITIVE, UniqueId.of(Currency.OBJECT_SCHEME, "AUD"),
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, "DefaultThreeCurveAUDConfig").get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType, final String[] valueRequirementNames) {
    for (final String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirementName(securityType, valueRequirementName);
    }
  }

  private void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType, final String[] valueRequirementNames, final ValueProperties valueProperties) {
    for (final String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirement(securityType, valueRequirementName, valueProperties);
    }
  }

  private UniqueId getPortfolioId(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio " + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  private void storeViewDefinition(final ViewDefinition viewDefinition) {
    final ConfigDocument<ViewDefinition> configDocument = new ConfigDocument<ViewDefinition>(ViewDefinition.class);
    configDocument.setName(viewDefinition.getName());
    configDocument.setValue(viewDefinition);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), configDocument);
  }

}

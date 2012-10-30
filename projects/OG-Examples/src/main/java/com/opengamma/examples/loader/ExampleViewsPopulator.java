/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_JACOBIAN;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.examples.tool.ExampleDatabasePopulator.AUD_SWAP_PORFOLIO_NAME;
import static com.opengamma.examples.tool.ExampleDatabasePopulator.MIXED_CMS_PORTFOLIO_NAME;
import static com.opengamma.examples.tool.ExampleDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.tool.ExampleDatabasePopulator.SWAPTION_PORTFOLIO_NAME;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.model.sabrcube.SABRFunction;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
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
public class ExampleViewsPopulator extends AbstractTool<ToolContext> {

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
    new ExampleViewsPopulator().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDeprecatedDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getAUDSwapView1Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getAUDSwapView2Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getAUDSwapView3Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getSwaptionParityViewDefinition(SWAPTION_PORTFOLIO_NAME));
    storeViewDefinition(getSABRExtrapolationViewDefinition(MIXED_CMS_PORTFOLIO_NAME));
//    storeViewDefinition(getFXLocalVolatilityViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME));
  }

  private ViewDefinition getEquityViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxFullCalculationPeriod(30000L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMaxDeltaCalculationPeriod(30000L);

    final ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalc, EquitySecurity.SECURITY_TYPE, 
        new String[] {ValueRequirementNames.FAIR_VALUE, ValueRequirementNames.CAPM_BETA, ValueRequirementNames.HISTORICAL_VAR,
          ValueRequirementNames.SHARPE_RATIO, ValueRequirementNames.TREYNOR_RATIO, ValueRequirementNames.JENSENS_ALPHA, 
          ValueRequirementNames.TOTAL_RISK_ALPHA });
    defaultCalc.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, ValueRequirementNames.PNL, ValueProperties.with(ValuePropertyNames.CURRENCY, Currency.USD.getCode()).get());
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDeprecatedDefinition(final String swapPortfolioName) {
    final UniqueId portfolioId = getPortfolioId(swapPortfolioName).toLatest();
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
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
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

  private ViewDefinition getAUDSwapView1Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (3m / 6m basis) (1)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig = "DefaultThreeCurveAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getAUDSwapView2Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (3m / 6m basis) (2)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig = "ForwardFromDiscountingAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getAUDSwapView3Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (no basis)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig = "SingleAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetType.PRIMITIVE, Currency.AUD.getUniqueId(),
        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getSABRExtrapolationViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("Mixed CM", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig = "DefaultTwoCurveUSDConfig";
    final ViewCalculationConfiguration noExtrapolationConfig = new ViewCalculationConfiguration(viewDefinition, "No Extrapolation");
    final ViewCalculationConfiguration rightExtrapolationConfig = new ViewCalculationConfiguration(viewDefinition, "Right Extrapolation");
    final String[] securityTypes = new String[] {CapFloorCMSSpreadSecurity.SECURITY_TYPE, CapFloorSecurity.SECURITY_TYPE, SwapSecurity.SECURITY_TYPE};
    for (final String securityType : securityTypes) {
      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION).get());
      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION).get());
      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_NU_SENSITIVITY,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION).get());
      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_RHO_SENSITIVITY,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION).get());
      noExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
          ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION).get());
      noExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
          ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION).get());
      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).get());
      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).get());
      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_NU_SENSITIVITY,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).get());
      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_RHO_SENSITIVITY,
          ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).get());
      rightExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
          ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).get());
      rightExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
          ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).get());
    }
    viewDefinition.addViewCalculationConfiguration(noExtrapolationConfig);
    viewDefinition.addViewCalculationConfiguration(rightExtrapolationConfig);
    return viewDefinition;
  }

  private ViewDefinition getSwaptionParityViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("Swap / Swaption Parity", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig = "DefaultTwoCurveUSDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);

    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());

    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());

    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());

    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.USD.getUniqueId(),
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetType.PRIMITIVE, Currency.USD.getUniqueId(),
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    return viewDefinition;
  }

//  private ViewDefinition getFXLocalVolatilityViewDefinition(final String portfolioName) {
//    final UniqueId portfolioId = getPortfolioId(portfolioName);
//    final ViewDefinition viewDefinition = new ViewDefinition("Local Volatility", portfolioId, UserPrincipal.getTestUser());
//    viewDefinition.setDefaultCurrency(Currency.USD);
//    viewDefinition.setMaxDeltaCalculationPeriod(500L);
//    viewDefinition.setMaxFullCalculationPeriod(500L);
//    viewDefinition.setMinDeltaCalculationPeriod(500L);
//    viewDefinition.setMinFullCalculationPeriod(500L);
//    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, "LV");
//    final UniqueId pairId = UnorderedCurrencyPair.of(Currency.USD, Currency.EUR).getUniqueId();
//    final ValueProperties properties = ValueProperties.builder()
//        .with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.SABR)
//        .with(CALCULATION_METHOD, "LocalVolatilityPDE")
//        .with("PDEDirection", "Forward").get();
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_PRESENT_VALUE, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FORWARD_DELTA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_FORWARD_DELTA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FORWARD_GAMMA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_FORWARD_GAMMA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FORWARD_VEGA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_FORWARD_VEGA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FORWARD_VANNA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_FORWARD_VANNA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.FORWARD_VOMMA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_FORWARD_VOMMA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.DUAL_DELTA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_DUAL_DELTA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.DUAL_GAMMA, properties);
//    defaultCalConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, ValueRequirementNames.GRID_DUAL_GAMMA, properties);
//    defaultCalConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, pairId,
//        ValueProperties.with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.SABR).get()));
//    defaultCalConfig.addSpecificRequirement(new ValueRequirement(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, pairId,
//        ValueProperties.with(BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR, BlackVolatilitySurfacePropertyNamesAndValues.SABR).get()));
//    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
//    return viewDefinition;
//  }

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
    final ConfigItem<ViewDefinition> config = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
  }

}

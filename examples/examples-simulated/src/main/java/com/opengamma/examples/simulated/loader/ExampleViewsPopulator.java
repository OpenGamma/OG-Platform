/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import static com.opengamma.engine.value.ValueRequirementNames.ASSET_LEG_PV;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
>>>>>>> 7decf75... [PLAT-6345] Adding more outputs for equity and bond TRS
=======
import static com.opengamma.engine.value.ValueRequirementNames.ASSET_LEG_PV;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_EQUIVALENT_VALUE;
>>>>>>> a8c2f08... Revert "Revert "[PLAT-5345] Adding bond TRS analytics to examples-simulated""
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.FAIR_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD;
import static com.opengamma.engine.value.ValueRequirementNames.FUNDING_LEG_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.FUNDING_LEG_PV;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.GAMMA_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.NOTIONAL;
import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.engine.value.ValueRequirementNames.RHO;
import static com.opengamma.engine.value.ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY;
import static com.opengamma.engine.value.ValueRequirementNames.THETA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA_P;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_PHI;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_RHO;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_THETA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VANNA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VOMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA_QUOTE_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_JACOBIAN;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.YTM;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.AUD_SWAP_PORFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.BOND_TRS_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.EQUITY_OPTION_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.EQUITY_TRS_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.EUR_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.FUTURE_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.FX_FORWARD_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.FX_VOLATILITY_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.MIXED_CMS_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.SWAPTION_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.US_GOVERNMENT_BOND_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.VANILLA_FX_OPTION_PORTFOLIO_NAME;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_REALIZED_VARIANCE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_REALIZED_VARIANCE_METHOD;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutputAggregationType;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.bond.BondFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRFunction;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Example code to create a set of example views.
 * <p>
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleViewsPopulator extends AbstractTool<ToolContext> {

  /** Name of the default calculation configurations */
  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleViewsPopulator.class);
  /** A list of currencies */
  private static final Currency[] s_swapCurrencies = new Currency[] {
    Currency.USD,
    Currency.GBP,
    Currency.EUR,
    Currency.JPY,
    Currency.CHF };
  /** A list of curve configuration names */
  private static final String[] s_curveConfigNames = new String[] {
    "DefaultTwoCurveUSDConfig",
    "DefaultTwoCurveGBPConfig",
    "DefaultTwoCurveEURConfig",
    "DefaultTwoCurveJPYConfig",
    "DefaultTwoCurveCHFConfig" };
  /** A list of currency pairs */
  public static final UnorderedCurrencyPair[] CURRENCY_PAIRS = new UnorderedCurrencyPair[] {
    UnorderedCurrencyPair.of(Currency.USD, Currency.EUR),
    UnorderedCurrencyPair.of(Currency.USD, Currency.CHF),
    UnorderedCurrencyPair.of(Currency.USD, Currency.AUD),
    UnorderedCurrencyPair.of(Currency.USD, Currency.GBP),
    UnorderedCurrencyPair.of(Currency.USD, Currency.JPY),
    UnorderedCurrencyPair.of(Currency.GBP, Currency.EUR),
    UnorderedCurrencyPair.of(Currency.CHF, Currency.JPY) };
  /** Map of currencies to swaption surface / cube names */
  public static final Map<Currency, String> SWAPTION_CURRENCY_CONFIGS = new HashMap<>();
  /** Map of countries to swaption surface / cube names */
  public static final Map<String, String> SWAPTION_COUNTRY_CONFIGS = new HashMap<>();
  /** Map of currencies to curves */
  public static final Map<Currency, Pair<String, String>> SWAPTION_CURVES = new HashMap<>();
  /** List of (curve construction configuration, curve definition) pairs for bond TRS issuers */
  private static final List<Pair<String, String>> BOND_TRS_ISSUER_CURVES = new ArrayList<>();

  static {
    SWAPTION_CURRENCY_CONFIGS.put(Currency.USD, "PROVIDER1");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.GBP, "PROVIDER1");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.EUR, "PROVIDER2");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.JPY, "PROVIDER3");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.CHF, "PROVIDER2");
    SWAPTION_COUNTRY_CONFIGS.put("US", "FWD SWAP PROVIDER1");
    SWAPTION_COUNTRY_CONFIGS.put("GB", "FWD SWAP PROVIDER1");
    SWAPTION_COUNTRY_CONFIGS.put("EU", "FWD SWAP PROVIDER2");
    SWAPTION_COUNTRY_CONFIGS.put("JP", "FWD SWAP PROVIDER3");
    SWAPTION_COUNTRY_CONFIGS.put("SF", "FWD_SWAP PROVIDER2");
    SWAPTION_CURVES.put(Currency.USD, Pairs.of("Discounting", "Forward3M"));
    SWAPTION_CURVES.put(Currency.GBP, Pairs.of("Discounting", "Forward6M"));
    SWAPTION_CURVES.put(Currency.EUR, Pairs.of("Discounting", "Forward6M"));
    SWAPTION_CURVES.put(Currency.JPY, Pairs.of("Discounting", "Forward6M"));
    SWAPTION_CURVES.put(Currency.CHF, Pairs.of("Discounting", "Forward6M"));
    BOND_TRS_ISSUER_CURVES.add(Pairs.of("UG Government Bond Configuration", "UG Government Curve"));
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleViewsPopulator().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDefinitionWithSeparateOutputs(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getAUDSwapView1Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getAUDSwapView2Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getAUDSwapView3Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getSwaptionParityViewDefinition(SWAPTION_PORTFOLIO_NAME));
    storeViewDefinition(getFXOptionViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option View"));
    storeViewDefinition(getFXOptionGreeksViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option Greeks View"));
    storeViewDefinition(getATMSwaptionViewDefinition(MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME, "Swaption Black Pricing View"));
    storeViewDefinition(getSABRExtrapolationViewDefinition(MIXED_CMS_PORTFOLIO_NAME));
    storeViewDefinition(getEURFixedIncomeViewDefinition(EUR_SWAP_PORTFOLIO_NAME, "EUR Swap Desk View"));
    storeViewDefinition(getFXForwardViewDefinition(FX_FORWARD_PORTFOLIO_NAME, "FX Forward View"));
    storeViewDefinition(getEquityOptionViewDefinition(EQUITY_OPTION_PORTFOLIO_NAME, "Equity Option View"));
    storeViewDefinition(getFutureViewDefinition(FUTURE_PORTFOLIO_NAME, "Futures View"));
    storeViewDefinition(getBondViewDefinition(US_GOVERNMENT_BOND_PORTFOLIO_NAME, "Government Bond View"));
    storeViewDefinition(getFXVolatilitySwapViewDefinition(FX_VOLATILITY_SWAP_PORTFOLIO_NAME, "FX Volatility Swap View"));
    storeViewDefinition(getBondTotalReturnSwapViewDefinition(BOND_TRS_PORTFOLIO_NAME, "Bond TRS View"));
    storeViewDefinition(getEquityTotalReturnSwapViewDefinition(EQUITY_TRS_PORTFOLIO_NAME, "Equity TRS View"));
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
    addValueRequirements(defaultCalc, EquitySecurity.SECURITY_TYPE, new String[] {ValueRequirementNames.FAIR_VALUE, ValueRequirementNames.CAPM_BETA, ValueRequirementNames.HISTORICAL_VAR,
      ValueRequirementNames.SHARPE_RATIO, ValueRequirementNames.TREYNOR_RATIO, ValueRequirementNames.JENSENS_ALPHA, ValueRequirementNames.TOTAL_RISK_ALPHA, ValueRequirementNames.PNL });
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDefinitionWithSeparateOutputs(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View (outputs by ccy)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    // The name "Default" has no special meaning, but means that the currency conversion function can never be used and so we get the instrument's natural currency
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default").withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
    for (int i = 0; i < s_swapCurrencies.length; i++) {
      final Currency ccy = s_swapCurrencies[i];
      final String ccyCode = ccy.getCode();
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
      defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy), ValueProperties.with(CURVE, "Discounting")
          .with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      if (ccyCode.equals("USD")) {
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward3M").with(CURVE_CURRENCY, ccyCode).get());
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, ValueProperties.with(CURVE, "Forward3M").with(CURVE_CURRENCY, ccyCode).get());
        defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy), ValueProperties.with(CURVE, "Forward3M")
            .with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      } else {
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, ValueProperties.with(CURVE, "Forward6M").with(CURVE_CURRENCY, ccyCode).get());
        defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward6M").with(ValuePropertyNames.CURVE_CURRENCY, ccyCode)
            .get());
        defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy), ValueProperties.with(CURVE, "Forward6M")
            .with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      }
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getMultiCurrencySwapViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalcConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    // The name "Default" has no special meaning, but means that the currency conversion function can never be used and so we get the instrument's natural currency
    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default").withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
    final MergedOutput discountingPV01Output = new MergedOutput("Discounting PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput discountingYCNSOutput = new MergedOutput("Discounting Bucketed PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput forwardPV01Output = new MergedOutput("Forward PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput forwardYCNSOutput = new MergedOutput("Forward Bucketed PV01", MergedOutputAggregationType.LINEAR);
    for (int i = 0; i < s_swapCurrencies.length; i++) {
      final Currency ccy = s_swapCurrencies[i];
      final String ccyCode = ccy.getCode();
      discountingPV01Output.addMergedRequirement(PV01, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
      discountingYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy), ValueProperties.with(CURVE, "Discounting")
          .with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));

      if (ccyCode.equals("USD")) {
        forwardYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward3M").with(CURVE_CURRENCY, ccyCode).get());
        forwardPV01Output.addMergedRequirement(PV01, ValueProperties.with(CURVE, "Forward3M").with(CURVE_CURRENCY, ccyCode).get());
        defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy), ValueProperties.with(CURVE, "Forward3M")
            .with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      } else {
        forwardYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward6M").with(ValuePropertyNames.CURVE_CURRENCY, ccyCode).get());
        forwardPV01Output.addMergedRequirement(PV01, ValueProperties.with(CURVE, "Forward6M").with(CURVE_CURRENCY, ccyCode).get());
        defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy), ValueProperties.with(CURVE, "Forward6M")
            .with(CURVE_CALCULATION_CONFIG, s_curveConfigNames[i]).get()));
      }
    }
    defaultCalcConfig.addMergedOutput(discountingPV01Output);
    defaultCalcConfig.addMergedOutput(discountingYCNSOutput);
    defaultCalcConfig.addMergedOutput(forwardPV01Output);
    defaultCalcConfig.addMergedOutput(forwardYCNSOutput);
    viewDefinition.addViewCalculationConfiguration(defaultCalcConfig);
    return viewDefinition;
  }

  private ViewDefinition getFXOptionViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final Set<Currency> ccysAdded = new HashSet<>();
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(pair.getUniqueId());
      final ValueProperties properties = ValueProperties.builder().with(SURFACE, "DEFAULT").with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX).get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, properties));
      defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VEGA_QUOTE_MATRIX, properties);
      defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VEGA_MATRIX, properties);
      if (!ccysAdded.contains(pair.getFirstCurrency())) {
        final String ccy = pair.getFirstCurrency().getCode();
        final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, "Discounting").with(CURVE_CURRENCY, ccy).get();
        defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
        ccysAdded.add(pair.getFirstCurrency());
      }
      if (!ccysAdded.contains(pair.getSecondCurrency())) {
        final String ccy = pair.getSecondCurrency().getCode();
        final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, "Discounting").with(CURVE_CURRENCY, ccy).get();
        defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
        ccysAdded.add(pair.getSecondCurrency());
      }
    }
    final ValueProperties currencyProperty = ValueProperties.builder().with(ValuePropertyNames.CURRENCY, "USD").get();
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  private ViewDefinition getFXOptionGreeksViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties currencyProperty = ValueProperties.builder().with(ValuePropertyNames.CURRENCY, "USD").get();
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_DELTA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VEGA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_GAMMA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_GAMMA_P, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_RHO, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_PHI, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VOMMA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VANNA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_THETA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, SECURITY_IMPLIED_VOLATILITY, ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for FX volatility swaps that requests the surface data
   * for each currency pair and the fair value for each swap.
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getFXVolatilitySwapViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(pair.getUniqueId());
      final ValueProperties properties = ValueProperties.builder()
          .with(SURFACE, "DEFAULT")
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
          .get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, properties));
    }
    final ValueProperties properties = ValueProperties.builder()
        .with(SURFACE, "DEFAULT")
        .with(X_INTERPOLATOR_NAME, Interpolator1DFactory.LINEAR)
        .with(LEFT_X_EXTRAPOLATOR_NAME, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)
        .with(RIGHT_X_EXTRAPOLATOR_NAME, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)
        .with(PROPERTY_REALIZED_VARIANCE_METHOD, HISTORICAL_REALIZED_VARIANCE)
        .with(CURVE_EXPOSURES, "Exposures")
        .get();
    defaultCalculationConfig.addPortfolioRequirement(FXVolatilitySwapSecurity.SECURITY_TYPE, FAIR_VALUE, properties);
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
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
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig)
        .get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig)
        .get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "Discounting")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "ForwardBasis3M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "ForwardBasis6M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
        .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
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
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig)
        .get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig)
        .get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "Discounting")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "ForwardBasis3M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "ForwardBasis6M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
        .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
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
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE, "Single")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(Currency.AUD), ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
        .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getATMSwaptionViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    for (final Map.Entry<Currency, String> entry : SWAPTION_CURRENCY_CONFIGS.entrySet()) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(entry.getKey().getUniqueId());
      final ValueProperties properties = ValueProperties.builder().with(SURFACE, entry.getValue())
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM).get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, properties));
    }
    for (final Map.Entry<Currency, Pair<String, String>> entry : SWAPTION_CURVES.entrySet()) {
      ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, entry.getValue().getFirst()).with(ValuePropertyNames.CURVE_CURRENCY, entry.getKey().getCode())
          .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).get();
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, properties);
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, BUCKETED_PV01, properties);
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PV01, properties);
      properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, entry.getValue().getSecond()).with(ValuePropertyNames.CURVE_CURRENCY, entry.getKey().getCode())
          .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).get();
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, properties);
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PV01, properties);
    }
    final ValueProperties calculationMethodProperty = ValueProperties.builder().with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD).get();
    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE, calculationMethodProperty);
    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, VALUE_VEGA, calculationMethodProperty);
    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, SECURITY_IMPLIED_VOLATILITY, ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  private ViewDefinition getSABRExtrapolationViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("Constant Maturity Swap View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig = "DefaultTwoCurveUSDConfig";
    final ViewCalculationConfiguration noExtrapolationConfig = new ViewCalculationConfiguration(viewDefinition, "No Extrapolation");
    final ViewCalculationConfiguration rightExtrapolationConfig = new ViewCalculationConfiguration(viewDefinition, "Right Extrapolation");
    final String[] securityTypes = new String[] {CapFloorCMSSpreadSecurity.SECURITY_TYPE, CapFloorSecurity.SECURITY_TYPE, SwapSecurity.SECURITY_TYPE };
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
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig)
        .with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());
    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());

    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, BUCKETED_PV01,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig).with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION).withOptional(CALCULATION_METHOD).get());

    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.USD), ValueProperties.with(CURVE, "Discounting")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.USD), ValueProperties.with(CURVE, "Forward3M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    return viewDefinition;
  }

  private ViewDefinition getEURFixedIncomeViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.EUR);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final String curveConfig1 = "EUR-OIS-3M-6M";
    final String curveConfig2 = "EUR-OIS-3MFut-6M";
    final ViewCalculationConfiguration firstConfig = new ViewCalculationConfiguration(viewDefinition, "EUR-OIS-3M-6M");
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.EUR), ValueProperties.with(CURVE, "Discounting")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.EUR), ValueProperties.with(CURVE, "Forward3M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.EUR), ValueProperties.with(CURVE, "Forward6M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
    viewDefinition.addViewCalculationConfiguration(firstConfig);
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    viewDefinition.addViewCalculationConfiguration(firstConfig);
    final ViewCalculationConfiguration secondConfig = new ViewCalculationConfiguration(viewDefinition, "EUR-OIS-3MFut-6M");
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.EUR), ValueProperties.with(CURVE, "Discounting")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.EUR), ValueProperties.with(CURVE, "Forward3MFut")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(Currency.EUR), ValueProperties.with(CURVE, "Forward6M")
        .with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
    viewDefinition.addViewCalculationConfiguration(secondConfig);
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    final ViewCalculationConfiguration thirdConfig = new ViewCalculationConfiguration(viewDefinition, "STIR futures MtM");
    thirdConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CALCULATION_METHOD, "MarkToMarket").get());
    viewDefinition.addViewCalculationConfiguration(firstConfig);
    viewDefinition.addViewCalculationConfiguration(secondConfig);
    viewDefinition.addViewCalculationConfiguration(thirdConfig);
    return viewDefinition;
  }

  private ViewDefinition getFXForwardViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration calculationConfig1 = new ViewCalculationConfiguration(viewDefinition, "FX Implied Curves");
    final ViewCalculationConfiguration calculationConfig2 = new ViewCalculationConfiguration(viewDefinition, "FX Forward Points");
    calculationConfig1.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING).with(CURRENCY, Currency.USD.getCode()).get());
    calculationConfig2.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS).with(CURRENCY, Currency.USD.getCode()).get());
    calculationConfig1.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, ValueProperties.with(CALCULATION_METHOD, CalculationPropertyNamesAndValues.DISCOUNTING).get());
    calculationConfig2.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, ValueProperties.with(CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS).get());
    viewDefinition.addViewCalculationConfiguration(calculationConfig1);
    viewDefinition.addViewCalculationConfiguration(calculationConfig2);
    return viewDefinition;
  }

  private ViewDefinition getEquityOptionViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties constraints = ValueProperties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING).get();
    defaultCalcConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, VALUE_DELTA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, VALUE_DELTA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, DELTA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, DELTA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, GAMMA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, GAMMA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, THETA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, THETA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, RHO, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, RHO, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, VEGA, constraints);
    defaultCalcConfig.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, VEGA, constraints);
    viewDefinition.addViewCalculationConfiguration(defaultCalcConfig);
    return viewDefinition;
  }

  private ViewDefinition getFutureViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalConfig, IndexFutureSecurity.SECURITY_TYPE, new String[] {PRESENT_VALUE, PV01, VALUE_DELTA, VALUE_RHO, FORWARD });
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getBondViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    ViewCalculationConfiguration config = new ViewCalculationConfiguration(viewDefinition, "Curves");
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get());
    viewDefinition.addViewCalculationConfiguration(config);
    config = new ViewCalculationConfiguration(viewDefinition, "Yields");
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get());
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get());
    viewDefinition.addViewCalculationConfiguration(config);
    return viewDefinition;
  }

<<<<<<< HEAD
<<<<<<< HEAD
=======
  /**
   * Creates a view definition for bond total return swaps that requests the present value,
   * funding and asset leg present values, PV01 for all relevant curves, gamma PV01,
   * the details of the funding leg, the details of the bond, the bond equivalent value 
   * and the notional.
=======
  /**
   * Creates a view definition for bond total return swaps that requests the bond curve
   * for each issuer and the present value.
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getBondTotalReturnSwapViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
<<<<<<< HEAD
    final ValueProperties properties = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, "Discounting")
        .with(CURVE_EXPOSURES, "Bond Exposures")
        .get();
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, FUNDING_LEG_PV, properties);
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, FUNDING_LEG_DETAILS, properties);
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, ASSET_LEG_PV, properties);
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, GAMMA_PV01, properties);
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, BOND_DETAILS, properties);
<<<<<<< HEAD
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, NOTIONAL, ValueProperties.builder().get());
=======
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, BOND_EQUIVALENT_VALUE, properties);
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, NOTIONAL, properties);
>>>>>>> a8c2f08... Revert "Revert "[PLAT-5345] Adding bond TRS analytics to examples-simulated""
    final ValueProperties thetaProperties = properties.copy()
        .with(PROPERTY_DAYS_TO_MOVE_FORWARD, "1")
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
        .get();
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, VALUE_THETA, thetaProperties);
    final String[] curveNames = new String[] {"USD Discounting", "USD 3M Forward Ibor", "UG Government Curve" };
    for (final String curveName : curveNames) {
      final ValueProperties curveProperties = properties.copy()
          .with(CURVE, curveName)
          .get();
      defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, PV01, curveProperties);
    }
=======
    for (final Pair<String, String> pair : BOND_TRS_ISSUER_CURVES) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.NULL;
      final ValueProperties properties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, pair.getFirst())
          .with(CURVE, pair.getSecond())
          .get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, target, properties));
    }
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE_EXPOSURES, "Bond Exposures")
        .get();
    defaultCalculationConfig.addPortfolioRequirement(BondTotalReturnSwapSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
<<<<<<< HEAD
   * Creates a view definition for equity total return swaps that requests the present value,
   * funding and asset leg present values, PV01 for all relevant curves, value delta, gamma PV01
   * the details of the funding leg and the notional.
=======
   * Creates a view definition for equity total return swaps that requests the present value.
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getEquityTotalReturnSwapViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(500L);
    viewDefinition.setMaxFullCalculationPeriod(500L);
    viewDefinition.setMinDeltaCalculationPeriod(500L);
    viewDefinition.setMinFullCalculationPeriod(500L);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties properties = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, "Discounting")
        .with(CURVE_EXPOSURES, "Exposures")
        .get();
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
<<<<<<< HEAD
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, FUNDING_LEG_PV, properties);
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, FUNDING_LEG_DETAILS, properties);
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, ASSET_LEG_PV, properties);
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, GAMMA_PV01, properties);
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, VALUE_DELTA, properties);
<<<<<<< HEAD
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, NOTIONAL, ValueProperties.builder().get());
=======
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, NOTIONAL, properties);
>>>>>>> a8c2f08... Revert "Revert "[PLAT-5345] Adding bond TRS analytics to examples-simulated""
    final ValueProperties thetaProperties = properties.copy()
        .with(PROPERTY_DAYS_TO_MOVE_FORWARD, "1")
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
        .get();
    defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, VALUE_THETA, thetaProperties);
    final String[] curveNames = new String[] {"USD Discounting", "USD 3M Forward Ibor" };
    for (final String curveName : curveNames) {
      final ValueProperties curveProperties = properties.copy()
          .with(CURVE, curveName)
          .get();
      defaultCalculationConfig.addPortfolioRequirement(EquityTotalReturnSwapSecurity.SECURITY_TYPE, PV01, curveProperties);
    }
=======
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

<<<<<<< HEAD
>>>>>>> 7decf75... [PLAT-6345] Adding more outputs for equity and bond TRS
=======
>>>>>>> 6aec53f... Revert "Revert "[PLAT-6098], [PLAT-6099], [PLAT-6344], [PLAT-6345] Adding support for equity and bond TRS""
  private void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType, final String[] valueRequirementNames) {
    for (final String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirementName(securityType, valueRequirementName);
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

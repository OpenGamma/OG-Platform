/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesPrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurvePrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveTradeDefaults;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveDefaults;
import com.opengamma.financial.analytics.model.curve.interestrate.YieldCurveDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityDividendYieldPricingDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionDefaults;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityForwardCalculationDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationDefaults;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaults;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionSABRDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionHestonDefaults;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.model.volatility.cube.CubeFunctions;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults;
import com.opengamma.financial.analytics.model.volatility.local.LocalFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.FXPDECurveDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSABRDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSplineDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.CommodityBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.EquityBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfacePrimitiveDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfaceSecurityDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.FXBlackVolatilitySurfaceTradeDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.PureBlackVolatilitySurfaceDefaults;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.spring.defaults.EquityInstrumentDefaultValues;
import com.opengamma.web.spring.defaults.GeneralBlackVolatilityInterpolationDefaults;
import com.opengamma.web.spring.defaults.GeneralLocalVolatilitySurfaceDefaults;
import com.opengamma.web.spring.defaults.TargetSpecificBlackVolatilitySurfaceDefaults;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class DemoStandardFunctionConfiguration extends AbstractRepositoryConfigurationBean {

  private String _mark2MarketField = "PX_LAST";
  private String _costOfCarryField = "COST_OF_CARRY";

  public void setMark2MarketField(final String mark2MarketField) {
    _mark2MarketField = mark2MarketField;
  }

  public String getMark2MarketField() {
    return _mark2MarketField;
  }

  public void setCostOfCarryField(final String costOfCarryField) {
    _costOfCarryField = costOfCarryField;
  }

  public String getCostOfCarryField() {
    return _costOfCarryField;
  }

  public static RepositoryConfigurationSource instance() {
    return new DemoStandardFunctionConfiguration().getObjectCreating();
  }

  protected void addBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSABRDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSABRInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getMixedLogNormalInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSplineDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSplineInterpolationDefaults()));
  }

  protected void addCommodityBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
  }

  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  protected void addEquityDividendYieldFuturesDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityFutureDefaults = EquityInstrumentDefaultValues.builder()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveNames()
        .createDefaults();
    final List<String> equityFutureDefaultsWithPriority = new ArrayList<String>();
    equityFutureDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityFutureDefaultsWithPriority.addAll(equityFutureDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityDividendYieldPricingDefaults.class.getName(), equityFutureDefaultsWithPriority));
  }

  protected void addEquityForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityForwardDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .createDefaults();
    final List<String> equityForwardDefaultsWithPriority = new ArrayList<String>();
    equityForwardDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityForwardDefaultsWithPriority.addAll(equityForwardDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCalculationDefaults.class.getName(), equityForwardDefaultsWithPriority));
    final List<String> equityForwardCurveDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useDiscountingCurveCurrency()
        .createDefaults();
    final List<String> equityForwardCurveDefaultsWithPriority = new ArrayList<String>();
    equityForwardCurveDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityForwardCurveDefaultsWithPriority.addAll(equityForwardCurveDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityForwardCurveDefaults.class.getName(), equityForwardCurveDefaultsWithPriority));
  }

  protected void addEquityIndexOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useForwardCurveNames()
        .useForwardCurveCalculationMethodNames()
        .useDiscountingCurveCurrency()
        .useForwardCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfacePrimitiveDefaults.class.getName(), defaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfaceSecurityDefaults.class.getName(), defaults));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityBlackVolatilitySurfaceTradeDefaults.class.getName(), defaults));
  }

  protected void addEquityOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityIndexOptionDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useForwardCurveCalculationConfigNames()
        .useForwardCurveNames()
        .useVolatilitySurfaceNames()
        .useInterpolationMethodNames()
        .createDefaults();
    final List<String> equityIndexOptionDefaultsWithPriority = new ArrayList<String>();
    equityIndexOptionDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityIndexOptionDefaultsWithPriority.addAll(equityIndexOptionDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityOptionDefaults.class.getName(), equityIndexOptionDefaultsWithPriority));
  }

  protected void addEquityPureVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfaceDefaults.class.getName(), defaults));
  }

  protected void addEquityVarianceSwapDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> equityVarianceSwapStaticReplicationDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    final List<String> equityVarianceSwapStaticReplicationDefaultsWithPriority = new ArrayList<String>();
    equityVarianceSwapStaticReplicationDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapStaticReplicationDefaultsWithPriority.addAll(equityVarianceSwapStaticReplicationDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapStaticReplicationDefaults.class.getName(), equityVarianceSwapStaticReplicationDefaultsWithPriority));
    final List<String> equityVarianceSwapDefaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useForwardCurveNames()
        .useForwardCurveCalculationConfigNames()
        .useForwardCurveCalculationMethodNames()
        .useDiscountingCurveCurrency()
        .useVolatilitySurfaceNames()
        .createDefaults();
    final List<String> equityVarianceSwapDefaultsWithPriority = new ArrayList<String>();
    equityVarianceSwapDefaultsWithPriority.add(PriorityClass.NORMAL.name());
    equityVarianceSwapDefaultsWithPriority.addAll(equityVarianceSwapDefaults);
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityVarianceSwapDefaults.class.getName(), equityVarianceSwapDefaultsWithPriority));
  }

  protected void addForwardCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesPrimitiveDefaults.class,
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting",
            "CHF", "DefaultTwoCurveCHFConfig", "Discounting"));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesSecurityDefaults.class,
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting",
            "CHF", "DefaultTwoCurveCHFConfig", "Discounting"));
  }

  protected void addFXImpliedYieldCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXImpliedYieldCurveDefaults.class, "0.0001", "0.0001", "1000",
        DecompositionFactory.SV_COLT_NAME, "false", "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator", "ARS", "AUD",
        "BRL", "CAD", "CHF", "CLP", "CNY", "CZK", "EGP", "EUR", "GBP", "HKD", "HUF", "IDR", "ILS", "INR", "JPY", "KRW", "MXN",
        "MYR", "NOK", "NZD", "PHP", "PLN", "SEK", "SGD", "TRY", "TWD", "ZAR"));
  }

  protected void addFXOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXForwardCurvePrimitiveDefaults.class.getName(),
        Arrays.asList("EURUSD", "DiscountingImplied", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXForwardCurveSecurityDefaults.class.getName(),
        Arrays.asList("EURUSD", "DiscountingImplied", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXForwardCurveTradeDefaults.class.getName(),
        Arrays.asList("EURUSD", "DiscountingImplied", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD)));
  }

  protected void addInterestRateFutureDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig",
        "EUR", "DefaultTwoCurveEURConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "RUB", "DefaultCashCurveRUBConfig"));
  }

  protected void addInterestRateFutureOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionBlackDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "DEFAULT_PRICE"));
    functionConfigs.add(functionConfiguration(IRFutureOptionSABRDefaults.class, PriorityClass.ABOVE_NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "DEFAULT_PRICE", SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        "EUR", "DefaultTwoCurveEURConfig", "DEFAULT_PRICE", SmileFittingProperties.NON_LINEAR_LEAST_SQUARES));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionHestonDefaults.class,
        "USD", "DefaultTwoCurveUSDConfig", "DEFAULT_PRICE",
        "EUR", "DefaultTwoCurveEURConfig", "DEFAULT_PRICE"));
  }

  protected void addLocalVolatilityPDEDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXPDECurveDefaults.class,
        "USD", "Discounting", "DefaultTwoCurveUSDConfig",
        "EUR", "Discounting", "DefaultTwoCurveEURConfig"));
  }

  protected void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
  }

  protected void addSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaults.class, PriorityClass.BELOW_NORMAL.name(),
        SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        "USD", "DefaultTwoCurveUSDConfig", "BLOOMBERG",
        "EUR", "DefaultTwoCurveEURConfig", "BLOOMBERG",
        "AUD", "DefaultTwoCurveAUDConfig", "BLOOMBERG",
        "GBP", "DefaultTwoCurveGBPConfig", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaults.class, PriorityClass.BELOW_NORMAL.name(),
        SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
        "USD", "DefaultTwoCurveUSDConfig", "BLOOMBERG",
        "EUR", "DefaultTwoCurveEURConfig", "BLOOMBERG",
        "AUD", "DefaultTwoCurveAUDConfig", "BLOOMBERG",
        "GBP", "DefaultTwoCurveGBPConfig", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "USD", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "EUR", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "GBP", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "AUD", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaults.class, PriorityClass.BELOW_NORMAL.name(),
        SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, "0.07", "10.0",
        "USD", "DefaultTwoCurveUSDConfig", "BLOOMBERG",
        "EUR", "DefaultTwoCurveEURConfig", "BLOOMBERG",
        "AUD", "DefaultTwoCurveAUDConfig", "BLOOMBERG",
        "GBP", "DefaultTwoCurveGBPConfig", "BLOOMBERG"));
    functionConfigs
        .add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, PriorityClass.BELOW_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
            "0.07", "10.0", LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
            "USD", "DefaultTwoCurveUSDConfig", "BLOOMBERG",
            "EUR", "DefaultTwoCurveEURConfig", "BLOOMBERG",
            "AUD", "DefaultTwoCurveAUDConfig", "BLOOMBERG",
            "GBP", "DefaultTwoCurveGBPConfig", "BLOOMBERG"));
  }

  protected void addYieldCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(YieldCurveDefaults.class, "0.0001", "0.0001", "1000", DecompositionFactory.SV_COLT_NAME, "false", "USD", "CHF", "CAD", "GBP", "AUD",
        "EUR", "BRL", "HUF", "KRW", "MXN", "NZD", "JPY", "HKD", "CNY", "RUB", "ARS"));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addBlackVolatilitySurfaceDefaults(functions);
    addCommodityBlackVolatilitySurfaceDefaults(functions);
    addCurrencyConversionFunctions(functions);
    addEquityDividendYieldFuturesDefaults(functions);
    addEquityForwardDefaults(functions);
    addEquityIndexOptionBlackVolatilitySurfaceDefaults(functions);
    addEquityOptionDefaults(functions);
    addEquityPureVolatilitySurfaceDefaults(functions);
    addEquityVarianceSwapDefaults(functions);
    addForwardCurveDefaults(functions);
    addFXImpliedYieldCurveDefaults(functions);
    addFXOptionBlackVolatilitySurfaceDefaults(functions);
    addInterestRateFutureDefaults(functions);
    addInterestRateFutureOptionDefaults(functions);
    addLocalVolatilityPDEDefaults(functions);
    addLocalVolatilitySurfaceDefaults(functions);
    addSABRDefaults(functions);
    addYieldCurveDefaults(functions);
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "FUNDING"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  protected RepositoryConfigurationSource bondFunctions() {
    return BondFunctions.defaults(ImmutableMap.of(
        "USD", new BondFunctions.Defaults.CurrencyInfo("Discounting", "DefaultTwoCurveUSDConfig", "Discounting", "DefaultTwoCurveUSDConfig"),
        "EUR", new BondFunctions.Defaults.CurrencyInfo("Discounting", "DefaultTwoCurveEURConfig", "Discounting", "DefaultTwoCurveEURConfig"),
        "GBP", new BondFunctions.Defaults.CurrencyInfo("Discounting", "DefaultTwoCurveGBPConfig", "Discounting", "DefaultTwoCurveGBPConfig")));
  }

  protected RepositoryConfigurationSource bondFutureOptionFunctions() {
    return BondFutureOptionFunctions.defaults(ImmutableMap.of("USD", new BondFutureOptionFunctions.Defaults.CurrencyInfo("DefaultTwoCurveUSDConfig", "BBG")));
  }

  protected RepositoryConfigurationSource cdsFunctions() {
    return CreditFunctions.defaults(ImmutableMap.of("USD", new CreditFunctions.Defaults.CurrencyInfo("ISDA", "ISDA", "ISDA")));
  }

  protected RepositoryConfigurationSource cubeFunctions() {
    return CubeFunctions.defaults();
  }

  protected RepositoryConfigurationSource equityOptionFunctions() {
    return OptionFunctions.defaults();
  }

  protected RepositoryConfigurationSource externalSensitivitiesFunctions() {
    final Map<String, SensitivitiesFunctions.Defaults.CurrencyInfo> defaults = new HashMap<String, SensitivitiesFunctions.Defaults.CurrencyInfo>();
    defaults.put("EUR", new SensitivitiesFunctions.Defaults.CurrencyInfo("DefaultTwoCurveEURConfig"));
    defaults.put("USD", new SensitivitiesFunctions.Defaults.CurrencyInfo("DefaultTwoCurveUSDConfig"));
    defaults.put("CHF", new SensitivitiesFunctions.Defaults.CurrencyInfo("DefaultTwoCurveCHFConfig"));
    defaults.put("JPY", new SensitivitiesFunctions.Defaults.CurrencyInfo("DefaultTwoCurveJPYConfig"));
    defaults.put("GBP", new SensitivitiesFunctions.Defaults.CurrencyInfo("DefaultTwoCurveGBPConfig"));
    return SensitivitiesFunctions.defaults(defaults);
  }

  protected RepositoryConfigurationSource fixedIncomeFunctions() {
    final Map<String, FixedIncomeFunctions.Defaults.CurrencyInfo> defaults = new HashMap<String, FixedIncomeFunctions.Defaults.CurrencyInfo>();
    defaults.put("EUR", new FixedIncomeFunctions.Defaults.CurrencyInfo("DefaultTwoCurveEURConfig"));
    defaults.put("USD", new FixedIncomeFunctions.Defaults.CurrencyInfo("DefaultTwoCurveUSDConfig"));
    defaults.put("CHF", new FixedIncomeFunctions.Defaults.CurrencyInfo("DefaultTwoCurveCHFConfig"));
    defaults.put("JPY", new FixedIncomeFunctions.Defaults.CurrencyInfo("DefaultTwoCurveJPYConfig"));
    defaults.put("GBP", new FixedIncomeFunctions.Defaults.CurrencyInfo("DefaultTwoCurveGBPConfig"));
    return FixedIncomeFunctions.defaults(defaults);
  }

  protected RepositoryConfigurationSource forexFunctions() {
    final Map<String, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo> curveDefaults =
        new HashMap<String, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>();
    curveDefaults.put("EUR", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveEURConfig", "Discounting"));
    curveDefaults.put("USD", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveUSDConfig", "Discounting"));
    curveDefaults.put("CHF", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveCHFConfig", "Discounting"));
    curveDefaults.put("RUB", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultCashCurveRUBConfig", "Cash"));
    curveDefaults.put("CAD", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveCADConfig", "Discounting"));
    curveDefaults.put("AUD", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveAUDConfig", "Discounting"));
    curveDefaults.put("CHF", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveCHFConfig", "Discounting"));
    curveDefaults.put("MXN", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultCashCurveMXNConfig", "Cash"));
    curveDefaults.put("JPY", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveJPYConfig", "Discounting"));
    curveDefaults.put("GBP", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveGBPConfig", "Discounting"));
    curveDefaults.put("NZD", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveNZDConfig", "Discounting"));
    curveDefaults.put("HUF", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultCashCurveHUFConfig", "Cash"));
    curveDefaults.put("KRW", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultCashCurveKRWConfig", "Cash"));
    curveDefaults.put("BRL", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultCashCurveBRLConfig", "Cash"));
    curveDefaults.put("HKD", new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultCashCurveHKDConfig", "Cash"));
    final Map<Pair<String, String>, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo> surfaceDefaults =
        new HashMap<Pair<String, String>, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>();
    surfaceDefaults.put(Pair.of("USD", "EUR"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "EUR"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "CAD"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "AUD"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "CHF"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "MXN"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "JPY"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "GBP"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "NZD"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "HUF"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "KRW"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "BRL"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("EUR", "CHF"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("USD", "HKD"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    surfaceDefaults.put(Pair.of("EUR", "JPY"), new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo("TULLETT"));
    return ForexFunctions.defaults(curveDefaults, surfaceDefaults);
  }

  protected RepositoryConfigurationSource forexOptionFunctions() {
    return com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.defaults();
  }

  protected RepositoryConfigurationSource forwardCurveFunctions() {
    return ForwardFunctions.defaults();
  }

  protected RepositoryConfigurationSource futureOptionFunctions() {
    return FutureOptionFunctions.defaults(ImmutableMap.of("USD", new FutureOptionFunctions.Defaults.CurrencyInfo("Discounting", "DefaultTwoCurveUSDConfig", "BBG_S ", "Spline")));
  }

  protected RepositoryConfigurationSource localVolatilityFunctions() {
    return LocalFunctions.defaults();
  }

  protected RepositoryConfigurationSource pnlFunctions() {
    return new CombiningRepositoryConfigurationSource(PNLFunctions.calculators(getMark2MarketField(), getCostOfCarryField()),
        PNLFunctions.defaults("FUNDING", "FUNDING", "FUNDING"));
  }

  protected RepositoryConfigurationSource portfolioTheoryFunctions() {
    return new CombiningRepositoryConfigurationSource(PortfolioTheoryFunctions.calculators(), PortfolioTheoryFunctions.defaults());
  }

  protected RepositoryConfigurationSource swaptionFunctions() {
    return com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.defaults(ImmutableMap.of("EUR",
        new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo("DefaultTwoCurveEURConfig", "DEFAULT")));
  }

  protected RepositoryConfigurationSource varFunctions() {
    return VaRFunctions.defaults();
  }

  protected RepositoryConfigurationSource volatilitySurfaceFunctions() {
    return new CombiningRepositoryConfigurationSource(com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.defaults(),
        com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.defaults());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), bondFunctions(), cdsFunctions(), cubeFunctions(), equityOptionFunctions(), externalSensitivitiesFunctions(),
        fixedIncomeFunctions(), forexFunctions(), forexOptionFunctions(), forwardCurveFunctions(), futureOptionFunctions(), localVolatilityFunctions(), pnlFunctions(), portfolioTheoryFunctions(),
        swaptionFunctions(), varFunctions(), volatilitySurfaceFunctions());
  }

}

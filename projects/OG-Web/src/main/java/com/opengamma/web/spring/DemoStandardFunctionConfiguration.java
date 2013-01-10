/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
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
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.cube.CubeFunctions;
import com.opengamma.financial.analytics.model.volatility.local.LocalFunctions;
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

  protected void addFXOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(FXBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllFXDefaults()));
  }

  protected void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
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
    addFXOptionBlackVolatilitySurfaceDefaults(functions);
    addLocalVolatilitySurfaceDefaults(functions);
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
    final Map<String, ForwardFunctions.Defaults.CurrencyInfo> ccyDefaults = new HashMap<String, ForwardFunctions.Defaults.CurrencyInfo>();
    ccyDefaults.put("USD", new ForwardFunctions.Defaults.CurrencyInfo("DefaultTwoCurveUSDConfig", "Discounting"));
    ccyDefaults.put("EUR", new ForwardFunctions.Defaults.CurrencyInfo("DefaultTwoCurveEURConfig", "Discounting"));
    ccyDefaults.put("CHF", new ForwardFunctions.Defaults.CurrencyInfo("DefaultTwoCurveCHFConfig", "Discounting"));
    final Map<Pair<String, String>, ForwardFunctions.Defaults.CurrencyPairInfo> ccypDefaults = new HashMap<Pair<String, String>, ForwardFunctions.Defaults.CurrencyPairInfo>();
    ccypDefaults.put(Pair.of("EUR", "USD"), new ForwardFunctions.Defaults.CurrencyPairInfo("DiscountingImplied", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD));
    return ForwardFunctions.defaults(ccyDefaults, ccypDefaults);
  }

  protected RepositoryConfigurationSource futureFunctions() {
    final Map<String, FutureFunctions.Defaults.CurrencyInfo> defaults = new HashMap<String, FutureFunctions.Defaults.CurrencyInfo>();
    defaults.put("USD", new FutureFunctions.Defaults.CurrencyInfo("DefaultTwoCurveUSDConfig"));
    defaults.put("EUR", new FutureFunctions.Defaults.CurrencyInfo("DefaultTwoCurveEURConfig"));
    defaults.put("CHF", new FutureFunctions.Defaults.CurrencyInfo("DefaultTwoCurveCHFConfig"));
    defaults.put("RUB", new FutureFunctions.Defaults.CurrencyInfo("DefaultsCashCurveRUBConfig"));
    return FutureFunctions.defaults(defaults);
  }

  protected RepositoryConfigurationSource futureOptionFunctions() {
    return FutureOptionFunctions.defaults(ImmutableMap.of("USD", new FutureOptionFunctions.Defaults.CurrencyInfo("Discounting", "DefaultTwoCurveUSDConfig", "BBG_S ", "Spline")));
  }

  protected RepositoryConfigurationSource interestRateFunctions() {
    return InterestRateFunctions.defaults(ImmutableSet.of("ARS", "AUD",
        "BRL", "CAD", "CHF", "CLP", "CNY", "CZK", "EGP", "EUR", "GBP", "HKD", "HUF", "IDR", "ILS", "INR", "JPY", "KRW", "MXN",
        "MYR", "NOK", "NZD", "PHP", "PLN", "SEK", "SGD", "TRY", "TWD", "ZAR"));
  }

  protected RepositoryConfigurationSource irFutureOptionFunctions() {
    final Map<String, IRFutureOptionFunctions.Defaults.CurrencyInfo> defaults = new HashMap<String, IRFutureOptionFunctions.Defaults.CurrencyInfo>();
    defaults.put("USD", new IRFutureOptionFunctions.Defaults.CurrencyInfo("DefaultTwoCurveUSDConfig", "DEFAULT_PRICE"));
    defaults.put("EUR", new IRFutureOptionFunctions.Defaults.CurrencyInfo("DefaultTwoCurveEURConfig", "DEFAULT_PRICE"));
    return IRFutureOptionFunctions.defaults(defaults);
  }

  protected RepositoryConfigurationSource localVolatilityFunctions() {
    final Map<String, com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo> defaults =
        new HashMap<String, com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>();
    defaults.put("USD", new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveUSDConfig", "Discounting"));
    defaults.put("EUR", new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveEURConfig", "Discounting"));
    return LocalFunctions.defaults(defaults);
  }

  protected RepositoryConfigurationSource pnlFunctions() {
    return new CombiningRepositoryConfigurationSource(PNLFunctions.calculators(getMark2MarketField(), getCostOfCarryField()),
        PNLFunctions.defaults("FUNDING", "FUNDING", "FUNDING"));
  }

  protected RepositoryConfigurationSource portfolioTheoryFunctions() {
    return new CombiningRepositoryConfigurationSource(PortfolioTheoryFunctions.calculators(), PortfolioTheoryFunctions.defaults());
  }

  protected RepositoryConfigurationSource sabrCubeFunctions() {
    final Map<String, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo> defaults =
        new HashMap<String, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>();
    defaults.put("USD", new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveUSDConfig", "BLOOMBERG"));
    defaults.put("EUR", new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveEURConfig", "BLOOMBERG"));
    defaults.put("AUD", new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveAUDConfig", "BLOOMBERG"));
    defaults.put("GBP", new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo("DefaultTwoCurveGBPConfig", "BLOOMBERG"));
    return SABRCubeFunctions.defaults(defaults);
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
        fixedIncomeFunctions(), forexFunctions(), forexOptionFunctions(), forwardCurveFunctions(), futureFunctions(), futureOptionFunctions(), interestRateFunctions(), localVolatilityFunctions(),
        pnlFunctions(), portfolioTheoryFunctions(), swaptionFunctions(), varFunctions(), volatilitySurfaceFunctions());
  }

}

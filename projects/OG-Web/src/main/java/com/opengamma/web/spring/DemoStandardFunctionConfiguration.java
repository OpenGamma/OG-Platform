/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.DOUBLE_QUADRATIC;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondDefaultCurveNamesFunction;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionDefaults;
import com.opengamma.financial.analytics.model.credit.ISDALegacyCDSHazardCurveDefaults;
import com.opengamma.financial.analytics.model.credit.ISDALegacyVanillaCDSDefaults;
import com.opengamma.financial.analytics.model.credit.ISDAYieldCurveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromMarketQuotesDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesPrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurvePrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveTradeDefaults;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.InterpolatedForwardCurveDefaults;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveDefaults;
import com.opengamma.financial.analytics.model.curve.interestrate.YieldCurveDefaults;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityDividendYieldPricingDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityVanillaBarrierOptionDefaults;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityForwardCalculationDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationDefaults;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardDefaults;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackCurveDefaults;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackSurfaceDefaults;
import com.opengamma.financial.analytics.model.forex.option.black.FXOneLookBarrierOptionBlackDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaults;
import com.opengamma.financial.analytics.model.futureoption.CommodityFutureOptionBlackDefaults;
import com.opengamma.financial.analytics.model.horizon.SwaptionBlackThetaDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionSABRDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionHestonDefaults;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.var.NormalHistoricalVaRDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults;
import com.opengamma.financial.analytics.model.volatility.cube.defaults.SABRNonLinearSwaptionVolatilityCubeFittingDefaults;
import com.opengamma.financial.analytics.model.volatility.local.LocalFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.FXPDECurveDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.SABRIRFutureOptionNLSSDefaults;
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
import com.opengamma.financial.analytics.volatility.surface.InterpolatedVolatilitySurfaceDefaultPropertiesFunction;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
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

  private static void addBlackDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SwaptionBlackDefaultPropertiesFunction.class, PriorityClass.NORMAL.name(), "EUR", "DefaultTwoCurveEURConfig", "DEFAULT"));
    functionConfigs.add(functionConfiguration(SwaptionBlackThetaDefaults.class, PriorityClass.NORMAL.name(), "1", "EUR", "DefaultTwoCurveEURConfig", "DEFAULT"));
  }

  private static void addBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSABRDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSABRInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getMixedLogNormalInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSplineDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSplineInterpolationDefaults()));
  }

  private static void addBondDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondDefaultCurveNamesFunction.class, PriorityClass.ABOVE_NORMAL.name(),
        "USD", "Discounting", "DefaultTwoCurveUSDConfig", "Discounting", "DefaultTwoCurveUSDConfig",
        "EUR", "Discounting", "DefaultTwoCurveEURConfig", "Discounting", "DefaultTwoCurveEURConfig",
        "GBP", "Discounting", "DefaultTwoCurveGBPConfig", "Discounting", "DefaultTwoCurveGBPConfig"));
  }

  private static void addBondFutureOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondFutureOptionDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "BBG"));
  }

  private static void addCDSDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ISDAYieldCurveDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "0"));
    functionConfigs.add(functionConfiguration(ISDALegacyCDSHazardCurveDefaults.class, PriorityClass.NORMAL.name(), "100", "1e-15", "0.5",
        "USD", "ISDA", "ISDA", "ISDA"));
    functionConfigs.add(functionConfiguration(ISDALegacyVanillaCDSDefaults.class, PriorityClass.NORMAL.name(), "30"));
  }

  private static void addCommodityBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
  }

  private static void addCommodityFutureOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityFutureOptionBlackDefaults.class.getName(),
        Arrays.asList("USD", "Discounting", "DefaultTwoCurveUSDConfig", "BBG_S ", "Spline")));
  }

  private static void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  private static void addEquityBarrierOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(EquityVanillaBarrierOptionDefaults.class, "0.0", "0.001"));
  }

  private static void addEquityDividendYieldFuturesDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityIndexOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityPureVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    final List<String> defaults = EquityInstrumentDefaultValues.builder()
        .useEquityName()
        .useDiscountingCurveNames()
        .useDiscountingCurveCurrency()
        .useDiscountingCurveCalculationConfigNames()
        .useVolatilitySurfaceNames()
        .createDefaults();
    functionConfigs.add(new ParameterizedFunctionConfiguration(PureBlackVolatilitySurfaceDefaults.class.getName(), defaults));
  }

  private static void addEquityVarianceSwapDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addExternallyProvidedSensitivitiesDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesDefaultPropertiesFunction.class, PriorityClass.BELOW_NORMAL.name(),
        "EUR", "DefaultTwoCurveEURConfig",
        "USD", "DefaultTwoCurveUSDConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "GBP", "DefaultTwoCurveGBPConfig"));
  }

  private static void addFixedIncomeInstrumentDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultPropertiesFunction.class, PriorityClass.BELOW_NORMAL.name(), "false",
        "EUR", "DefaultTwoCurveEURConfig",
        "USD", "DefaultTwoCurveUSDConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "GBP", "DefaultTwoCurveGBPConfig"));
  }

  private static void addForexForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXForwardDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "Discounting",
        "EUR", "DefaultTwoCurveEURConfig", "Discounting",
        "CHF", "DefaultTwoCurveCHFConfig", "Discounting",
        "RUB", "DefaultCashCurveRUBConfig", "Cash",
        "CAD", "DefaultTwoCurveCADConfig", "Discounting"));
  }

  private static void addForexOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXOptionBlackCurveDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "Discounting",
        "EUR", "DefaultTwoCurveEURConfig", "Discounting",
        "CAD", "DefaultTwoCurveCADConfig", "Discounting",
        "AUD", "DefaultTwoCurveAUDConfig", "Discounting",
        "CHF", "DefaultTwoCurveCHFConfig", "Discounting",
        "MXN", "DefaultCashCurveMXNConfig", "Cash",
        "JPY", "DefaultTwoCurveJPYConfig", "Discounting",
        "GBP", "DefaultTwoCurveGBPConfig", "Discounting",
        "NZD", "DefaultTwoCurveNZDConfig", "Discounting",
        "HUF", "DefaultCashCurveHUFConfig", "Cash",
        "KRW", "DefaultCashCurveKRWConfig", "Cash",
        "BRL", "DefaultCashCurveBRLConfig", "Cash",
        "HKD", "DefaultCashCurveHKDConfig", "Cash"));
    functionConfigs.add(functionConfiguration(FXOptionBlackSurfaceDefaults.class, PriorityClass.NORMAL.name(), DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR,
        LINEAR_EXTRAPOLATOR,
        "USD", "EUR", "TULLETT",
        "USD", "CAD", "TULLETT",
        "USD", "AUD", "TULLETT",
        "USD", "CHF", "TULLETT",
        "USD", "MXN", "TULLETT",
        "USD", "JPY", "TULLETT",
        "USD", "GBP", "TULLETT",
        "USD", "NZD", "TULLETT",
        "USD", "HUF", "TULLETT",
        "USD", "KRW", "TULLETT",
        "USD", "BRL", "TULLETT",
        "EUR", "CHF", "TULLETT",
        "USD", "HKD", "TULLETT",
        "EUR", "JPY", "TULLETT"));
  }

  private static void addForwardCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardCurveFromMarketQuotesDefaults.class,
        "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator"));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesPrimitiveDefaults.class,
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting",
            "CHF", "DefaultTwoCurveCHFConfig", "Discounting"));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesSecurityDefaults.class,
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting",
            "CHF", "DefaultTwoCurveCHFConfig", "Discounting"));
    functions.add(functionConfiguration(InterpolatedForwardCurveDefaults.class, "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator"));
  }

  private static void addFXBarrierOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String overhedge = "0.0";
    final String relativeStrikeSmoothing = "0.001";
    functionConfigs.add(functionConfiguration(FXOneLookBarrierOptionBlackDefaultPropertiesFunction.class, overhedge, relativeStrikeSmoothing));
  }

  private static void addFXImpliedYieldCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXImpliedYieldCurveDefaults.class, "0.0001", "0.0001", "1000",
        DecompositionFactory.SV_COLT_NAME, "false", "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator", "ARS", "AUD",
        "BRL", "CAD", "CHF", "CLP", "CNY", "CZK", "EGP", "EUR", "GBP", "HKD", "HUF", "IDR", "ILS", "INR", "JPY", "KRW", "MXN",
        "MYR", "NOK", "NZD", "PHP", "PLN", "SEK", "SGD", "TRY", "TWD", "ZAR"));
  }

  private static void addFXOptionBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addInterestRateFutureDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig",
        "EUR", "DefaultTwoCurveEURConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "RUB", "DefaultCashCurveRUBConfig"));
  }

  private static void addInterestRateFutureOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionBlackDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "DEFAULT_PRICE"));
    functionConfigs.add(functionConfiguration(IRFutureOptionSABRDefaults.class, PriorityClass.ABOVE_NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "DEFAULT_PRICE", SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        "EUR", "DefaultTwoCurveEURConfig", "DEFAULT_PRICE", SmileFittingProperties.NON_LINEAR_LEAST_SQUARES));
    functionConfigs.add(functionConfiguration(SABRIRFutureOptionNLSSDefaults.class, PriorityClass.ABOVE_NORMAL.name(),
        LINEAR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, "false", "true", "false", "false",
        "0.05", "1.0", "0.07", "0.0", "0.001"));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionHestonDefaults.class,
        "USD", "DefaultTwoCurveUSDConfig", "DEFAULT_PRICE",
        "EUR", "DefaultTwoCurveEURConfig", "DEFAULT_PRICE"));
  }

  private static void addInterpolatedVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterpolatedVolatilitySurfaceDefaultPropertiesFunction.class, "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator",
        "Linear"));
  }

  private static void addLocalVolatilityPDEDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXPDECurveDefaults.class,
        "USD", "Discounting", "DefaultTwoCurveUSDConfig",
        "EUR", "Discounting", "DefaultTwoCurveEURConfig"));
  }

  private static void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
  }

  private static void addSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
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
    functionConfigs.add(functionConfiguration(SABRNonLinearSwaptionVolatilityCubeFittingDefaults.class, "0.05", "0.5", "0.7", "0.3", "false", "true", "false", "false", "0.001", "Linear",
        "FlatExtrapolator", "Linear", "FlatExtrapolator", "ForwardSwapQuotes", "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator"));
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

  private static void addVaRDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    final String defaultMeanCalculatorName = StatisticsCalculatorFactory.MEAN;
    final String defaultStdDevCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    final String defaultConfidenceLevelName = "0.99";
    final String defaultHorizonName = "1";
    //   functionConfigs.add(functionConfiguration(OptionPositionParametricVaRFunction.class, DEFAULT_CONFIG_NAME));
    //functionConfigs.add(functionConfiguration(OptionPortfolioParametricVaRFunction.class, DEFAULT_CONFIG_NAME, startDate, defaultReturnCalculatorName,
    //  defaultScheduleName, defaultSamplingCalculatorName, "0.99", "1", ValueRequirementNames.VALUE_DELTA));
    //functionConfigs.add(functionConfiguration(PositionValueGreekSensitivityPnLFunction.class, DEFAULT_CONFIG_NAME, startDate, defaultReturnCalculatorName,
    //  defaultScheduleName, defaultSamplingCalculatorName, ValueRequirementNames.VALUE_DELTA));
    functionConfigs.add(functionConfiguration(NormalHistoricalVaRDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingCalculatorName, defaultMeanCalculatorName, defaultStdDevCalculatorName, defaultConfidenceLevelName, defaultHorizonName,
        PriorityClass.NORMAL.name()));
  }

  private static void addYieldCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(YieldCurveDefaults.class, "0.0001", "0.0001", "1000", DecompositionFactory.SV_COLT_NAME, "false", "USD", "CHF", "CAD", "GBP", "AUD",
        "EUR", "BRL", "HUF", "KRW", "MXN", "NZD", "JPY", "HKD", "CNY", "RUB", "ARS"));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addBlackDefaults(functions);
    addBlackVolatilitySurfaceDefaults(functions);
    addBondDefaults(functions);
    addBondFutureOptionDefaults(functions);
    addCDSDefaults(functions);
    addCommodityBlackVolatilitySurfaceDefaults(functions);
    addCommodityFutureOptionDefaults(functions);
    addCurrencyConversionFunctions(functions);
    addEquityBarrierOptionDefaults(functions);
    addEquityDividendYieldFuturesDefaults(functions);
    addEquityForwardDefaults(functions);
    addEquityIndexOptionBlackVolatilitySurfaceDefaults(functions);
    addEquityOptionDefaults(functions);
    addEquityPureVolatilitySurfaceDefaults(functions);
    addEquityVarianceSwapDefaults(functions);
    addExternallyProvidedSensitivitiesDefaults(functions);
    addFixedIncomeInstrumentDefaults(functions);
    addForexForwardDefaults(functions);
    addForexOptionDefaults(functions);
    addFXBarrierOptionDefaults(functions);
    addForwardCurveDefaults(functions);
    addFXImpliedYieldCurveDefaults(functions);
    addFXOptionBlackVolatilitySurfaceDefaults(functions);
    addInterestRateFutureDefaults(functions);
    addInterestRateFutureOptionDefaults(functions);
    addInterpolatedVolatilitySurfaceDefaults(functions);
    addLocalVolatilityPDEDefaults(functions);
    addLocalVolatilitySurfaceDefaults(functions);
    addSABRDefaults(functions);
    addVaRDefaults(functions);
    addYieldCurveDefaults(functions);
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "FUNDING"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
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

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), localVolatilityFunctions(), pnlFunctions(), portfolioTheoryFunctions());
  }

}

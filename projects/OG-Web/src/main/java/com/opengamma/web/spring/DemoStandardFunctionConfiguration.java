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
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFormatter;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLSettings;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.FinancialFunctions;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.cashflow.FixedPayCashFlowFunction;
import com.opengamma.financial.analytics.cashflow.FixedReceiveCashFlowFunction;
import com.opengamma.financial.analytics.cashflow.FloatingPayCashFlowFunction;
import com.opengamma.financial.analytics.cashflow.FloatingReceiveCashFlowFunction;
import com.opengamma.financial.analytics.cashflow.NettedFixedCashFlowFunction;
import com.opengamma.financial.analytics.model.bond.BondDefaultCurveNamesFunction;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionDefaults;
import com.opengamma.financial.analytics.model.credit.ISDALegacyCDSHazardCurveDefaults;
import com.opengamma.financial.analytics.model.credit.ISDALegacyCDSHazardCurveFunction;
import com.opengamma.financial.analytics.model.credit.ISDALegacyVanillaCDSCleanPriceFunction;
import com.opengamma.financial.analytics.model.credit.ISDALegacyVanillaCDSDefaults;
import com.opengamma.financial.analytics.model.credit.ISDALegacyVanillaCDSDirtyPriceFunction;
import com.opengamma.financial.analytics.model.credit.ISDAYieldCurveDefaults;
import com.opengamma.financial.analytics.model.credit.ISDAYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurvePrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveTradeDefaults;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.EquityForwardCurveDefaults;
import com.opengamma.financial.analytics.model.equity.futures.EquityDividendYieldPricingDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionDefaults;
import com.opengamma.financial.analytics.model.equity.option.EquityVanillaBarrierOptionDefaults;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionModelFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioPositionFunction;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityForwardCalculationDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapDefaults;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationDefaults;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPV01Function;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateParallelCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPresentValueFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardDefaults;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackCurveDefaults;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackSurfaceDefaults;
import com.opengamma.financial.analytics.model.forex.option.black.FXOneLookBarrierOptionBlackDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaults;
import com.opengamma.financial.analytics.model.futureoption.CommodityFutureOptionBlackDefaults;
import com.opengamma.financial.analytics.model.horizon.SwaptionBlackThetaDefaults;
import com.opengamma.financial.analytics.model.horizon.SwaptionConstantSpreadThetaFunction;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionSABRDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackDefaults;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionHestonDefaults;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.EquityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.PositionExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFXFuturePnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFXFuturePnLFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFuturePnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFuturePnLFunction;
import com.opengamma.financial.analytics.model.pnl.TradeExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.TradeExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.pnl.ValueGreekSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.ValueGreekSensitivityPnLFunction;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSecurityMarkFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesCreditFactorsFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesNonYieldCurveFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesYieldCurveCS01Function;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesYieldCurvePV01Function;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackImpliedVolatilityFunction;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackPV01Function;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackPresentValueFunction;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackVolatilitySensitivityFunction;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.var.NormalHistoricalVaRDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.BackwardPDEDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.FXPDECurveDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.ForwardPDEDefaults;
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
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.spring.defaults.EquityInstrumentDefaultValues;
import com.opengamma.web.spring.defaults.GeneralBlackVolatilityInterpolationDefaults;
import com.opengamma.web.spring.defaults.GeneralLocalVolatilitySurfaceDefaults;
import com.opengamma.web.spring.defaults.TargetSpecificBlackVolatilitySurfaceDefaults;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class DemoStandardFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  // TODO: Change to inherit from AbstractRepositoryConfigurationBean

  private static final boolean OUTPUT_REPO_CONFIGURATION = false;

  public static <F extends FunctionDefinition> FunctionConfiguration functionConfiguration(final Class<F> clazz, final String... args) {
    if (Modifier.isAbstract(clazz.getModifiers())) {
      throw new IllegalStateException("Attempting to register an abstract class - " + clazz);
    }
    if (args.length == 0) {
      return new StaticFunctionConfiguration(clazz.getName());
    }
    return new ParameterizedFunctionConfiguration(clazz.getName(), Arrays.asList(args));
  }

  private static void addBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSABRDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSABRInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getMixedLogNormalInterpolationDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSplineDefaults.class.getName(),
        GeneralBlackVolatilityInterpolationDefaults.getSplineInterpolationDefaults()));
  }

  private static void addBondCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondDefaultCurveNamesFunction.class, PriorityClass.ABOVE_NORMAL.name(),
        "USD", "Discounting", "DefaultTwoCurveUSDConfig", "Discounting", "DefaultTwoCurveUSDConfig",
        "EUR", "Discounting", "DefaultTwoCurveEURConfig", "Discounting", "DefaultTwoCurveEURConfig",
        "GBP", "Discounting", "DefaultTwoCurveGBPConfig", "Discounting", "DefaultTwoCurveGBPConfig"));
  }

  private static void addBondFutureOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondFutureOptionDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "BBG"));
  }

  private static void addCommodityBlackVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfacePrimitiveDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceSecurityDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityBlackVolatilitySurfaceTradeDefaults.class.getName(),
        TargetSpecificBlackVolatilitySurfaceDefaults.getAllCommodityDefaults()));
  }

  private static void addCommodityFutureOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(CommodityFutureOptionBlackDefaults.class.getName(),
        Arrays.asList("USD", "Discounting", "DefaultTwoCurveUSDConfig", "BBG_S ", "Spline")));
  }

  private static void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.BLOOMBERG_LIVE_DATA));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  private static void addEquityBarrierOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(EquityVanillaBarrierOptionDefaults.class, "0.0", "0.001"));
  }

  private static void addEquityDividendYieldFuturesFunctions(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityForwardFunctions(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addEquityVarianceSwapCalculators(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addForexForwardCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXForwardDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig", "Discounting",
        "EUR", "DefaultTwoCurveEURConfig", "Discounting",
        "CHF", "DefaultTwoCurveCHFConfig", "Discounting",
        "RUB", "DefaultCashCurveRUBConfig", "Cash",
        "CAD", "DefaultTwoCurveCADConfig", "Discounting"));
  }

  private static void addForexOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addFXBarrierOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
    final String overhedge = "0.0";
    final String relativeStrikeSmoothing = "0.001";
    functionConfigs.add(functionConfiguration(FXOneLookBarrierOptionBlackDefaultPropertiesFunction.class, overhedge, relativeStrikeSmoothing));
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

  private static void addInterestRateFutureCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig",
        "EUR", "DefaultTwoCurveEURConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "RUB", "DefaultCashCurveRUBConfig"));
  }

  private static void addInterestRateFutureOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
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

  private static void addLocalVolatilityPDEFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ForwardPDEDefaults.class,
        "0.5", "100", "100", "5.0", "0.05", "1.5", "1.0", Interpolator1DFactory.DOUBLE_QUADRATIC));
    functionConfigs.add(functionConfiguration(BackwardPDEDefaults.class,
        "0.5", "100", "100", "5.0", "0.05", "3.5", Interpolator1DFactory.DOUBLE_QUADRATIC));
    functionConfigs.add(functionConfiguration(FXPDECurveDefaults.class,
        "USD", "Discounting", "DefaultTwoCurveUSDConfig",
        "EUR", "Discounting", "DefaultTwoCurveEURConfig"));
  }

  private static void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
  }

  private static void addPnLCalculators(final List<FunctionConfiguration> functionConfigs) {
    final String defaultReturnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT;
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    functionConfigs.add(functionConfiguration(TradeExchangeTradedPnLFunction.class, DEFAULT_CONFIG_NAME, "PX_LAST", "COST_OF_CARRY"));
    functionConfigs.add(functionConfiguration(TradeExchangeTradedDailyPnLFunction.class, DEFAULT_CONFIG_NAME, "PX_LAST", "COST_OF_CARRY"));
    functionConfigs.add(functionConfiguration(PositionExchangeTradedDailyPnLFunction.class, DEFAULT_CONFIG_NAME, "PX_LAST", "COST_OF_CARRY"));
    functionConfigs.add(functionConfiguration(SecurityPriceSeriesFunction.class, DEFAULT_CONFIG_NAME, MarketDataRequirementNames.MARKET_VALUE));
    functionConfigs.add(functionConfiguration(SecurityPriceSeriesDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(EquityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName,
        defaultReturnCalculatorName));
    functionConfigs.add(functionConfiguration(SimpleFuturePnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SimpleFuturePnLDefaultPropertiesFunction.class, "FUNDING", defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePnLDefaultPropertiesFunction.class, "FUNDING", "FUNDING", defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(ValueGreekSensitivityPnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(ValueGreekSensitivityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingCalculatorName, defaultReturnCalculatorName));
  }

  private static void addSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "USD", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "EUR", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "GBP", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "AUD", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaults.class, PriorityClass.BELOW_NORMAL.name(),
        SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        "USD", "DefaultTwoCurveUSDConfig", "BLOOMBERG",
        "EUR", "DefaultTwoCurveEURConfig", "BLOOMBERG",
        "AUD", "DefaultTwoCurveAUDConfig", "BLOOMBERG",
        "GBP", "DefaultTwoCurveGBPConfig", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaults.class, PriorityClass.BELOW_NORMAL.name(),
        SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, "0.07", "10.0",
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
    functionConfigs
        .add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, PriorityClass.BELOW_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
            "0.07", "10.0", LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
            "USD", "DefaultTwoCurveUSDConfig", "BLOOMBERG",
            "EUR", "DefaultTwoCurveEURConfig", "BLOOMBERG",
            "AUD", "DefaultTwoCurveAUDConfig", "BLOOMBERG",
            "GBP", "DefaultTwoCurveGBPConfig", "BLOOMBERG"));
  }

  private static void addVaRCalculators(final List<FunctionConfiguration> functionConfigs) {
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

  public static RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> functionConfigs = new ArrayList<FunctionConfiguration>();

    addBlackVolatilitySurfaceDefaults(functionConfigs);
    addBondCalculators(functionConfigs);
    addBondFutureOptionCalculators(functionConfigs);
    addCommodityBlackVolatilitySurfaceDefaults(functionConfigs);
    addCommodityFutureOptionCalculators(functionConfigs);
    addCurrencyConversionFunctions(functionConfigs);
    addEquityBarrierOptionCalculators(functionConfigs);
    addEquityDividendYieldFuturesFunctions(functionConfigs);
    addEquityForwardFunctions(functionConfigs);
    addEquityIndexOptionBlackVolatilitySurfaceDefaults(functionConfigs);
    addEquityOptionCalculators(functionConfigs);
    addEquityPureVolatilitySurfaceDefaults(functionConfigs);
    addEquityVarianceSwapCalculators(functionConfigs);
    addForexForwardCalculators(functionConfigs);
    addForexOptionCalculators(functionConfigs);
    addFXBarrierOptionCalculators(functionConfigs);
    addFXOptionBlackVolatilitySurfaceDefaults(functionConfigs);
    addInterestRateFutureCalculators(functionConfigs);
    addInterestRateFutureOptionCalculators(functionConfigs);
    addLocalVolatilityPDEFunctions(functionConfigs);
    addLocalVolatilitySurfaceDefaults(functionConfigs);
    addPnLCalculators(functionConfigs);
    addSABRDefaults(functionConfigs);
    addVaRCalculators(functionConfigs);

    addPortfolioAnalysisCalculators(functionConfigs);
    addFixedIncomeInstrumentCalculators(functionConfigs);
    addBlackCalculators(functionConfigs);
    addCDSCalculators(functionConfigs);
    addExternallyProvidedSensitivitiesFunctions(functionConfigs);
    addCashFlowFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "FUNDING"));
    functionConfigs.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));

    functionConfigs.addAll(FinancialFunctions.DEFAULT.getRepositoryConfiguration().getFunctions());

    final RepositoryConfiguration repoConfig = new RepositoryConfiguration(functionConfigs);

    if (OUTPUT_REPO_CONFIGURATION) {
      final FudgeMsg msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(repoConfig).getMessage();
      FudgeMsgFormatter.outputToSystemOut(msg);
      try {
        final FudgeXMLSettings xmlSettings = new FudgeXMLSettings();
        xmlSettings.setEnvelopeElementName(null);
        final FudgeMsgWriter msgWriter = new FudgeMsgWriter(new FudgeXMLStreamWriter(FudgeContext.GLOBAL_DEFAULT, new OutputStreamWriter(System.out), xmlSettings));
        msgWriter.setDefaultMessageProcessingDirectives(0);
        msgWriter.setDefaultMessageVersion(0);
        msgWriter.setDefaultTaxonomyId(0);
        msgWriter.writeMessage(msg);
        msgWriter.flush();
      } catch (final Exception e) {
        // Just swallow it.
      }
    }
    return repoConfig;
  }

  private static void addBlackCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SwaptionBlackPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(SwaptionBlackVolatilitySensitivityFunction.class));
    functionConfigs.add(functionConfiguration(SwaptionBlackPV01Function.class));
    functionConfigs.add(functionConfiguration(SwaptionBlackYieldCurveNodeSensitivitiesFunction.class));
    functionConfigs.add(functionConfiguration(SwaptionBlackImpliedVolatilityFunction.class));
    functionConfigs.add(functionConfiguration(SwaptionConstantSpreadThetaFunction.class));
    functionConfigs.add(functionConfiguration(SwaptionBlackDefaultPropertiesFunction.class, PriorityClass.NORMAL.name(), "EUR", "DefaultTwoCurveEURConfig", "DEFAULT"));
    functionConfigs.add(functionConfiguration(SwaptionBlackThetaDefaults.class, PriorityClass.NORMAL.name(), "1", "EUR", "DefaultTwoCurveEURConfig", "DEFAULT"));
  }

  private static void addFixedIncomeInstrumentCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentPV01Function.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultPropertiesFunction.class, PriorityClass.BELOW_NORMAL.name(), "false",
        "EUR", "DefaultTwoCurveEURConfig",
        "USD", "DefaultTwoCurveUSDConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "GBP", "DefaultTwoCurveGBPConfig"));
  }

  private static void addCDSCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ISDAYieldCurveFunction.class));
    functionConfigs.add(functionConfiguration(ISDAYieldCurveDefaults.class, PriorityClass.NORMAL.name(),
        "USD", "0"));
    functionConfigs.add(functionConfiguration(ISDALegacyCDSHazardCurveFunction.class));
    functionConfigs.add(functionConfiguration(ISDALegacyCDSHazardCurveDefaults.class, PriorityClass.NORMAL.name(), "100", "1e-15", "0.5",
        "USD", "ISDA", "ISDA", "ISDA"));
    functionConfigs.add(functionConfiguration(ISDALegacyVanillaCDSCleanPriceFunction.class));
    functionConfigs.add(functionConfiguration(ISDALegacyVanillaCDSDirtyPriceFunction.class));
    functionConfigs.add(functionConfiguration(ISDALegacyVanillaCDSDefaults.class, PriorityClass.NORMAL.name(), "30"));
  }

  private static void addPortfolioAnalysisCalculators(final List<FunctionConfiguration> functionConfigs) {
    final String defaultReturnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT;
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingFunctionName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    final String defaultStdDevCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    final String defaultCovarianceCalculatorName = StatisticsCalculatorFactory.SAMPLE_COVARIANCE;
    final String defaultVarianceCalculatorName = StatisticsCalculatorFactory.SAMPLE_VARIANCE;
    final String defaultExcessReturnCalculatorName = StatisticsCalculatorFactory.MEAN;

    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPortfolioNodeFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultCovarianceCalculatorName, defaultVarianceCalculatorName));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPositionFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultCovarianceCalculatorName, defaultVarianceCalculatorName));
    functionConfigs.add(functionConfiguration(CAPMBetaModelPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(CAPMBetaModelPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPositionFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionModelFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SharpeRatioDefaultPropertiesPortfolioNodeFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName));
    functionConfigs.add(functionConfiguration(SharpeRatioDefaultPropertiesPositionFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName));
    functionConfigs.add(functionConfiguration(SharpeRatioPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SharpeRatioPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TreynorRatioDefaultPropertiesPortfolioNodeFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName, defaultCovarianceCalculatorName,
        defaultVarianceCalculatorName));
    functionConfigs.add(functionConfiguration(TreynorRatioDefaultPropertiesPositionFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName, defaultCovarianceCalculatorName,
        defaultVarianceCalculatorName));
    functionConfigs.add(functionConfiguration(TreynorRatioPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TreynorRatioPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(JensenAlphaDefaultPropertiesPortfolioNodeFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName, defaultCovarianceCalculatorName,
        defaultVarianceCalculatorName));
    functionConfigs.add(functionConfiguration(JensenAlphaDefaultPropertiesPositionFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName, defaultCovarianceCalculatorName,
        defaultVarianceCalculatorName));
    functionConfigs.add(functionConfiguration(JensenAlphaFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPositionFunction.class, defaultSamplingPeriodName, defaultScheduleName,
        defaultSamplingFunctionName, defaultReturnCalculatorName, defaultStdDevCalculatorName, defaultExcessReturnCalculatorName));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
  }

  private static void addCashFlowFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FixedPayCashFlowFunction.class));
    functionConfigs.add(functionConfiguration(FixedReceiveCashFlowFunction.class));
    functionConfigs.add(functionConfiguration(FloatingPayCashFlowFunction.class));
    functionConfigs.add(functionConfiguration(FloatingReceiveCashFlowFunction.class));
    functionConfigs.add(functionConfiguration(NettedFixedCashFlowFunction.class));
  }

  private static void addExternallyProvidedSensitivitiesFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesNonYieldCurveFunction.class));

    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesCreditFactorsFunction.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSecurityMarkFunction.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurvePV01Function.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveCS01Function.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesDefaultPropertiesFunction.class, PriorityClass.BELOW_NORMAL.name(),
        "EUR", "DefaultTwoCurveEURConfig",
        "USD", "DefaultTwoCurveUSDConfig",
        "CHF", "DefaultTwoCurveCHFConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "GBP", "DefaultTwoCurveGBPConfig"));
  }

  public static RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new SimpleRepositoryConfigurationSource(constructRepositoryConfiguration());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR;
import static com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING;
import static com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING;
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
import com.opengamma.financial.analytics.model.bond.BondDefaultCurveNamesFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentDefaultCurveNameFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardDefaultsDeprecated;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackDefaultsDeprecated;
import com.opengamma.financial.analytics.model.forex.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaultValuesFunctionDeprecated;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.EquityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFXFuturePnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFuturePnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.ValueGreekSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodeSensitivityPnLDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFXFuturePresentValueFunction;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFuturePresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.var.NormalHistoricalVaRDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingProperties;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class ExampleStandardFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  // TODO: Change to inherit from AbstractRepositoryConfigurationBean

  private static final String USD = Currency.USD.getCode();
  private static final String SECONDARY = "SECONDARY";
  private static final String COST_OF_CARRY_FIELD = "COST_OF_CARRY";
  private static final String HTS_PRICE_FIELD = "CLOSE";
  private static final boolean OUTPUT_REPO_CONFIGURATION = false;

  public static <F extends FunctionDefinition> FunctionConfiguration functionConfiguration(final Class<F> clazz, final String... args) {
    if (Modifier.isAbstract(clazz.getModifiers())) {
      throw new IllegalStateException("Attempting to register an abstract class - " + clazz);
    }
    if (args.length == 0) {
      return new StaticFunctionConfiguration(clazz.getName());
    } else {
      return new ParameterizedFunctionConfiguration(clazz.getName(), Arrays.asList(args));
    }
  }

  private static void addBondDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondDefaultCurveNamesFunction.class, PriorityClass.ABOVE_NORMAL.name(),
        "USD", "Discounting", "DefaultTwoCurveUSDConfig", "Discounting", "DefaultTwoCurveUSDConfig",
        "EUR", "Discounting", "DefaultTwoCurveEURConfig", "Discounting", "DefaultTwoCurveEURConfig",
        "GBP", "Discounting", "DefaultTwoCurveGBPConfig", "Discounting", "DefaultTwoCurveGBPConfig"));
  }

  protected static void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  private static void addDeprecatedFixedIncomeInstrumentDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultCurveNameFunctionDeprecated.class, "ParRate", SECONDARY, SECONDARY, "AUD", "CAD", "CHF", "DKK", "EUR",
        "GBP", "JPY", "NZD", USD));
  }

  private static void addDeprecatedSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "USD", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING, "USD"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING, "0.07", "10.0", "USD"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING,
        "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", USD));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING,
        "0.07", "10.0", "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", USD));
  }

  private static void addExternallyProvidedSensitivitiesDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesDefaultPropertiesFunction.class, PriorityClass.ABOVE_NORMAL.name(),
        "USD", "DefaultTwoCurveUSDConfig",
        "GBP", "DefaultTwoCurveGBPConfig",
        "EUR", "DefaultTwoCurveEURConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "CHF", "DefaultTwoCurveCHFConfig"));
  }

  private static void addFixedIncomeInstrumentDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultPropertiesFunction.class, PriorityClass.ABOVE_NORMAL.name(), "false",
        "USD", "DefaultTwoCurveUSDConfig",
        "GBP", "DefaultTwoCurveGBPConfig",
        "EUR", "DefaultTwoCurveEURConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "CHF", "DefaultTwoCurveCHFConfig"));
  }

  private static void addForexForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "EUR"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "GBP"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "GBP", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "JPY"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "JPY", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "CHF", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "GBP"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "GBP", "EUR"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "JPY"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "JPY", "EUR"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "GBP", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "JPY", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "CHF", "JPY"));
  }

  private static void addForexOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, SECONDARY, SECONDARY, PAR_RATE_STRING, SECONDARY,
        SECONDARY, PAR_RATE_STRING, SECONDARY, "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "USD", "EUR"));
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, SECONDARY, SECONDARY, PAR_RATE_STRING, SECONDARY,
        SECONDARY, PAR_RATE_STRING, SECONDARY, "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "EUR", "USD"));
  }

  private static void addInterestRateFutureDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaultValuesFunctionDeprecated.class, SECONDARY, SECONDARY, PAR_RATE_STRING, USD, "EUR"));
  }

  private static void addPnLDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String defaultCurveCalculationMethod = PRESENT_VALUE_STRING;
    final String defaultReturnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT;
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    functionConfigs.add(functionConfiguration(SecurityPriceSeriesDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(EquityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName, defaultReturnCalculatorName));
    functionConfigs.add(functionConfiguration(SimpleFuturePnLDefaultPropertiesFunction.class, SECONDARY, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePnLDefaultPropertiesFunction.class, SECONDARY, SECONDARY,
        defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(YieldCurveNodeSensitivityPnLDefaultsDeprecated.class, SECONDARY, SECONDARY, defaultCurveCalculationMethod, defaultSamplingPeriodName,
        defaultScheduleName, defaultSamplingCalculatorName, "AUD", USD, "CAD", "DKK", "EUR", "GBP", "JPY", "NZD", "CHF"));
    functionConfigs.add(functionConfiguration(ValueGreekSensitivityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName,
        defaultReturnCalculatorName));
  }

  private static void addPortfolioAnalysisDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String returnCalculator = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT;
    final String samplingPeriod = "P2Y";
    final String schedule = ScheduleCalculatorFactory.DAILY;
    final String samplingFunction = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    final String stdDevCalculator = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    final String covarianceCalculator = StatisticsCalculatorFactory.SAMPLE_COVARIANCE;
    final String varianceCalculator = StatisticsCalculatorFactory.SAMPLE_VARIANCE;
    final String excessReturnCalculator = StatisticsCalculatorFactory.MEAN;
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator));
    functionConfigs.add(functionConfiguration(SharpeRatioDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(SharpeRatioDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(TreynorRatioDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(TreynorRatioDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(JensenAlphaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(JensenAlphaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
  }

  private static void addSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, USD, SECONDARY));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        "0.07", "10.0",
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES,
        "0.07", "10.0", LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
  }

  private static void addVaRDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    final String defaultMeanCalculatorName = StatisticsCalculatorFactory.MEAN;
    final String defaultStdDevCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    final String defaultConfidenceLevelName = "0.99";
    final String defaultHorizonName = "1";
    functionConfigs.add(functionConfiguration(NormalHistoricalVaRDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName,
        defaultMeanCalculatorName, defaultStdDevCalculatorName, defaultConfidenceLevelName, defaultHorizonName, PriorityClass.ABOVE_NORMAL.name()));
  }

  public static RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> functionConfigs = new ArrayList<FunctionConfiguration>();

    addBondDefaults(functionConfigs);
    addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.addAll(FixedIncomeFunctions.deprecated().getRepositoryConfiguration().getFunctions());
    addDeprecatedFixedIncomeInstrumentDefaults(functionConfigs);
    functionConfigs.addAll(SABRCubeFunctions.deprecated().getRepositoryConfiguration().getFunctions());
    addDeprecatedSABRDefaults(functionConfigs);
    functionConfigs.addAll(SensitivitiesFunctions.calculators(DEFAULT_CONFIG_NAME).getRepositoryConfiguration().getFunctions());
    addExternallyProvidedSensitivitiesDefaults(functionConfigs);
    addFixedIncomeInstrumentDefaults(functionConfigs);
    functionConfigs.addAll(ForwardFunctions.deprecated().getRepositoryConfiguration().getFunctions());
    addForexForwardDefaults(functionConfigs);
    functionConfigs.add(functionConfiguration(ExampleForexSpotRateMarketDataFunction.class));
    functionConfigs.addAll(BlackFunctions.deprecated().getRepositoryConfiguration().getFunctions());
    addForexOptionDefaults(functionConfigs);
    functionConfigs.addAll(FutureFunctions.deprecated().getRepositoryConfiguration().getFunctions());
    addInterestRateFutureDefaults(functionConfigs);
    functionConfigs.addAll(PNLFunctions.calculators(DEFAULT_CONFIG_NAME, HTS_PRICE_FIELD, COST_OF_CARRY_FIELD, MarketDataRequirementNames.MARKET_VALUE).getRepositoryConfiguration().getFunctions());
    functionConfigs.addAll(PNLFunctions.deprecated().getRepositoryConfiguration().getFunctions());
    addPnLDefaults(functionConfigs);
    functionConfigs.addAll(PortfolioTheoryFunctions.calculators(DEFAULT_CONFIG_NAME).getRepositoryConfiguration().getFunctions());
    addPortfolioAnalysisDefaults(functionConfigs);
    addSABRDefaults(functionConfigs);
    addVaRDefaults(functionConfigs);
    functionConfigs.add(functionConfiguration(SimpleFuturePresentValueFunctionDeprecated.class, SECONDARY));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePresentValueFunction.class, SECONDARY, SECONDARY));
    functionConfigs.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, SECONDARY));

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

  public static RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new SimpleRepositoryConfigurationSource(constructRepositoryConfiguration());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}

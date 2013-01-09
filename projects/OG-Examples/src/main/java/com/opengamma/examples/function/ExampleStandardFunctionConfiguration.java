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

import java.util.List;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.bond.BondDefaultCurveNamesFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesPrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.interestrate.YieldCurveDefaults;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
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
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
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
import com.opengamma.financial.analytics.volatility.surface.Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class ExampleStandardFunctionConfiguration extends AbstractRepositoryConfigurationBean {

  // TODO: Make this an extension of DemoStandardFunctionConfiguration and overload the methods that
  // add data provider specific entries in favour of the synthetic data

  private String _mark2MarketField = "CLOSE";
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
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
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
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultCurveNameFunctionDeprecated.class, "ParRate", "SECONDARY", "SECONDARY", "AUD", "CAD", "CHF", "DKK", "EUR",
        "GBP", "JPY", "NZD", "USD"));
  }

  private static void addDeprecatedPNLDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(YieldCurveNodeSensitivityPnLDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PRESENT_VALUE_STRING, "P2Y",
        ScheduleCalculatorFactory.DAILY, TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING, "AUD", "USD", "CAD", "DKK", "EUR", "GBP", "JPY", "NZD", "CHF"));
  }

  private static void addDeprecatedSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING, "USD"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING, "0.07", "10.0", "USD"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING,
        "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", "USD"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING,
        "0.07", "10.0", "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", "USD"));
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
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY",
        "SECONDARY", PAR_RATE_STRING, "SECONDARY", "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "USD", "EUR"));
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY",
        "SECONDARY", PAR_RATE_STRING, "SECONDARY", "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "EUR", "USD"));
  }

  private void addFXForwardCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesPrimitiveDefaults.class,
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting"));
    functions.add(functionConfiguration(FXForwardCurveFromYieldCurvesSecurityDefaults.class,
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting"));
  }

  private static void addInterestRateFutureDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaultValuesFunctionDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "USD", "EUR"));
  }

  private static void addSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "USD", "SECONDARY"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, "USD", "DefaultTwoCurveUSDConfig",
        "SECONDARY"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, "0.07", "10.0", "USD",
        "DefaultTwoCurveUSDConfig", "SECONDARY"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, LINEAR, FLAT_EXTRAPOLATOR,
        FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, "USD", "DefaultTwoCurveUSDConfig", "SECONDARY"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, PriorityClass.ABOVE_NORMAL.name(), SmileFittingProperties.NON_LINEAR_LEAST_SQUARES, "0.07", "10.0", LINEAR,
        FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, "USD", "DefaultTwoCurveUSDConfig", "SECONDARY"));
  }

  private static void addSurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ExampleEquityOptionVolatilitySurfaceDataFunction.class, "SECONDARY", "EQUITY_OPTION", "SECONDARY"));
    functionConfigs.add(functionConfiguration(Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated.class, "SECONDARY", "EQUITY_OPTION", "DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator",
        "DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator"));
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

  private static void addYieldCurveDefaults(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(YieldCurveDefaults.class, "0.0001", "0.0001", "1000", DecompositionFactory.SV_COLT_NAME, "false", "USD",
        "EUR", "GBP", "JPY", "CHF", "AUD"));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addBondDefaults(functions);
    addCurrencyConversionFunctions(functions);
    addDeprecatedFixedIncomeInstrumentDefaults(functions);
    addDeprecatedSABRDefaults(functions);
    addDeprecatedPNLDefaults(functions);
    addExternallyProvidedSensitivitiesDefaults(functions);
    addFixedIncomeInstrumentDefaults(functions);
    addForexForwardDefaults(functions);
    functions.add(functionConfiguration(ExampleForexSpotRateMarketDataFunction.class));
    addForexOptionDefaults(functions);
    addFXForwardCurveDefaults(functions);
    addInterestRateFutureDefaults(functions);
    addSABRDefaults(functions);
    addSurfaceDefaults(functions);
    addVaRDefaults(functions);
    addYieldCurveDefaults(functions);
    functions.add(functionConfiguration(SimpleFuturePresentValueFunctionDeprecated.class, "SECONDARY"));
    functions.add(functionConfiguration(SimpleFXFuturePresentValueFunction.class, "SECONDARY", "SECONDARY"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  protected RepositoryConfigurationSource deprecatedFunctions() {
    return new CombiningRepositoryConfigurationSource(FixedIncomeFunctions.deprecated(), SABRCubeFunctions.deprecated(), ForwardFunctions.deprecated(), BlackFunctions.deprecated(),
        FutureFunctions.deprecated(), PNLFunctions.deprecated());
  }

  protected RepositoryConfigurationSource sensitivitiesFunctions() {
    return SensitivitiesFunctions.calculators();
  }

  protected RepositoryConfigurationSource pnlFunctions() {
    return new CombiningRepositoryConfigurationSource(PNLFunctions.calculators(getMark2MarketField(), getCostOfCarryField()),
        PNLFunctions.defaults("SECONDARY", "SECONDARY", "SECONDARY"));
  }

  protected RepositoryConfigurationSource portfolioTheoryFunctions() {
    return new CombiningRepositoryConfigurationSource(PortfolioTheoryFunctions.calculators(), PortfolioTheoryFunctions.defaults());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), deprecatedFunctions(), sensitivitiesFunctions(), pnlFunctions(), portfolioTheoryFunctions());
  }

}

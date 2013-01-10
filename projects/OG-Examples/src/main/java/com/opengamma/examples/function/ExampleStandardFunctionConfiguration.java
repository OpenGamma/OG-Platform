/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import static com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING;
import static com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING;

import java.util.List;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.fixedincome.FixedIncomeFunctions;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentDefaultCurveNameFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardDefaultsDeprecated;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackDefaultsDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaultValuesFunctionDeprecated;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodeSensitivityPnLDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCubeFunctions;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFXFuturePresentValueFunction;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFuturePresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.volatility.surface.Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * Constructs a standard function repository.
 */
public class ExampleStandardFunctionConfiguration extends StandardFunctionConfiguration {

  public static RepositoryConfigurationSource instance() {
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
  }

  public ExampleStandardFunctionConfiguration() {
    setMark2MarketField("CLOSE");
    setCostOfCarryField("COST_OF_CARRY");
  }

  @Override
  protected CurrencyInfo chfCurrencyInfo() {
    final CurrencyInfo i = super.chfCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveCHFConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo eurCurrencyInfo() {
    final CurrencyInfo i = super.eurCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveEURConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveGBPConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo jpyCurrencyInfo() {
    final CurrencyInfo i = super.jpyCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveJPYConfig");
    i.setDefaultCurve("Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo usdCurrencyInfo() {
    final CurrencyInfo i = super.usdCurrencyInfo();
    i.setDefaultCurveConfiguration("DefaultTwoCurveUSDConfig");
    i.setDefaultCurve("Discounting");
    i.setDefaultCube("SECONDARY");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdEurCurrencyPairInfo();
    i.setDefaultCurve("DiscountingImplied");
    return i;
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  protected void addFixedIncomeInstrumentDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultCurveNameFunctionDeprecated.class, "ParRate", "SECONDARY", "SECONDARY", "AUD", "CAD", "CHF", "DKK", "EUR",
        "GBP", "JPY", "NZD", "USD"));
  }

  protected void addPNLDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(YieldCurveNodeSensitivityPnLDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PRESENT_VALUE_STRING, "P2Y",
        ScheduleCalculatorFactory.DAILY, TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING, "AUD", "USD", "CAD", "DKK", "EUR", "GBP", "JPY", "NZD", "CHF"));
  }

  protected void addSABRDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING, "USD"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING, "0.07", "10.0", "USD"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING,
        "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", "USD"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaultsDeprecated.class, "SECONDARY", "SECONDARY", "SECONDARY", "NonLinearLeastSquares", PAR_RATE_STRING,
        "0.07", "10.0", "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", "USD"));
  }

  // TODO: Should this be in the Standard repository?
  protected void addExternallyProvidedSensitivitiesDefaults(final List<FunctionConfiguration> functionConfigs) {
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
  }

  protected void addForexForwardDefaults(final List<FunctionConfiguration> functionConfigs) {
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

  protected void addForexOptionDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY",
        "SECONDARY", PAR_RATE_STRING, "SECONDARY", "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "USD", "EUR"));
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY",
        "SECONDARY", PAR_RATE_STRING, "SECONDARY", "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "EUR", "USD"));
  }

  protected void addInterestRateFutureDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaultValuesFunctionDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "USD", "EUR"));
  }

  protected void addSurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ExampleEquityOptionVolatilitySurfaceDataFunction.class, "SECONDARY", "EQUITY_OPTION", "SECONDARY"));
    functionConfigs.add(functionConfiguration(Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated.class, "SECONDARY", "EQUITY_OPTION", "DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator",
        "DoubleQuadratic", "FlatExtrapolator", "FlatExtrapolator"));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
    addFixedIncomeInstrumentDefaults(functions);
    addSABRDefaults(functions);
    addPNLDefaults(functions);
    addExternallyProvidedSensitivitiesDefaults(functions);
    addForexForwardDefaults(functions);
    addForexOptionDefaults(functions);
    addInterestRateFutureDefaults(functions);
    addSurfaceDefaults(functions);
    functions.add(functionConfiguration(ExampleForexSpotRateMarketDataFunction.class));
    functions.add(functionConfiguration(SimpleFuturePresentValueFunctionDeprecated.class, "SECONDARY"));
    functions.add(functionConfiguration(SimpleFXFuturePresentValueFunction.class, "SECONDARY", "SECONDARY"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  protected RepositoryConfigurationSource deprecatedFunctions() {
    return CombiningRepositoryConfigurationSource.of(FixedIncomeFunctions.deprecated(), SABRCubeFunctions.deprecated(),
        com.opengamma.financial.analytics.model.forex.forward.ForwardFunctions.deprecated(), BlackFunctions.deprecated(),
        FutureFunctions.deprecated(), PNLFunctions.deprecated());
  }

  @Override
  protected RepositoryConfigurationSource pnlFunctions() {
    return CombiningRepositoryConfigurationSource.of(PNLFunctions.calculators(getMark2MarketField(), getCostOfCarryField()),
        PNLFunctions.defaults("SECONDARY", "SECONDARY", "SECONDARY"));
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    // TODO: only refer to the new stuff here
    return CombiningRepositoryConfigurationSource.of(super.createObject(), bondFunctions(), deprecatedFunctions(), equityOptionFunctions(), externalSensitivitiesFunctions(), fixedIncomeFunctions(),
        forexOptionFunctions(), forwardCurveFunctions(), interestRateFunctions(), localVolatilityFunctions(), pnlFunctions(), portfolioTheoryFunctions(), sabrCubeFunctions(),
        varFunctions());
  }

}

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
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentDefaultCurveNameFunctionDeprecated;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaultValuesFunctionDeprecated;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodeSensitivityPnLDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFXFuturePresentValueFunction;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFuturePresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.volatility.surface.Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * Constructs a standard function repository.
 */
public class ExampleStandardFunctionConfiguration extends StandardFunctionConfiguration {

  public static FunctionConfigurationSource instance() {
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
  }

  public ExampleStandardFunctionConfiguration() {
    setMark2MarketField("CLOSE");
    setCostOfCarryField("COST_OF_CARRY");
  }

  @Override
  protected CurrencyInfo audCurrencyInfo() {
    final CurrencyInfo i = super.audCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultThreeCurveAUDConfig");
    i.setCurveConfiguration("model/forex", "DefaultThreeCurveAUDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/forex/discounting", "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo chfCurrencyInfo() {
    final CurrencyInfo i = super.chfCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveCHFConfig");
    i.setCurveConfiguration("model/forex", "DefaultTwoCurveCHFConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveCHFConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/forex/discounting", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER2");
    return i;
  }

  @Override
  protected CurrencyInfo eurCurrencyInfo() {
    final CurrencyInfo i = super.eurCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/forex", "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveEURConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/forex/discounting", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER2");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/forex", "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveGBPConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/forex/discounting", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER1");
    return i;
  }

  @Override
  protected CurrencyInfo jpyCurrencyInfo() {
    final CurrencyInfo i = super.jpyCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveJPYConfig");
    i.setCurveConfiguration("model/forex", "DefaultTwoCurveJPYConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveJPYConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/forex/discounting", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER3");
    return i;
  }

  @Override
  protected CurrencyInfo usdCurrencyInfo() {
    final CurrencyInfo i = super.usdCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/forex", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveUSDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/forex/discounting", "Discounting");
    i.setCubeName(null, "SECONDARY");
    i.setForwardCurveName(null, "Forward3M");
    i.setSurfaceName(null, "SECONDARY");
    i.setSurfaceName("model/swaption/black", "PROVIDER1");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdEurCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo chfJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/forex", "DEFAULT");
    return i;
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
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
    addInterestRateFutureDefaults(functions);
    addSurfaceDefaults(functions);
    functions.add(functionConfiguration(SimpleFuturePresentValueFunctionDeprecated.class, "SECONDARY"));
    functions.add(functionConfiguration(SimpleFXFuturePresentValueFunction.class, "SECONDARY", "SECONDARY"));
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  @Override
  protected FunctionConfigurationSource deprecatedFunctions() {
    return null;
  }

  @Override
  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    super.setPNLFunctionDefaults(defaults);
    defaults.setCurveName("SECONDARY");
    defaults.setPayCurveName("SECONDARY");
    defaults.setReceiveCurveName("SECONDARY");
  }

}

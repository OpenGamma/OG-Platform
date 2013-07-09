/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import java.util.List;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * Constructs a standard function repository.
 */
public class ExampleStandardFunctionConfiguration extends StandardFunctionConfiguration {

  /**
   * Gets an instance of the example function configuration.
   * @return Gets the instance
   */
  public static FunctionConfigurationSource instance() {
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
  }

  /**
   * The function configuration.
   */
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
    i.setCurveConfiguration("mode/future", "DefaultTwoCurveEURConfig");
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

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
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

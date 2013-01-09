/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.List;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.financial.property.AggregationDefaultPropertyFunction;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class PNLFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new PNLFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new Deprecated().getObjectCreating();
  }

  /**
   * Function repository configuration source for the deprecated functions contained in this package.
   */
  public static class Deprecated extends AbstractRepositoryConfigurationBean {

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(YieldCurveNodePnLFunctionDeprecated.class));
    }

  }

  public static RepositoryConfigurationSource calculators(final String mark2marketField, final String costOfCarryField) {
    final Calculators factory = new Calculators();
    factory.setMark2MarketField(mark2marketField);
    factory.setCostOfCarryField(costOfCarryField);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource calculators(final String htsResolutionKey, final String mark2marketField, final String costOfCarryField, final String valueFieldName) {
    final Calculators factory = new Calculators();
    factory.setHtsResolutionKey(htsResolutionKey);
    factory.setMark2MarketField(mark2marketField);
    factory.setCostOfCarryField(costOfCarryField);
    factory.setValueFieldName(valueFieldName);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the configurable functions contained in this package.
   */
  public static class Calculators extends AbstractRepositoryConfigurationBean {

    private String _htsResolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
    private String _mark2MarketField;
    private String _costOfCarryField;
    private String _valueFieldName = MarketDataRequirementNames.MARKET_VALUE;

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

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

    public void setValueFieldName(final String valueFieldName) {
      _valueFieldName = valueFieldName;
    }

    public String getValueFieldName() {
      return _valueFieldName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getHtsResolutionKey(), "htsResolutionKey");
      ArgumentChecker.notNullInjected(getMark2MarketField(), "mark2MarketField");
      ArgumentChecker.notNullInjected(getCostOfCarryField(), "costOfCarryField");
      ArgumentChecker.notNullInjected(getValueFieldName(), "valueFieldName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(TradeExchangeTradedPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(TradeExchangeTradedDailyPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(PositionExchangeTradedDailyPnLFunction.class, getHtsResolutionKey(), getMark2MarketField(), getCostOfCarryField()));
      functions.add(functionConfiguration(SecurityPriceSeriesFunction.class, getHtsResolutionKey(), getValueFieldName()));
      functions.add(functionConfiguration(SimpleFuturePnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SimpleFXFuturePnLFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(ValueGreekSensitivityPnLFunction.class, getHtsResolutionKey()));
    }

  }

  public static RepositoryConfigurationSource defaults(final String curveName, final String payCurveName, final String receiveCurveName) {
    final Defaults factory = new Defaults();
    factory.setCurveName(curveName);
    factory.setPayCurveName(payCurveName);
    factory.setReceiveCurveName(receiveCurveName);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final String curveName, final String payCurveName, final String receiveCurveName, final String returnCalculatorName,
      final String samplingPeriodName, final String scheduleName, final String samplingCalculatorName) {
    final Defaults factory = new Defaults();
    factory.setCurveName(curveName);
    factory.setPayCurveName(payCurveName);
    factory.setReceiveCurveName(receiveCurveName);
    factory.setReturnCalculatorName(returnCalculatorName);
    factory.setSamplingPeriodName(samplingPeriodName);
    factory.setScheduleName(scheduleName);
    factory.setSamplingCalculatorName(samplingCalculatorName);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    private String _curveName;
    private String _payCurveName;
    private String _receiveCurveName;
    private String _returnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT;
    private final String _samplingPeriodName = "P2Y";
    private String _scheduleName = ScheduleCalculatorFactory.DAILY;
    private String _samplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;

    public void setCurveName(final String curveName) {
      _curveName = curveName;
    }

    public String getCurveName() {
      return _curveName;
    }

    public void setPayCurveName(final String payCurveName) {
      _payCurveName = payCurveName;
    }

    public String getPayCurveName() {
      return _payCurveName;
    }

    public void setReceiveCurveName(final String receiveCurveName) {
      _receiveCurveName = receiveCurveName;
    }

    public String getReceiveCurveName() {
      return _receiveCurveName;
    }

    public void setReturnCalculatorName(final String returnCalculatorName) {
      _returnCalculatorName = returnCalculatorName;
    }

    public String getReturnCalculatorName() {
      return _returnCalculatorName;
    }

    public void setSamplingPeriodName(final String samplingPeriodName) {
      _samplingCalculatorName = samplingPeriodName;
    }

    public String getSamplingPeriodName() {
      return _samplingPeriodName;
    }

    public void setScheduleName(final String scheduleName) {
      _scheduleName = scheduleName;
    }

    public String getScheduleName() {
      return _scheduleName;
    }

    public void setSamplingCalculatorName(final String samplingCalculatorName) {
      _samplingCalculatorName = samplingCalculatorName;
    }

    public String getSamplingCalculatorName() {
      return _samplingCalculatorName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getCurveName(), "curveName");
      ArgumentChecker.notNullInjected(getPayCurveName(), "payCurveName");
      ArgumentChecker.notNullInjected(getReceiveCurveName(), "receiveCurveName");
      ArgumentChecker.notNullInjected(getReturnCalculatorName(), "returnCalculatorName");
      ArgumentChecker.notNullInjected(getSamplingPeriodName(), "samplingPeriodName");
      ArgumentChecker.notNullInjected(getScheduleName(), "scheduleName");
      ArgumentChecker.notNullInjected(getSamplingCalculatorName(), "samplingCalculatorName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(EquityPnLDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName(),
          getReturnCalculatorName()));
      functions.add(functionConfiguration(SecurityPriceSeriesDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName()));
      functions.add(functionConfiguration(SimpleFuturePnLDefaultPropertiesFunction.class, getCurveName(), getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName()));
      functions.add(functionConfiguration(SimpleFXFuturePnLDefaultPropertiesFunction.class, getPayCurveName(), getReceiveCurveName(), getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName()));
      functions.add(functionConfiguration(ValueGreekSensitivityPnLDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName(), getReturnCalculatorName()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EquityPnLFunction.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedDailyPnLFunction.Impl.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(AggregationDefaultPropertyFunction.class, ValueRequirementNames.DAILY_PNL, MissingInputsFunction.AGGREGATION_STYLE_FULL));
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

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

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractRepositoryConfigurationBean {

    /**
     * Per currency information.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfiguration;

      public void setCurveConfiguration(final String curveConfiguration) {
        _curveConfiguration = curveConfiguration;
      }

      public String getCurveConfiguration() {
        return _curveConfiguration;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfiguration(), "curveConfiguration");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();
    private String _curveName;
    private String _payCurveName;
    private String _receiveCurveName;
    private String _returnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT;
    private final String _samplingPeriodName = "P2Y";
    private String _scheduleName = ScheduleCalculatorFactory.DAILY;
    private String _samplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;

    public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(perCurrencyInfo);
    }

    public Map<String, CurrencyInfo> getPerCurrencyInfo() {
      return _perCurrencyInfo;
    }

    public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
      _perCurrencyInfo.put(currency, info);
    }

    public CurrencyInfo getCurrencyInfo(final String currency) {
      return _perCurrencyInfo.get(currency);
    }

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
      ArgumentChecker.notNullInjected(getReturnCalculatorName(), "returnCalculatorName");
      ArgumentChecker.notNullInjected(getSamplingPeriodName(), "samplingPeriodName");
      ArgumentChecker.notNullInjected(getScheduleName(), "scheduleName");
      ArgumentChecker.notNullInjected(getSamplingCalculatorName(), "samplingCalculatorName");
      super.afterPropertiesSet();
    }

    protected void addYieldCurveNodePnLDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3 + getPerCurrencyInfo().size() * 2];
      int i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfiguration();
      }
      functions.add(functionConfiguration(YieldCurveNodePnLDefaults.class, args));
    }

    protected void addPositionPnLDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[3 + getPerCurrencyInfo().size()];
      int i = 0;
      args[i++] = getSamplingPeriodName();
      args[i++] = getScheduleName();
      args[i++] = getSamplingCalculatorName();
      for (final String currency : getPerCurrencyInfo().keySet()) {
        args[i++] = currency;
      }
      functions.add(functionConfiguration(PositionPnLDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(EquityPnLDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName(),
          getReturnCalculatorName()));
      functions.add(functionConfiguration(SecurityPriceSeriesDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName()));
      if (getCurveName() != null) {
        functions.add(functionConfiguration(SimpleFuturePnLDefaultPropertiesFunction.class, getCurveName(), getSamplingPeriodName(), getScheduleName(),
            getSamplingCalculatorName()));
      }
      if ((getPayCurveName() != null) && (getReceiveCurveName() != null)) {
        functions.add(functionConfiguration(SimpleFXFuturePnLDefaultPropertiesFunction.class, getPayCurveName(), getReceiveCurveName(), getSamplingPeriodName(), getScheduleName(),
            getSamplingCalculatorName()));
      }
      functions.add(functionConfiguration(ValueGreekSensitivityPnLDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingCalculatorName(), getReturnCalculatorName()));
      if (!getPerCurrencyInfo().isEmpty()) {
        addYieldCurveNodePnLDefaults(functions);
        addPositionPnLDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureOptionBlackYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(EquityPnLFunction.class));
    functions.add(functionConfiguration(FXForwardCurrencyExposurePnLFunction.class));
    functions.add(functionConfiguration(FXForwardYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(FXOptionBlackDeltaPnLFunction.class));
    functions.add(functionConfiguration(FXOptionBlackVegaPnLFunction.class));
    functions.add(functionConfiguration(InterestRateFutureOptionBlackYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(InterestRateFutureYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedDailyPnLFunction.Impl.class));
    functions.add(functionConfiguration(PortfolioExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionExchangeTradedPnLFunction.class));
    functions.add(functionConfiguration(PositionPnLFunction.class));
    functions.add(functionConfiguration(SwaptionBlackYieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(YieldCurveNodePnLFunction.class));
    functions.add(functionConfiguration(AggregationDefaultPropertyFunction.class, ValueRequirementNames.DAILY_PNL, MissingInputsFunction.AGGREGATION_STYLE_FULL));
  }

}

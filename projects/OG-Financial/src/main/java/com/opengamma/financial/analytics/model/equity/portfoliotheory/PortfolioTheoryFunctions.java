/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.List;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class PortfolioTheoryFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new PortfolioTheoryFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the functions contained in this package.
   */
  public static class Calculators extends AbstractFunctionConfigurationBean {

    private String _htsResolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getHtsResolutionKey(), "htsResolutionKey");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(CAPMBetaModelPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(CAPMBetaModelPortfolioNodeFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(CAPMFromRegressionModelFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SharpeRatioPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(SharpeRatioPortfolioNodeFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TreynorRatioPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TreynorRatioPortfolioNodeFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(JensenAlphaFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TotalRiskAlphaPositionFunction.class, getHtsResolutionKey()));
      functions.add(functionConfiguration(TotalRiskAlphaPortfolioNodeFunction.class, getHtsResolutionKey()));
    }

  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private String _returnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT;
    private final String _samplingPeriodName = "P2Y";
    private String _scheduleName = ScheduleCalculatorFactory.DAILY;
    private String _samplingFunctionName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    private String _stdDevCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    private String _covarianceCalculatorName = StatisticsCalculatorFactory.SAMPLE_COVARIANCE;
    private String _varianceCalculatorName = StatisticsCalculatorFactory.SAMPLE_VARIANCE;
    private String _excessReturnCalculatorName = StatisticsCalculatorFactory.MEAN;

    public void setReturnCalculatorName(final String returnCalculatorName) {
      _returnCalculatorName = returnCalculatorName;
    }

    public String getReturnCalculatorName() {
      return _returnCalculatorName;
    }

    public void setSamplingPeriodName(final String samplingPeriodName) {
      _samplingFunctionName = samplingPeriodName;
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

    public void setSamplingFunctionName(final String samplingFunctionName) {
      _samplingFunctionName = samplingFunctionName;
    }

    public String getSamplingFunctionName() {
      return _samplingFunctionName;
    }

    public void setStdDevCalculatorName(final String stdDevCalculatorName) {
      _stdDevCalculatorName = stdDevCalculatorName;
    }

    public String getStdDevCalculatorName() {
      return _stdDevCalculatorName;
    }

    public void setCovarianceCalculatorName(final String covarianceCalculatorName) {
      _covarianceCalculatorName = covarianceCalculatorName;
    }

    public String getCovarianceCalculatorName() {
      return _covarianceCalculatorName;
    }

    public void setVarianceCalculatorName(final String varianceCalculatorName) {
      _varianceCalculatorName = varianceCalculatorName;
    }

    public String getVarianceCalculatorName() {
      return _varianceCalculatorName;
    }

    public void setExcessReturnCalculatorName(final String excessReturnCalculatorName) {
      _excessReturnCalculatorName = excessReturnCalculatorName;
    }

    public String getExcessReturnCalculatorName() {
      return _excessReturnCalculatorName;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNullInjected(getReturnCalculatorName(), "returnCalculatorName");
      ArgumentChecker.notNullInjected(getSamplingPeriodName(), "samplingPeriodName");
      ArgumentChecker.notNullInjected(getScheduleName(), "scheduleName");
      ArgumentChecker.notNullInjected(getSamplingFunctionName(), "samplingFunctionName");
      ArgumentChecker.notNullInjected(getStdDevCalculatorName(), "stdDevCalculatorName");
      ArgumentChecker.notNullInjected(getCovarianceCalculatorName(), "covarianceCalculatorName");
      ArgumentChecker.notNullInjected(getVarianceCalculatorName(), "varianceCalculatorName");
      ArgumentChecker.notNullInjected(getExcessReturnCalculatorName(), "excessReturnCalculatorName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(CAPMBetaDefaultPropertiesPortfolioNodeFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getCovarianceCalculatorName(), getVarianceCalculatorName()));
      functions.add(functionConfiguration(CAPMBetaDefaultPropertiesPositionFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getCovarianceCalculatorName(), getVarianceCalculatorName()));
      functions.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName()));
      functions.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPositionFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName()));
      functions.add(functionConfiguration(SharpeRatioDefaultPropertiesPortfolioNodeFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName()));
      functions.add(functionConfiguration(SharpeRatioDefaultPropertiesPositionFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName()));
      functions.add(functionConfiguration(TreynorRatioDefaultPropertiesPortfolioNodeFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName(), getCovarianceCalculatorName(),
          getVarianceCalculatorName()));
      functions.add(functionConfiguration(TreynorRatioDefaultPropertiesPositionFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName(), getCovarianceCalculatorName(),
          getVarianceCalculatorName()));
      functions.add(functionConfiguration(JensenAlphaDefaultPropertiesPortfolioNodeFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName(), getCovarianceCalculatorName(),
          getVarianceCalculatorName()));
      functions.add(functionConfiguration(JensenAlphaDefaultPropertiesPositionFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName(), getCovarianceCalculatorName(),
          getVarianceCalculatorName()));
      functions.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName()));
      functions.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPositionFunction.class, getSamplingPeriodName(), getScheduleName(),
          getSamplingFunctionName(), getReturnCalculatorName(), getStdDevCalculatorName(), getExcessReturnCalculatorName()));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(StandardEquityModelFunction.class));
  }

}

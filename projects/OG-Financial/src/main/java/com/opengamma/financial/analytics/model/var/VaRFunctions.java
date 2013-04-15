/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.List;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class VaRFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new VaRFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private String _samplingPeriodName = "P2Y";
    private String _scheduleName = ScheduleCalculatorFactory.DAILY;
    private String _samplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    private String _meanCalculatorName = StatisticsCalculatorFactory.MEAN;
    private String _stdDevCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    private double _confidenceLevel = 0.99;
    private double _horizon = 1d;

    public String getSamplingPeriodName() {
      return _samplingPeriodName;
    }

    public void setSamplingPeriodName(final String samplingPeriodName) {
      _samplingPeriodName = samplingPeriodName;
    }

    public String getScheduleName() {
      return _scheduleName;
    }

    public void setScheduleName(final String scheduleName) {
      _scheduleName = scheduleName;
    }

    public String getSamplingCalculatorName() {
      return _samplingCalculatorName;
    }

    public void setSamplingCalculatorName(final String samplingCalculatorName) {
      _samplingCalculatorName = samplingCalculatorName;
    }

    public String getMeanCalculatorName() {
      return _meanCalculatorName;
    }

    public void setMeanCalculatorName(final String meanCalculatorName) {
      _meanCalculatorName = meanCalculatorName;
    }

    public String getStdDevCalculatorName() {
      return _stdDevCalculatorName;
    }

    public void setStdDevCalculatorName(final String stdDevCalculatorName) {
      _stdDevCalculatorName = stdDevCalculatorName;
    }

    public double getConfidenceLevel() {
      return _confidenceLevel;
    }

    public void setConfidenceLevel(final double confidenceLevel) {
      _confidenceLevel = confidenceLevel;
    }

    public double getHorizon() {
      return _horizon;
    }

    public void setHorizonName(final double horizon) {
      _horizon = horizon;
    }

    @Override
    public void afterPropertiesSet() {
      ArgumentChecker.notNull(getSamplingPeriodName(), "samplingPeriodName");
      ArgumentChecker.notNull(getScheduleName(), "scheduleName");
      ArgumentChecker.notNull(getSamplingCalculatorName(), "samplingCalculatorName");
      ArgumentChecker.notNull(getMeanCalculatorName(), "meanCalculatorName");
      ArgumentChecker.notNull(getStdDevCalculatorName(), "stdDevCalculatorName");
      super.afterPropertiesSet();
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(NormalHistoricalVaRDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName(), getMeanCalculatorName(),
          getStdDevCalculatorName(), Double.toString(getConfidenceLevel()), Double.toString(getHorizon())));
      functions.add(functionConfiguration(EmpiricalHistoricalVaRDefaultPropertiesFunction.class, getSamplingPeriodName(), getScheduleName(), getSamplingCalculatorName(),
          Double.toString(getConfidenceLevel()), Double.toString(getHorizon())));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(EmpiricalHistoricalConditionalVaRFunction.class));
    functions.add(functionConfiguration(EmpiricalHistoricalVaRFunction.class));
    functions.add(functionConfiguration(NormalHistoricalVaRFunction.class));
  }

}

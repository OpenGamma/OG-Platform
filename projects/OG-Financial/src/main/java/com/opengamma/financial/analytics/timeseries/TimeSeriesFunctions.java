/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class TimeSeriesFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new TimeSeriesFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CreditSpreadCurveHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesSecurityFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesLatestPositionProviderIdValueFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesLatestSecurityValueFunction.class));
    functions.add(functionConfiguration(HistoricalTimeSeriesLatestValueFunction.class));
    functions.add(functionConfiguration(HistoricalValuationFunction.class));
    functions.add(functionConfiguration(YieldCurveHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(FXVolatilitySurfaceHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunction.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunctionDeprecated.class));
    functions.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesShiftFunctionDeprecated.class));
    functions.add(functionConfiguration(DefaultHistoricalTimeSeriesShiftFunction.class));
  }

}

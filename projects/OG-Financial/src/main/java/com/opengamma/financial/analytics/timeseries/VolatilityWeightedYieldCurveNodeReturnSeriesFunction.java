/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesRelativeWeightedDifferenceOperator;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesWeightedVolatilityOperator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class VolatilityWeightedYieldCurveNodeReturnSeriesFunction extends YieldCurveNodeReturnSeriesFunction {
  
  private static final TimeSeriesRelativeWeightedDifferenceOperator RELATIVE_WEIGHTED_DIFFERENCE = new TimeSeriesRelativeWeightedDifferenceOperator();
  
  @Override
  protected ValueProperties getResultProperties(ComputationTarget target) {
    return VolatilityWeightingFunctionUtils.addVolatilityWeightingProperties(super.getResultProperties(target));
  }

  @Override
  protected String getYCHTSStart(ValueProperties constraints) {
    Set<String> startDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    String startDate = Iterables.getOnlyElement(startDates);

    Set<String> volWeightingStartDates = constraints.getValues(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_START_DATE_PROPERTY);
    if (volWeightingStartDates == null || volWeightingStartDates.size() != 1) {
      // NOTE jonathan 2013-04-29 -- should start a day earlier so the result after weighting starts at the startDate,
      // but need to know previous date with data
      return startDate;
    } else {
      return Iterables.getOnlyElement(volWeightingStartDates);
    }
  }
  
  @Override
  protected LocalDateDoubleTimeSeries getReturnSeries(LocalDateDoubleTimeSeries ts, ValueRequirement desiredValue) {
    double lambda = Double.parseDouble(desiredValue.getConstraint(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_LAMBDA_PROPERTY));
    TimeSeriesWeightedVolatilityOperator weightedVolOp = TimeSeriesWeightedVolatilityOperator.relative(lambda);
    LocalDateDoubleTimeSeries weightedVolSeries = (LocalDateDoubleTimeSeries) weightedVolOp.evaluate(ts);
    return (LocalDateDoubleTimeSeries) RELATIVE_WEIGHTED_DIFFERENCE.evaluate(ts, weightedVolSeries);
  }
  
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesWeightedVolatilityOperator;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class VolatilityWeightedFXReturnSeriesFunction extends FXReturnSeriesFunction {
  
  @Override
  protected ValueProperties getResultProperties() {
    return VolatilityWeightingFunctionUtils.addVolatilityWeightingProperties(super.getResultProperties());
  }
  
  @Override
  protected String getSpotSeriesStart(ValueProperties constraints) {
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
  protected LocalDateDoubleTimeSeries getReturnSeries(LocalDateDoubleTimeSeries spotSeries, ValueRequirement desiredValue) {
    LocalDateDoubleTimeSeries differenceSeries = super.getReturnSeries(spotSeries, desiredValue);
    double lambda = Double.parseDouble(desiredValue.getConstraint(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_LAMBDA_PROPERTY));
    TimeSeriesWeightedVolatilityOperator weightedVol = new TimeSeriesWeightedVolatilityOperator(lambda);
    LocalDateDoubleTimeSeries weightedVolSeries = (LocalDateDoubleTimeSeries) weightedVol.evaluate(spotSeries);
    int n = weightedVolSeries.size();
    double endDateWeightedVol = weightedVolSeries.getLatestValueFast();
    double[] volWeightedDifferences = new double[n];
    for (int i = 0; i < n; i++) {
      volWeightedDifferences[i] = differenceSeries.getValueAtIndexFast(i) * endDateWeightedVol / weightedVolSeries.getValueAtIndexFast(i); 
    }
    LocalDateDoubleTimeSeries volWeightedDifferenceSeries = ImmutableLocalDateDoubleTimeSeries.of(weightedVolSeries.timesArrayFast(), volWeightedDifferences);
    return volWeightedDifferenceSeries;
  }
  
}

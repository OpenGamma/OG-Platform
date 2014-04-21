/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesRelativeWeightedDifferenceOperator;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesWeightedVolatilityOperator;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.timeseries.VolatilityWeightingFunctionUtils;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Calculates a PnL series by performing a full historical valuation over the required period, and weights the returns
 * by volatility.
 */
public class VolatilityWeightedHistoricalValuationPnLFunction extends HistoricalValuationPnLFunction {

  private static final TimeSeriesRelativeWeightedDifferenceOperator RELATIVE_WEIGHTED_DIFFERENCE = new TimeSeriesRelativeWeightedDifferenceOperator();
  
  @Override
  protected String getPriceSeriesStart(ValueProperties outputConstraints) {
    if (super.getPriceSeriesStart(outputConstraints) == null) {
      return null;
    }
    Set<String> volatilityWeightingStartDates = outputConstraints.getValues(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_START_DATE_PROPERTY);
    if (volatilityWeightingStartDates == null || volatilityWeightingStartDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(volatilityWeightingStartDates);
  }

  @Override
  protected void removeTransformationProperties(Builder builder) {
    super.removeTransformationProperties(builder);
    builder.withoutAny(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_LAMBDA_PROPERTY);
    builder.withoutAny(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_START_DATE_PROPERTY);
  }

  @Override
  protected void addTransformationProperties(Builder builder, ValueRequirement desiredValue) {
    VolatilityWeightingFunctionUtils.addVolatilityWeightingProperties(builder, desiredValue);
  }

  @Override
  protected DateDoubleTimeSeries<?> calculatePnlSeries(LocalDateDoubleTimeSeries priceSeries, FunctionExecutionContext executionContext, ValueRequirement desiredValue) {
    double lambda = Double.parseDouble(desiredValue.getConstraint(VolatilityWeightingFunctionUtils.VOLATILITY_WEIGHTING_LAMBDA_PROPERTY));
    TimeSeriesWeightedVolatilityOperator weightedVolatilityOperator = TimeSeriesWeightedVolatilityOperator.relative(lambda);
    DateDoubleTimeSeries<?> weightedVolatilitySeries = weightedVolatilityOperator.evaluate(priceSeries);
    LocalDateDoubleTimeSeries weightedPnlSeries = (LocalDateDoubleTimeSeries) RELATIVE_WEIGHTED_DIFFERENCE.evaluate(priceSeries, weightedVolatilitySeries);
    LocalDate pnlSeriesStart = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    if (pnlSeriesStart.isAfter(weightedPnlSeries.getEarliestTime())) {
      weightedPnlSeries = weightedPnlSeries.subSeries(pnlSeriesStart, true, weightedPnlSeries.getLatestTime(), true);
    }
    return weightedPnlSeries;
  }
  
}

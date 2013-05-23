/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.util.tuple.Pair;

/**
 * Function to source the latest time series data point from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class HistoricalTimeSeriesLatestValueFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Pair<LocalDate, Double> latestDataPoint = timeSeriesSource.getLatestDataPoint(target.getUniqueId());
    final String ageLimitValue = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
    final Period ageLimit = HistoricalTimeSeriesFunctionUtils.UNLIMITED_AGE_LIMIT_VALUE.equals(ageLimitValue) ? null : Period.parse(ageLimitValue);
    final Object value;
    if (checkMissing(executionContext, latestDataPoint, ageLimit)) {
      value = MissingInput.MISSING_MARKET_DATA;
    } else {
      final String adjusterString = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
      final HistoricalTimeSeriesAdjustment htsa = HistoricalTimeSeriesAdjustment.parse(adjusterString);
      value = htsa.adjust(latestDataPoint.getSecond());
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), value));
  }

  // TODO: reverse logic here to be checkAvailable()
  private boolean checkMissing(final FunctionExecutionContext executionContext, final Pair<LocalDate, Double> latestDataPoint, final Period ageLimit) {
    if (latestDataPoint == null) {
      return true;
    }
    if (ageLimit != null) {
      LocalDate now = LocalDate.now(executionContext.getValuationClock());
      Period difference = ageLimit.minus(Period.between(latestDataPoint.getFirst(), now));
      if (difference.isNegative()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE; // UID of the time series
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> adjustValues = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
    final Set<String> ageLimitValues = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
    if (adjustValues != null && adjustValues.size() == 1 && ageLimitValues != null && ageLimitValues.size() == 1) {
      return ImmutableSet.of();
    }
    final Builder constraints = desiredValue.getConstraints().copy();
    if (adjustValues == null || adjustValues.isEmpty()) {
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, "");
    } else {
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, adjustValues.iterator().next());
    }
    if (ageLimitValues == null || ageLimitValues.isEmpty()) {
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY, HistoricalTimeSeriesFunctionUtils.UNLIMITED_AGE_LIMIT_VALUE);
    } else {
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY, ageLimitValues.iterator().next());
    }
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), constraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.isEmpty()) {
      // Use full results - graph builder will compose correctly against the desired value
      return getResults(context, target);
    } else {
      // Use the substituted result
      return inputs.keySet();
    }
  }

}

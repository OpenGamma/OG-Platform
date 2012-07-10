/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.MissingMarketDataSentinel;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjustment;

/**
 * Function to source time series data from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class HistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesFunction.class);

  protected static HistoricalTimeSeries executeImpl(final FunctionExecutionContext executionContext, final HistoricalTimeSeriesSource timeSeriesSource, final ValueRequirement desiredValue) {
    final LocalDate startDate = DateConstraint.getLocalDate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    LocalDate endDate = DateConstraint.getLocalDate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY));
    if (endDate == null) {
      endDate = executionContext.getValuationClock().today();
    }
    final boolean includeEnd = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    HistoricalTimeSeries hts = timeSeriesSource.getHistoricalTimeSeries(desiredValue.getTargetSpecification().getUniqueId(), startDate, includeStart, endDate, includeEnd);
    final String adjusterString = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
    if (adjusterString != null) {
      hts = HistoricalTimeSeriesAdjustment.parse(adjusterString).adjust(hts);
    }
    return hts;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    Object value = executeImpl(executionContext, timeSeriesSource, desiredValue);
    if (value == null) {
      s_logger.error("Couldn't get time series {}", desiredValue);
      value = MissingMarketDataSentinel.getInstance();
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()), value));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder constraints = null;
    Set<String> values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, "");
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, "");
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    if (constraints == null) {
      // We can satisfy the desired value as-is
      return Collections.emptySet();
    } else {
      // We need to substitute ourselves with the adjusted constraints
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), constraints.get()));
    }
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

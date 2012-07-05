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
import javax.time.calendar.Period;

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

/**
 * Function to source time series data from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class HistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Property describing the "start date" of the time series value.
   */
  public static final String START_DATE_PROPERTY = "Start";
  /**
   * Property describing whether the start date was included in the time series.
   */
  public static final String INCLUDE_START_PROPERTY = "IncludeStart";
  /**
   * Property describing the "end date" of the time series value.
   */
  public static final String END_DATE_PROPERTY = "End";
  /**
   * Property describing whether the end date was included in the time series.
   */
  public static final String INCLUDE_END_PROPERTY = "IncludeEnd";

  /**
   * Value for {@link #INCLUDE_START_PROPERTY} or {@link #INCLUDE_END_PROPERTY}.
   */
  public static final String YES_VALUE = "Yes";
  /**
   * Value for {@link #INCLUDE_START_PROPERTY} or {@link #INCLUDE_END_PROPERTY}.
   */
  public static final String NO_VALUE = "No";

  private static boolean parseBoolean(final String str) {
    return YES_VALUE.equals(str);
  }

  /**
   * Parses a local date described on a constraint. The empty string is considered to be null. A string starting with a - is a period subtracted from the valuation date. Anything else is parsed using
   * LocalDate.
   * 
   * @param executionContext the execution context, containing a clock for the current valuation date
   * @param str the string to parse
   * @return the parsed object or null for no value
   */
  private static LocalDate parseLocalDate(final FunctionExecutionContext executionContext, final String str) {
    if (str.length() == 0) {
      return null;
    } else if (str.charAt(0) == '-') {
      return executionContext.getValuationClock().today().minus(Period.parse(str.substring(1)));
    } else {
      return LocalDate.parse(str);
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final LocalDate startDate = parseLocalDate(executionContext, desiredValue.getConstraint(START_DATE_PROPERTY));
    final boolean includeStart = parseBoolean(desiredValue.getConstraint(INCLUDE_START_PROPERTY));
    LocalDate endDate = parseLocalDate(executionContext, desiredValue.getConstraint(END_DATE_PROPERTY));
    if (endDate == null) {
      endDate = executionContext.getValuationClock().today();
    }
    final boolean includeEnd = parseBoolean(desiredValue.getConstraint(INCLUDE_END_PROPERTY));
    final HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(target.getUniqueId(), startDate, includeStart, endDate, includeEnd);
    if (timeSeries == null) {
      return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()),
          MissingMarketDataSentinel.getInstance()));
    } else {
      return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()), timeSeries));
    }
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
        .withAny(START_DATE_PROPERTY)
        .with(INCLUDE_START_PROPERTY, YES_VALUE, NO_VALUE)
        .withAny(END_DATE_PROPERTY)
        .with(INCLUDE_END_PROPERTY, YES_VALUE, NO_VALUE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder constraints = null;
    Set<String> values = desiredValue.getConstraints().getValues(START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(START_DATE_PROPERTY, "");
    }
    values = desiredValue.getConstraints().getValues(INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(INCLUDE_START_PROPERTY, YES_VALUE);
    }
    values = desiredValue.getConstraints().getValues(END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(END_DATE_PROPERTY, "");
    }
    values = desiredValue.getConstraints().getValues(INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(INCLUDE_END_PROPERTY, YES_VALUE);
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

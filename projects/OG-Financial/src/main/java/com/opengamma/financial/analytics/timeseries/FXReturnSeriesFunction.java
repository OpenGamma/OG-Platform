/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesPercentageChangeOperator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Calculates an absolute return series from a time-series of FX spot rates.
 */
public class FXReturnSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  /** Absolute returns */
  public static final String ABSOLUTE_RETURNS = "Absolute";
  /** Relative returns */
  public static final String RELATIVE_RETURNS = "Relative";
  /** Removes weekends */
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  /** A weekend calendar */
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  /** Calculates an absolute return time series */
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  /** Calculates a relative return time series */
  private static final TimeSeriesPercentageChangeOperator PERCENTAGE_CHANGE = new TimeSeriesPercentageChangeOperator();

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  /**
   * Gets the result properties for an FX return series with no transformation method set.
   * @return The result properties
   */
  protected ValueProperties getResultProperties() {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .with(ValuePropertyNames.TRANSFORMATION_METHOD, "None")
        .get();
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.RETURN_SERIES, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String spotSeriesStart = getSpotSeriesStart(constraints);
    if (spotSeriesStart == null) {
      return null;
    }
    final DateConstraint start = DateConstraint.parse(spotSeriesStart);
    final String returnSeriesEnd = getReturnSeriesEnd(constraints);
    if (returnSeriesEnd == null) {
      return null;
    }
    final DateConstraint end = DateConstraint.parse(returnSeriesEnd);
    final Set<String> includeStarts = constraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if (includeStarts != null && includeStarts.size() != 1) {
      return null;
    }
    final boolean includeStart = includeStarts == null ? true : HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(Iterables.getOnlyElement(includeStarts));
    final Set<String> includeEnds = constraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if (includeEnds != null && includeEnds.size() != 1) {
      return null;
    }
    final boolean includeEnd = includeEnds == null ? false : HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(Iterables.getOnlyElement(includeEnds));
    final Set<String> samplingMethod = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingMethod == null || samplingMethod.size() != 1) {
      return null;
    }
    final Set<String> scheduleMethod = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleMethod == null || scheduleMethod.size() != 1) {
      return null;
    }
    final Set<String> returnMethods = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
    if (returnMethods == null || returnMethods.size() != 1) {
      final ValueProperties newConstraints = constraints.copy()
          .withoutAny(ValuePropertyNames.RETURN_CALCULATOR)
          .with(ValuePropertyNames.RETURN_CALCULATOR, ABSOLUTE_RETURNS)
          .get();
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.RETURN_SERIES, target.toSpecification(), newConstraints));
    }
    return ImmutableSet.of(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement((UnorderedCurrencyPair) target.getValue(), start, includeStart, end, includeEnd));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    if (ValueRequirementNames.RETURN_SERIES.equals(input.getValueName())) {
      return Collections.singleton(input);
    }
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.RETURN_SERIES, target.toSpecification(), getResultProperties()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ComputedValue timeSeriesValue = inputs.getComputedValue(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
    final DateDoubleTimeSeries<?> timeSeries = (DateDoubleTimeSeries<?>) timeSeriesValue.getValue();
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(timeSeriesValue.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final LocalDate spotSeriesStart = DateConstraint.evaluate(executionContext, getSpotSeriesStart(desiredValue.getConstraints()));
    final LocalDate returnSeriesStart = DateConstraint.evaluate(executionContext, getReturnSeriesStart(desiredValue.getConstraints()));
    if (spotSeriesStart.isAfter(returnSeriesStart)) {
      throw new OpenGammaRuntimeException("Return series start date cannot be before spot series start date");
    }
    final LocalDate returnSeriesEnd = DateConstraint.evaluate(executionContext, getReturnSeriesEnd(desiredValue.getConstraints()));
    final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final Schedule scheduleCalculator = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
    final TimeSeriesSamplingFunction samplingFunction = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(spotSeriesStart, returnSeriesEnd, true, false), WEEKEND_CALENDAR);
    LocalDateDoubleTimeSeries sampledTimeSeries = samplingFunction.getSampledTimeSeries(timeSeries, dates);
    sampledTimeSeries = sampledTimeSeries.reciprocal(); // Implementation note: to obtain the series for one unit of non-base currency expressed in base currency.
    LocalDateDoubleTimeSeries returnSeries = getReturnSeries(sampledTimeSeries, desiredValue);

    // Clip the time-series to the range originally asked for
    returnSeries = returnSeries.subSeries(returnSeriesStart, includeStart, returnSeries.getLatestTime(), true);

    return ImmutableSet.of(new ComputedValue(new ValueSpecification(ValueRequirementNames.RETURN_SERIES, target.toSpecification(), desiredValue.getConstraints()), returnSeries));
  }

  /**
   * Gets the spot series start date from the desired value constraints.
   * @param constraints The constraints
   * @return The spot series start date
   */
  protected String getSpotSeriesStart(final ValueProperties constraints) {
    return getReturnSeriesStart(constraints);
  }

  /**
   * Gets the return series start date from the desired value constraints.
   * @param constraints The constraints
   * @return The return series start date
   */
  protected String getReturnSeriesStart(final ValueProperties constraints) {
    final Set<String> startDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(startDates);
  }

  /**
   * Gets the return series end date from the desired value constraints.
   * @param constraints The constraints
   * @return The return series end date
   */
  protected String getReturnSeriesEnd(final ValueProperties constraints) {
    final Set<String> endDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if (endDates == null || endDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(endDates);
  }

  /**
   * Gets the return series from the spot series.
   * @param spotSeries The spot FX series
   * @param desiredValue The desired return series requirement
   * @return The FX return series
   */
  protected LocalDateDoubleTimeSeries getReturnSeries(final LocalDateDoubleTimeSeries spotSeries, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> returnProperty = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
    if (returnProperty != null && returnProperty.size() == 1 && Iterables.getOnlyElement(returnProperty).equals(RELATIVE_RETURNS)) {
      return (LocalDateDoubleTimeSeries) PERCENTAGE_CHANGE.evaluate(spotSeries);
    }
    return (LocalDateDoubleTimeSeries) DIFFERENCE.evaluate(spotSeries);
  }
}

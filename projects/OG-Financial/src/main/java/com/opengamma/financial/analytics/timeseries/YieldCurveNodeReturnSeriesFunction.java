/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

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
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Calculates return series for the market instruments at the nodal points of a yield curve.
 */
public class YieldCurveNodeReturnSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  
  @Override
  public ComputationTargetType getTargetType() {
    // NOTE jonathan 2013-04-23 -- should be ComputationTargetType.NULL
    return ComputationTargetType.CURRENCY;
  }

  protected ValueProperties getResultProperties(ComputationTarget target) {
    ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .with(ValuePropertyNames.CURRENCY, ((Currency) target.getValue()).getCode()).get();
    return properties;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties properties = getResultProperties(target);
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    Currency currency = (Currency) target.getValue();
    ValueProperties constraints = desiredValue.getConstraints();
    Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    String curveName = Iterables.getOnlyElement(curveNames);
    
    DateConstraint start = DateConstraint.parse(getYCHTSStart(constraints));
    DateConstraint end = DateConstraint.parse(getReturnSeriesEnd(constraints));
    if (start == null || end == null) {
      return null;
    }
    
    Set<String> includeStarts = constraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if (includeStarts != null && includeStarts.size() != 1) {
      return null;
    }
    boolean includeStart = includeStarts == null ? true : HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(Iterables.getOnlyElement(includeStarts));
    Set<String> includeEnds = constraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if (includeEnds != null && includeEnds.size() != 1) {
      return null;
    }
    boolean includeEnd = includeEnds == null ? false : HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(Iterables.getOnlyElement(includeEnds));
    
    return ImmutableSet.of(
        HistoricalTimeSeriesFunctionUtils.createYCHTSRequirement(currency, curveName, MarketDataRequirementNames.MARKET_VALUE, null, start, includeStart, end, includeEnd),
        getCurveSpecRequirement(currency, curveName));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final LocalDate tsStart = DateConstraint.evaluate(executionContext, getYCHTSStart(desiredValue.getConstraints()));
    final LocalDate returnSeriesStart = DateConstraint.evaluate(executionContext, getReturnSeriesStart(desiredValue.getConstraints()));
    if (tsStart.isAfter(returnSeriesStart)) {
      throw new OpenGammaRuntimeException("Return series start date cannot be before time-series start date");
    }
    LocalDate returnSeriesEnd = DateConstraint.evaluate(executionContext, getReturnSeriesEnd(desiredValue.getConstraints()));
    String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(tsStart, returnSeriesEnd, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    
    final ComputedValue bundleValue = inputs.getComputedValue(ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES);
    final HistoricalTimeSeriesBundle bundle = (HistoricalTimeSeriesBundle) bundleValue.getValue();
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(bundleValue.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
    
    TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeriesVector = getReturnSeriesVector(curveSpec, bundle, schedule, samplingFunction, returnSeriesStart, includeStart, desiredValue);
    ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(resultSpec, returnSeriesVector));
  }

  protected String getYCHTSStart(ValueProperties constraints) {
    return getReturnSeriesStart(constraints);
  }
  
  protected String getReturnSeriesStart(ValueProperties constraints) {
    Set<String> startDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(startDates);
  }
  
  protected String getReturnSeriesEnd(ValueProperties constraints) {
    Set<String> endDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if (endDates == null || endDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(endDates);
  }
  
  protected LocalDateDoubleTimeSeries getReturnSeries(LocalDateDoubleTimeSeries ts, FixedIncomeStripWithSecurity strip, ValueRequirement desiredValue) {
    return (LocalDateDoubleTimeSeries) DIFFERENCE.evaluate(ts);
  }
  
  private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getReturnSeriesVector(InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      HistoricalTimeSeriesBundle timeSeriesBundle, LocalDate[] schedule, TimeSeriesSamplingFunction samplingFunction, LocalDate startDate, boolean includeStart, ValueRequirement desiredValue) {
    Set<FixedIncomeStripWithSecurity> strips = curveSpec.getStrips();
    Tenor[] tenorsArray = new Tenor[strips.size()];
    LocalDateDoubleTimeSeries[] returnSeriesArray = new LocalDateDoubleTimeSeries[strips.size()];
    int stripIndex = 0;
    for (FixedIncomeStripWithSecurity strip : strips) {
      HistoricalTimeSeries dbNodeTimeSeries = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, strip.getSecurityIdentifier());
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("No time-series for strip referencing security " + strip.getSecurityIdentifier());
      }
      LocalDateDoubleTimeSeries nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      LocalDateDoubleTimeSeries returnSeries = getReturnSeries(nodeTimeSeries, strip, desiredValue);
      
      // Clip the time-series to the range originally asked for
      returnSeries = returnSeries.subSeries(startDate, includeStart, returnSeries.getLatestTime(), true);
      
      tenorsArray[stripIndex] = strip.getResolvedTenor(); 
      returnSeriesArray[stripIndex] = returnSeries;
      stripIndex++;
    }
    return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(tenorsArray, returnSeriesArray);
  }
  
  //-------------------------------------------------------------------------
  private ValueRequirement getCurveSpecRequirement(Currency currency, String yieldCurveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, yieldCurveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.CURRENCY.specification(currency), properties);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

}

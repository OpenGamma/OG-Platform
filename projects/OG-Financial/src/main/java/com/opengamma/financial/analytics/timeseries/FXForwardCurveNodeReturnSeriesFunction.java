/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.HashSet;
import java.util.Iterator;
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
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * Calculates return series for the market instruments at the nodal points of a yield curve.
 */
public class FXForwardCurveNodeReturnSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    // NOTE jonathan 2013-04-23 -- should be ComputationTargetType.NULL
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  protected ValueProperties getResultProperties(ComputationTarget target) {
    ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .with(ValuePropertyNames.TRANSFORMATION_METHOD, "None")
        .get();
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties properties = getResultProperties(target);
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final UnorderedCurrencyPair currencyPair = (UnorderedCurrencyPair) target.getValue();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final String curveName = Iterables.getOnlyElement(curveNames);
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    String curveCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);

    String ychtsStart = getFCHTSStart(constraints);
    if (ychtsStart == null) {
      return null;
    }
    DateConstraint start = DateConstraint.parse(ychtsStart);
    String returnSeriesEnd = getReturnSeriesEnd(constraints);
    if (returnSeriesEnd == null) {
      return null;
    }
    DateConstraint end = DateConstraint.parse(returnSeriesEnd);
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
    final Set<String> samplingMethod = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingMethod == null || samplingMethod.size() != 1) {
      return null;
    }
    final Set<String> scheduleMethod = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleMethod == null || scheduleMethod.size() != 1) {
      return null;
    }
    Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(HistoricalTimeSeriesFunctionUtils.createFXForwardCurveHTSRequirement(currencyPair, curveName, MarketDataRequirementNames.MARKET_VALUE,
        null, start, includeStart, end, includeEnd));

    ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (FXImpliedYieldCurveFunction.FX_IMPLIED.equals(curveCalculationConfig.getCalculationMethod())) {
      Currency impliedCcy = ComputationTargetType.CURRENCY.resolve(curveCalculationConfig.getTarget().getUniqueId());
      String baseCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfig.getExogenousConfigData().entrySet()).getKey();
      MultiCurveCalculationConfig baseCurveCalculationConfig = curveCalculationConfigSource.getConfig(baseCalculationConfigName);
      Currency baseCcy = ComputationTargetType.CURRENCY.resolve(baseCurveCalculationConfig.getTarget().getUniqueId());
      requirements.add(getFXForwardCurveDefinitionRequirement(UnorderedCurrencyPair.of(impliedCcy, baseCcy), curveName));
    } else {
      return null;
    }
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final LocalDate tsStart = DateConstraint.evaluate(executionContext, getFCHTSStart(desiredValue.getConstraints()));
    final LocalDate returnSeriesStart = DateConstraint.evaluate(executionContext, getReturnSeriesStart(desiredValue.getConstraints()));
    if (tsStart.isAfter(returnSeriesStart)) {
      throw new OpenGammaRuntimeException("Return series start date cannot be before time-series start date");
    }
    LocalDate returnSeriesEnd = DateConstraint.evaluate(executionContext, getReturnSeriesEnd(desiredValue.getConstraints()));
    String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);

    //REVIEW emcleod should "fromEnd" be hard-coded?
    LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(tsStart, returnSeriesEnd, true, false), WEEKEND_CALENDAR);

    final ComputedValue bundleValue = inputs.getComputedValue(ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES);
    final HistoricalTimeSeriesBundle bundle = (HistoricalTimeSeriesBundle) bundleValue.getValue();
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(bundleValue.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final FXForwardCurveDefinition fxForwardCurveDefinition = (FXForwardCurveDefinition) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION);

    final Tenor[] tenors = fxForwardCurveDefinition.getTenors();

    TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeriesVector = getReturnSeriesVector(bundle, tenors,
        schedule, samplingFunction, returnSeriesStart, includeStart, desiredValue);
    ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(resultSpec, returnSeriesVector));
  }

  protected String getFCHTSStart(ValueProperties constraints) {
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

  protected LocalDateDoubleTimeSeries getReturnSeries(LocalDateDoubleTimeSeries ts, ValueRequirement desiredValue) {
    return (LocalDateDoubleTimeSeries) DIFFERENCE.evaluate(ts);
  }

  private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getReturnSeriesVector(HistoricalTimeSeriesBundle timeSeriesBundle,
      Tenor[] tenors, LocalDate[] schedule, TimeSeriesSamplingFunction samplingFunction,
      LocalDate startDate, boolean includeStart, ValueRequirement desiredValue) {
    LocalDateDoubleTimeSeries[] returnSeriesArray = new LocalDateDoubleTimeSeries[tenors.length];
    if (tenors.length != timeSeriesBundle.size(MarketDataRequirementNames.MARKET_VALUE)) {
      throw new OpenGammaRuntimeException("Expected " + tenors.length + " nodal market data time-series but have " + timeSeriesBundle.size(MarketDataRequirementNames.MARKET_VALUE));
    }
    Iterator<HistoricalTimeSeries> tsIterator = timeSeriesBundle.iterator(MarketDataRequirementNames.MARKET_VALUE);
    if (tsIterator == null) {
      throw new OpenGammaRuntimeException("No nodal market data time-series available");
    }
    for (int t = 0; t < tenors.length; t++) {
      Tenor tenor = tenors[t];
      HistoricalTimeSeries dbNodeTimeSeries = tsIterator.next();
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("No time-series for strip with tenor " + tenor);
      }
      LocalDateDoubleTimeSeries nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      LocalDateDoubleTimeSeries returnSeries = getReturnSeries(nodeTimeSeries, desiredValue);

      // Clip the time-series to the range originally asked for
      returnSeries = returnSeries.subSeries(startDate, includeStart, returnSeries.getLatestTime(), true);

      returnSeriesArray[t] = returnSeries;
    }
    return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(tenors, returnSeriesArray);
  }

  private ValueRequirement getFXForwardCurveDefinitionRequirement(UnorderedCurrencyPair currencies, String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies), properties);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

}

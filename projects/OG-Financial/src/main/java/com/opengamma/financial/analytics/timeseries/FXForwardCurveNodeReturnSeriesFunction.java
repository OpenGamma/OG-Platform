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

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    // NOTE jonathan 2013-04-23 -- should be ComputationTargetType.NULL
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  protected ValueProperties getResultProperties(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION).withAny(ValuePropertyNames.SCHEDULE_CALCULATOR).withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .with(ValuePropertyNames.TRANSFORMATION_METHOD, "None").get();
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target);
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
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
    final String curveCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);

    final String ychtsStart = getFCHTSStart(constraints);
    if (ychtsStart == null) {
      return null;
    }
    final DateConstraint start = DateConstraint.parse(ychtsStart);
    final String returnSeriesEnd = getReturnSeriesEnd(constraints);
    if (returnSeriesEnd == null) {
      return null;
    }
    final DateConstraint end = DateConstraint.parse(returnSeriesEnd);
    if (start == null || end == null) {
      return null;
    }

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
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(HistoricalTimeSeriesFunctionUtils.createFXForwardCurveHTSRequirement(currencyPair, curveName, MarketDataRequirementNames.MARKET_VALUE, null, start, includeStart, end,
        includeEnd));

    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (FXImpliedYieldCurveFunction.FX_IMPLIED.equals(curveCalculationConfig.getCalculationMethod())) {
      final Currency impliedCcy = ComputationTargetType.CURRENCY.resolve(curveCalculationConfig.getTarget().getUniqueId());
      final String baseCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfig.getExogenousConfigData().entrySet()).getKey();
      final MultiCurveCalculationConfig baseCurveCalculationConfig = _curveCalculationConfigSource.getConfig(baseCalculationConfigName);
      final Currency baseCcy = ComputationTargetType.CURRENCY.resolve(baseCurveCalculationConfig.getTarget().getUniqueId());
      requirements.add(getFXForwardCurveDefinitionRequirement(UnorderedCurrencyPair.of(impliedCcy, baseCcy), curveName));
    } else {
      return null;
    }
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final LocalDate tsStart = DateConstraint.evaluate(executionContext, getFCHTSStart(desiredValue.getConstraints()));
    final LocalDate returnSeriesStart = DateConstraint.evaluate(executionContext, getReturnSeriesStart(desiredValue.getConstraints()));
    if (tsStart.isAfter(returnSeriesStart)) {
      throw new OpenGammaRuntimeException("Return series start date cannot be before time-series start date");
    }
    final LocalDate returnSeriesEnd = DateConstraint.evaluate(executionContext, getReturnSeriesEnd(desiredValue.getConstraints()));
    final String scheduleCalculatorName = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final String samplingFunctionName = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);

    //REVIEW emcleod should "fromEnd" be hard-coded?
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(tsStart, returnSeriesEnd, true, false), WEEKEND_CALENDAR);

    final ComputedValue bundleValue = inputs.getComputedValue(ValueRequirementNames.FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES);
    final HistoricalTimeSeriesBundle bundle = (HistoricalTimeSeriesBundle) bundleValue.getValue();
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(bundleValue.getSpecification().getProperty(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final FXForwardCurveDefinition fxForwardCurveDefinition = (FXForwardCurveDefinition) inputs.getValue(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION);

    final Tenor[] tenors = fxForwardCurveDefinition.getTenorsArray();

    final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeriesVector = getReturnSeriesVector(bundle, tenors, schedule, samplingFunction, returnSeriesStart, includeStart, desiredValue);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_RETURN_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return ImmutableSet.of(new ComputedValue(resultSpec, returnSeriesVector));
  }

  protected String getFCHTSStart(final ValueProperties constraints) {
    return getReturnSeriesStart(constraints);
  }

  protected String getReturnSeriesStart(final ValueProperties constraints) {
    final Set<String> startDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(startDates);
  }

  protected String getReturnSeriesEnd(final ValueProperties constraints) {
    final Set<String> endDates = constraints.getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if (endDates == null || endDates.size() != 1) {
      return null;
    }
    return Iterables.getOnlyElement(endDates);
  }

  protected LocalDateDoubleTimeSeries getReturnSeries(final LocalDateDoubleTimeSeries ts, final ValueRequirement desiredValue) {
    return (LocalDateDoubleTimeSeries) DIFFERENCE.evaluate(ts);
  }

  private TenorLabelledLocalDateDoubleTimeSeriesMatrix1D getReturnSeriesVector(final HistoricalTimeSeriesBundle timeSeriesBundle, final Tenor[] tenors, final LocalDate[] schedule,
      final TimeSeriesSamplingFunction samplingFunction, final LocalDate startDate, final boolean includeStart, final ValueRequirement desiredValue) {
    final LocalDateDoubleTimeSeries[] returnSeriesArray = new LocalDateDoubleTimeSeries[tenors.length];
    if (tenors.length != timeSeriesBundle.size(MarketDataRequirementNames.MARKET_VALUE)) {
      throw new OpenGammaRuntimeException("Expected " + tenors.length + " nodal market data time-series but have " + timeSeriesBundle.size(MarketDataRequirementNames.MARKET_VALUE));
    }
    final Iterator<HistoricalTimeSeries> tsIterator = timeSeriesBundle.iterator(MarketDataRequirementNames.MARKET_VALUE);
    if (tsIterator == null) {
      throw new OpenGammaRuntimeException("No nodal market data time-series available");
    }
    for (int t = 0; t < tenors.length; t++) {
      final Tenor tenor = tenors[t];
      final HistoricalTimeSeries dbNodeTimeSeries = tsIterator.next();
      if (dbNodeTimeSeries == null) {
        throw new OpenGammaRuntimeException("No time-series for strip with tenor " + tenor);
      }
      final LocalDateDoubleTimeSeries nodeTimeSeries = samplingFunction.getSampledTimeSeries(dbNodeTimeSeries.getTimeSeries(), schedule);
      LocalDateDoubleTimeSeries returnSeries = getReturnSeries(nodeTimeSeries, desiredValue);

      // Clip the time-series to the range originally asked for
      returnSeries = returnSeries.subSeries(startDate, includeStart, returnSeries.getLatestTime(), true);

      returnSeriesArray[t] = returnSeries;
    }
    return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(tenors, returnSeriesArray);
  }

  private ValueRequirement getFXForwardCurveDefinitionRequirement(final UnorderedCurrencyPair currencies, final String curveName) {
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies), properties);
  }

  private Schedule getScheduleCalculator(final String scheduleCalculatorName) {
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorName);
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final String samplingFunctionName) {
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }
}

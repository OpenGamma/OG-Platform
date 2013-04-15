/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.pnl.SensitivityAndReturnDataBundle;
import com.opengamma.analytics.financial.pnl.SensitivityPnLCalculator;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.sensitivity.Sensitivity;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTimeSeriesProvider;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Computes a Profit and Loss time series for a position based on value greeks.
 * Takes in a set of specified value greeks (which will be part of configuration),
 * converts to sensitivities, loads the underlying time series, and calculates
 * a series of P&L based on {@link SensitivityPnLCalculator}.
 * 
 */
public class ValueGreekSensitivityPnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final Greek GREEK = Greek.DELTA;
  private static final String REQUIREMENT_NAME = ValueRequirementNames.VALUE_DELTA; //TODO remove hard-coding
  private static final SensitivityPnLCalculator PNL_CALCULATOR = new SensitivityPnLCalculator();
  private final String _resolutionKey;

  public ValueGreekSensitivityPnLFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    String currency = null;
    for (final ComputedValue value : inputs.getAllValues()) {
      final String newCurrency = value.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
      if (newCurrency != null) {
        if (currency != null && !currency.equals(newCurrency)) {
          return null;
        }
        currency = newCurrency;
      }
    }
    final Position position = target.getPosition();
    final ComputationTargetSpecification positionSpec = target.toSpecification();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> samplingPeriodName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    final Set<String> scheduleCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> samplingFunctionName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    final Set<String> returnCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.RETURN_CALCULATOR);
    final HistoricalTimeSeries timeSeries = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final SensitivityAndReturnDataBundle[] dataBundleArray = new SensitivityAndReturnDataBundle[1];
    final Double value = (Double) inputs.getValue(REQUIREMENT_NAME);
    final ValueGreek valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(REQUIREMENT_NAME);
    final Sensitivity<?> sensitivity = new ValueGreekSensitivity(valueGreek, position.getUniqueId().toString());
    final Map<UnderlyingType, DoubleTimeSeries<?>> tsReturns = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
    final Period samplingPeriod = getSamplingPeriod(samplingPeriodName);
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    final TimeSeriesReturnCalculator returnCalculator = getTimeSeriesReturnCalculator(returnCalculatorName);
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    final LocalDateDoubleTimeSeries sampledTS = samplingFunction.getSampledTimeSeries(timeSeries.getTimeSeries(), schedule);
    for (final UnderlyingType underlyingType : valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings()) {
      if (underlyingType != UnderlyingType.SPOT_PRICE) {
        throw new OpenGammaRuntimeException("Have hard-coded to only use delta; should not have anything with " + underlyingType + " as the underlying type");
      }
      tsReturns.put(underlyingType, returnCalculator.evaluate(sampledTS));
    }
    dataBundleArray[0] = new SensitivityAndReturnDataBundle(sensitivity, value, tsReturns);
    final DoubleTimeSeries<?> result = PNL_CALCULATOR.evaluate(dataBundleArray);
    // Please see http://jira.opengamma.com/browse/PLAT-2330 for information about the PROPERTY_PNL_CONTRIBUTIONS constant
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName.iterator().next())
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta")
        .get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.PNL_SERIES, positionSpec, properties);
    final ComputedValue resultValue = new ComputedValue(resultSpecification, result);
    return Collections.singleton(resultValue);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getPosition().getSecurity() instanceof EquityOptionSecurity; //TODO need to widen this
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> samplingPeriodName = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodName == null || samplingPeriodName.isEmpty() || samplingPeriodName.size() != 1) {
      return null;
    }
    final Set<String> scheduleCalculatorName = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculatorName == null || scheduleCalculatorName.isEmpty() || scheduleCalculatorName.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionName = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingFunctionName == null || samplingFunctionName.isEmpty() || samplingFunctionName.size() != 1) {
      return null;
    }
    final Set<String> returnCalculatorName = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
    if (returnCalculatorName == null || returnCalculatorName.isEmpty() || returnCalculatorName.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(new ValueRequirement(REQUIREMENT_NAME, target.toSpecification()));
    final UnderlyingTimeSeriesProvider timeSeriesProvider = new UnderlyingTimeSeriesProvider(OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context), _resolutionKey,
        context.getSecuritySource());
    requirements.add(timeSeriesProvider.getSeriesRequirement(GREEK, (FinancialSecurity) target.getPosition().getSecurity(), DateConstraint.VALUATION_TIME.minus(samplingPeriodName.iterator().next()),
        DateConstraint.VALUATION_TIME));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    // Please see http://jira.opengamma.com/browse/PLAT-2330 for information about the PROPERTY_PNL_CONTRIBUTIONS constant
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
    results.add(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
    return results;
  }


  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    String currency = null;
    for (final ValueSpecification spec : inputs.keySet()) {
      final String newCurrency = spec.getProperty(ValuePropertyNames.CURRENCY);
      if (newCurrency != null) {
        if (currency != null && !currency.equals(newCurrency)) {
          return null;
        }
        currency = newCurrency;
      }
    }
    // Please see http://jira.opengamma.com/browse/PLAT-2330 for information about the PROPERTY_PNL_CONTRIBUTIONS constant
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
    if (samplingPeriodNames == null || samplingPeriodNames.isEmpty() || samplingPeriodNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique sampling period name: " + samplingPeriodNames);
    }
    return Period.parse(samplingPeriodNames.iterator().next());
  }

  private Schedule getScheduleCalculator(final Set<String> scheduleCalculatorNames) {
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.isEmpty() || scheduleCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique schedule calculator name: " + scheduleCalculatorNames);
    }
    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorNames.iterator().next());
  }

  private TimeSeriesSamplingFunction getSamplingFunction(final Set<String> samplingFunctionNames) {
    if (samplingFunctionNames == null || samplingFunctionNames.isEmpty() || samplingFunctionNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique sampling function name: " + samplingFunctionNames);
    }
    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionNames.iterator().next());
  }

  private TimeSeriesReturnCalculator getTimeSeriesReturnCalculator(final Set<String> calculatorNames) {
    if (calculatorNames == null || calculatorNames.isEmpty() || calculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique return calculator name: " + calculatorNames);
    }
    return TimeSeriesReturnCalculatorFactory.getReturnCalculator(calculatorNames.iterator().next());
  }
}

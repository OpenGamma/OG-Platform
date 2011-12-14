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

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTimeSeriesProvider;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.pnl.SensitivityAndReturnDataBundle;
import com.opengamma.financial.pnl.SensitivityPnLCalculator;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.schedule.Schedule;
import com.opengamma.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Computes a Profit and Loss time series for a position based on value greeks.
 * Takes in a set of specified value greeks (which will be part of configuration),
 * converts to sensitivities, loads the underlying time series, and calculates
 * a series of P&L based on {@link SensitivityPnLCalculator}.
 * 
 */
public class ValueGreekSensitivityPnLFunction extends AbstractFunction.NonCompiledInvoker {
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
    for (ComputedValue value : inputs.getAllValues()) {
      String newCurrency = value.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
      if (newCurrency != null) {
        if (currency != null && !currency.equals(newCurrency)) {
          return null;
        }
        currency = newCurrency;
      }
    }
    final Position position = target.getPosition();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> samplingPeriodName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    final Set<String> scheduleCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> samplingFunctionName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    final Set<String> returnCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.RETURN_CALCULATOR);
    final UnderlyingTimeSeriesProvider underlyingTimeSeriesProvider = new UnderlyingTimeSeriesProvider(historicalSource, _resolutionKey, securitySource);
    final SensitivityAndReturnDataBundle[] dataBundleArray = new SensitivityAndReturnDataBundle[1];
    final Object valueObj = inputs.getValue(REQUIREMENT_NAME);
    if (valueObj == null) {
      throw new OpenGammaRuntimeException("Could not get delta for " + position);
    }
    final Double value = (Double) valueObj;
    final ValueGreek valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(REQUIREMENT_NAME);
    final Sensitivity<?> sensitivity = new ValueGreekSensitivity(valueGreek, position.getUniqueId().toString());
    final Map<UnderlyingType, DoubleTimeSeries<?>> tsReturns = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
    final Period samplingPeriod = getSamplingPeriod(samplingPeriodName);
    final LocalDate startDate = now.minus(samplingPeriod);
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);     
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    final TimeSeriesReturnCalculator returnCalculator = getTimeSeriesReturnCalculator(returnCalculatorName);
    final LocalDate[] schedule = scheduleCalculator.getSchedule(startDate, now, true, false); //REVIEW emcleod should "fromEnd" be hard-coded? 
    for (final UnderlyingType underlyingType : valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings()) {
      if (underlyingType != UnderlyingType.SPOT_PRICE) {
        throw new OpenGammaRuntimeException("Have hard-coded to only use delta; should not have anything with " + underlyingType + " as the underlying type");
      }
      final DoubleTimeSeries<?> timeSeries = underlyingTimeSeriesProvider.getSeries(GREEK, security, startDate, now);
      final DoubleTimeSeries<?> sampledTS = samplingFunction.getSampledTimeSeries(timeSeries, schedule);      
      tsReturns.put(underlyingType, returnCalculator.evaluate(sampledTS));
    }
    dataBundleArray[0] = new SensitivityAndReturnDataBundle(sensitivity, value, tsReturns);
    final DoubleTimeSeries<?> result = PNL_CALCULATOR.evaluate(dataBundleArray);
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
      .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
      .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName.iterator().next())
      .get();
    final ValueRequirement resultRequirements = new ValueRequirement(ValueRequirementNames.PNL_SERIES, position, properties);
    final ValueSpecification resultSpecification = new ValueSpecification(resultRequirements, getUniqueId());
    final ComputedValue resultValue = new ComputedValue(resultSpecification, result);
    return Collections.singleton(resultValue);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquityOptionSecurity; //TODO need to widen this
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (!canApplyTo(context, target)) {
      return null;
    }
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
    requirements.add(new ValueRequirement(REQUIREMENT_NAME, target.getPosition()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    final ValueProperties properties = createValueProperties()
      .withAny(ValuePropertyNames.CURRENCY)
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.RETURN_CALCULATOR).get();
    results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition(), properties), getUniqueId()));
    return results;
  }

  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    String currency = null;
    for (ValueSpecification spec : inputs.keySet()) {
      String newCurrency = spec.getProperty(ValuePropertyNames.CURRENCY);
      if (newCurrency != null) {
        if (currency != null && !currency.equals(newCurrency)) {
          return null;
        }
        currency = newCurrency;
      }
    }
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, currency)
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.RETURN_CALCULATOR).get();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition(), properties), getUniqueId()));
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

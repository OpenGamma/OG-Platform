/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.greeks.AvailableValueGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTypeToHistoricalTimeSeries;
import com.opengamma.financial.analytics.timeseries.Schedule;
import com.opengamma.financial.analytics.timeseries.ScheduleCalculatorFactory;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.pnl.SensitivityAndReturnDataBundle;
import com.opengamma.financial.pnl.SensitivityPnLCalculator;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Computes a Profit and Loss time series for a position based on value greeks.
 * Takes in a set of specified value greeks (which will be part of configuration),
 * converts to sensitivities, loads the underlying time series, and calculates
 * a series of P&L based on {@link SensitivityPnLCalculator}.
 * 
 */
public class PositionValueGreekSensitivityPnLFunction extends AbstractFunction implements FunctionInvoker {
  private final String _dataSourceName;
  private final LocalDate _startDate;
  private final Set<ValueGreek> _valueGreeks;
  private final Set<String> _valueGreekRequirementNames;
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final Schedule _scheduleCalculator;
  private final TimeSeriesSamplingFunction _samplingCalculator;
  private static final SensitivityPnLCalculator PNL_CALCULATOR = new SensitivityPnLCalculator();

  public PositionValueGreekSensitivityPnLFunction(final String dataSourceName, final String startDate, final String returnCalculatorName,
      final String scheduleName, final String samplingFunctionName, final String valueGreekRequirementNames) {
    this(dataSourceName, startDate, returnCalculatorName, scheduleName, samplingFunctionName, new String[] {valueGreekRequirementNames});
  }

  public PositionValueGreekSensitivityPnLFunction(final String dataSourceName, final String startDate, final String returnCalculatorName,
      final String scheduleName, final String samplingFunctionName, final String... valueGreekRequirementNames) {
    Validate.notNull(dataSourceName, "data source name");
    Validate.notNull(startDate, "start date");
    _dataSourceName = dataSourceName;
    _startDate = LocalDate.parse(startDate);
    _valueGreeks = new HashSet<ValueGreek>();
    _valueGreekRequirementNames = new HashSet<String>();
    for (final String valueGreekRequirementName : valueGreekRequirementNames) {
      _valueGreekRequirementNames.add(valueGreekRequirementName);
      _valueGreeks.add(AvailableValueGreeks.getValueGreekForValueRequirementName(valueGreekRequirementName));
    }
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName, CalculationMode.STRICT);
    _scheduleCalculator = ScheduleCalculatorFactory.getSchedule(scheduleName);
    _samplingCalculator = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalDataSource historicalDataProvider = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final SecuritySource securitySource = executionContext.getSecuritySource();
    final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position), getUniqueIdentifier());
    final SensitivityAndReturnDataBundle[] dataBundleArray = new SensitivityAndReturnDataBundle[_valueGreekRequirementNames.size()];
    int i = 0;
    for (final String valueGreekRequirementName : _valueGreekRequirementNames) {
      final Object valueObj = inputs.getValue(valueGreekRequirementName);
      if (valueObj instanceof Double) {
        final Double value = (Double) valueObj;
        final ValueGreek valueGreek = AvailableValueGreeks.getValueGreekForValueRequirementName(valueGreekRequirementName);
        final Sensitivity<?> sensitivity = new ValueGreekSensitivity(valueGreek, position.getUniqueIdentifier().toString());
        final Map<UnderlyingType, DoubleTimeSeries<?>> tsReturns = new HashMap<UnderlyingType, DoubleTimeSeries<?>>();
        for (final UnderlyingType underlyingType : valueGreek.getUnderlyingGreek().getUnderlying().getUnderlyings()) {
          final DoubleTimeSeries<?> timeSeries = UnderlyingTypeToHistoricalTimeSeries.getSeries(historicalDataProvider, _dataSourceName, null, securitySource, underlyingType,
              position.getSecurity());
          final LocalDate[] schedule = _scheduleCalculator.getSchedule(_startDate, now, true);
          final DoubleTimeSeries<?> sampledTS = _samplingCalculator.getSampledTimeSeries(timeSeries, schedule);
          tsReturns.put(underlyingType, _returnCalculator.evaluate(sampledTS));
        }
        dataBundleArray[i++] = new SensitivityAndReturnDataBundle(sensitivity, value, tsReturns);
      } else {
        throw new IllegalArgumentException("Got a value for greek " + valueObj + " that wasn't a Double");
      }
    }
    final DoubleTimeSeries<?> result = PNL_CALCULATOR.evaluate(dataBundleArray);
    final ComputedValue resultValue = new ComputedValue(resultSpecification, result);
    return Collections.singleton(resultValue);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof OptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (final String valueGreekRequirementName : _valueGreekRequirementNames) {
      requirements.add(new ValueRequirement(valueGreekRequirementName, target.getPosition()));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition()), getUniqueIdentifier()));
    return results;
  }

  @Override
  public String getShortName() {
    return "PositionValueGreekSensitivityPnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}

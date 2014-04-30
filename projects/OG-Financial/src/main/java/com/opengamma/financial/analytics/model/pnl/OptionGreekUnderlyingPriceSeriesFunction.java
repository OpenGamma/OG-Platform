/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTimeSeriesProvider;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class OptionGreekUnderlyingPriceSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  //private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  //private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final String VALUE_REQUIREMENT_NAME = ValueRequirementNames.VALUE_DELTA; //TODO this should not be hard-coded
  private final String _resolutionKey;
  //private final String _fieldName; //TODO start using this
  //private final LocalDate _startDate;
  //private final Schedule _scheduleCalculator;
  //private final TimeSeriesSamplingFunction _samplingFunction;
  private final List<Pair<UnderlyingType, String>> _underlyings;

  public OptionGreekUnderlyingPriceSeriesFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
    final Greek greek = AvailableGreeks.getGreekForValueRequirementName(VALUE_REQUIREMENT_NAME);
    _underlyings = new ArrayList<Pair<UnderlyingType, String>>();
    final List<UnderlyingType> types = greek.getUnderlying().getUnderlyings();
    for (final UnderlyingType type : types) {
      _underlyings.add(Pairs.of(type, ValueRequirementNames.PRICE_SERIES + "_" + type));
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    //final Clock snapshotClock = executionContext.getValuationClock();
    //final LocalDate now = ZonedDateTime.now(snapshotClock).getDate();
    //final ValueRequirement desiredValue = desiredValues.iterator().next();
    //final Set<String> samplingPeriodName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    //final Set<String> scheduleCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    //final Set<String> samplingFunctionName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    //final Set<String> returnCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.RETURN_CALCULATOR);
    //final Period samplingPeriod = getSamplingPeriod(samplingPeriodName);
    //final LocalDate startDate = now.minus(samplingPeriod);
    //final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    //final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    //final LocalDate[] schedule = scheduleCalculator.getSchedule(startDate, now, true, false); //REVIEW emcleod should "fromEnd" be hard-coded?
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    //    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(greek.getUnderlying().getUnderlyings(), security), getUniqueId());
    final HistoricalTimeSeries hts = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final DoubleTimeSeries<?> ts = hts.getTimeSeries();
    //    final DoubleTimeSeries<?> resultTS;
    //    if (_scheduleCalculator != null && _samplingFunction != null) {
    //      final LocalDate[] schedule = _scheduleCalculator.getSchedule(_startDate, now, true, false); //REVIEW emcleod should "fromEnd" be hard-coded?
    //      resultTS = _samplingFunction.getSampledTimeSeries(ts, schedule);
    //    } else {
    //      resultTS = ts;
    //    }
    //    result.add(new ComputedValue(valueSpecification, resultTS));
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final UnderlyingTimeSeriesProvider underlyingTimeSeriesProvider = new UnderlyingTimeSeriesProvider(OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context), _resolutionKey,
        context.getSecuritySource());
    return Collections.singleton(underlyingTimeSeriesProvider.getSeriesRequirement(AvailableGreeks.getGreekForValueRequirementName(VALUE_REQUIREMENT_NAME), (FinancialSecurity) target.getSecurity()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createValueProperties().get();
    for (final Pair<UnderlyingType, String> underlying : _underlyings) {
      result.add(new ValueSpecification(underlying.getSecond(), targetSpec, properties));
    }
    return result;
  }

//  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
//    if (samplingPeriodNames == null || samplingPeriodNames.isEmpty() || samplingPeriodNames.size() != 1) {
//      throw new OpenGammaRuntimeException("Missing or non-unique sampling period name: " + samplingPeriodNames);
//    }
//    return Period.parse(samplingPeriodNames.iterator().next());
//  }
//
//  private Schedule getScheduleCalculator(final Set<String> scheduleCalculatorNames) {
//    if (scheduleCalculatorNames == null || scheduleCalculatorNames.isEmpty() || scheduleCalculatorNames.size() != 1) {
//      throw new OpenGammaRuntimeException("Missing or non-unique schedule calculator name: " + scheduleCalculatorNames);
//    }
//    return ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculatorNames.iterator().next());
//  }
//
//  private TimeSeriesSamplingFunction getSamplingFunction(final Set<String> samplingFunctionNames) {
//    if (samplingFunctionNames == null || samplingFunctionNames.isEmpty() || samplingFunctionNames.size() != 1) {
//      throw new OpenGammaRuntimeException("Missing or non-unique sampling function name: " + samplingFunctionNames);
//    }
//    return TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionNames.iterator().next());
//  }

}

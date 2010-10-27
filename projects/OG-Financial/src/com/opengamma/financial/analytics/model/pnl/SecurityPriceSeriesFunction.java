/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.timeseries.Schedule;
import com.opengamma.financial.analytics.timeseries.ScheduleCalculatorFactory;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SecurityPriceSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _dataSourceName;
  private final String _fieldName;
  private final LocalDate _startDate;
  private final Schedule _scheduleCalculator;
  private final TimeSeriesSamplingFunction _samplingFunction;

  public SecurityPriceSeriesFunction(final String dataSourceName, final String fieldName, final String startDate) {
    this(dataSourceName, fieldName, LocalDate.parse(startDate), null, null);
  }

  public SecurityPriceSeriesFunction(final String dataSourceName, final String fieldName, final String startDate, final String scheduleName, final String samplingFunctionName) {
    this(dataSourceName, fieldName, LocalDate.parse(startDate), ScheduleCalculatorFactory.getSchedule(scheduleName), TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName));
  }

  public SecurityPriceSeriesFunction(final String dataSourceName, final String fieldName, final LocalDate startDate) {
    this(dataSourceName, fieldName, startDate, null, null);
  }

  public SecurityPriceSeriesFunction(final String dataSourceName, final String fieldName, final LocalDate startDate, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction) {
    Validate.notNull(dataSourceName, "data source name");
    Validate.notNull(fieldName, "field name");
    Validate.notNull(startDate, "start date");
    _dataSourceName = dataSourceName;
    _fieldName = fieldName;
    _startDate = startDate;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, security), getUniqueIdentifier());
    final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = historicalDataSource.getHistoricalData(security.getIdentifiers(), _dataSourceName, null, _fieldName,
        _startDate, true, now, false);
    if (tsPair == null) {
      throw new NullPointerException("Could not get identifier / price series pair for security " + security);
    }
    final DoubleTimeSeries<?> ts = tsPair.getSecond();
    if (ts == null) {
      throw new NullPointerException("Could not get price series for security " + security);
    }
    final DoubleTimeSeries<?> resultTS;
    if (_scheduleCalculator != null && _samplingFunction != null) {
      final LocalDate[] schedule = _scheduleCalculator.getSchedule(_startDate, now, true); //REVIEW emcleod should "fromEnd" be hard-coded?
      resultTS = _samplingFunction.getSampledTimeSeries(ts, schedule);
    } else {
      resultTS = ts;
    }
    final ComputedValue result = new ComputedValue(valueSpecification, resultTS);
    return Sets.newHashSet(result);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement> emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getSecurity()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SampledSecurityPrice";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.schedule.Schedule;
import com.opengamma.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class SecurityPriceSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _resolutionKey;
  private final String _fieldName;
  private final LocalDate _startDate;
  private final Schedule _scheduleCalculator;
  private final TimeSeriesSamplingFunction _samplingFunction;

  public SecurityPriceSeriesFunction(final String resolutionKey, final String fieldName, final String startDate) {
    this(resolutionKey, fieldName, LocalDate.parse(startDate), null, null);
  }

  public SecurityPriceSeriesFunction(final String resolutionKey, final String fieldName, final String startDate, final String scheduleName, final String samplingFunctionName) {
    this(resolutionKey, fieldName, LocalDate.parse(startDate), ScheduleCalculatorFactory.getScheduleCalculator(scheduleName), TimeSeriesSamplingFunctionFactory.getFunction(samplingFunctionName));
  }

  public SecurityPriceSeriesFunction(final String resolutionKey, final String fieldName, final LocalDate startDate) {
    this(resolutionKey, fieldName, startDate, null, null);
  }

  public SecurityPriceSeriesFunction(final String resolutionKey, final String fieldName, final LocalDate startDate, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction) {
    Validate.notNull(resolutionKey, "resolution key");
    Validate.notNull(fieldName, "field name");
    Validate.notNull(startDate, "start date");
    _resolutionKey = resolutionKey;
    _fieldName = fieldName;
    _startDate = startDate;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    ValueRequirement vr = new ValueRequirement(ValueRequirementNames.PRICE_SERIES, security, createValueProperties().with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode()).get());
    final ValueSpecification valueSpecification = new ValueSpecification(vr, getUniqueId());
    
    final HistoricalTimeSeries tsPair = historicalSource.getHistoricalTimeSeries(_fieldName, security.getExternalIdBundle(), _resolutionKey, _startDate, true, now, true);
    if (tsPair == null) {
      throw new NullPointerException("Could not get identifier / price series pair for security " + security + " for " + _resolutionKey + "/" + _fieldName);
    }
    final DoubleTimeSeries<?> ts = tsPair.getTimeSeries();
    if (ts == null) {
      throw new NullPointerException("Could not get price series for security " + security);
    }
    if (ts.isEmpty()) {
      throw new OpenGammaRuntimeException("Could not get price series for security " + security);
    }
    final DoubleTimeSeries<?> resultTS;
    if (_scheduleCalculator != null && _samplingFunction != null) {
      final LocalDate[] schedule = _scheduleCalculator.getSchedule(_startDate, now, true, false); //REVIEW emcleod should "fromEnd" be hard-coded?
      resultTS = _samplingFunction.getSampledTimeSeries(ts, schedule);
    } else {
      resultTS = ts;
    }
    final ComputedValue result = new ComputedValue(valueSpecification, resultTS);
    return Sets.newHashSet(result);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    try {
      return FinancialSecurityUtils.getCurrency(target.getSecurity()) != null;
    } catch (UnsupportedOperationException e) {
      return false;
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement>emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getSecurity(), createValueProperties().with(ValuePropertyNames.CURRENCY,
        FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode()).get()), getUniqueId()));
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

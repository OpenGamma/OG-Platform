/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTypeToHistoricalTimeSeries;
import com.opengamma.financial.analytics.timeseries.Schedule;
import com.opengamma.financial.analytics.timeseries.ScheduleCalculatorFactory;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class OptionGreekUnderlyingPriceSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _dataSourceName;
  //private final String _fieldName; //TODO start using this
  private final LocalDate _startDate;
  private final Schedule _scheduleCalculator;
  private final TimeSeriesSamplingFunction _samplingFunction;
  private final List<Pair<UnderlyingType, String>> _underlyings;

  public OptionGreekUnderlyingPriceSeriesFunction(final String dataSourceName, final String fieldName, final String startDate, final String valueRequirementName) {
    this(dataSourceName, fieldName, LocalDate.parse(startDate), valueRequirementName, null, null);
  }

  public OptionGreekUnderlyingPriceSeriesFunction(final String dataSourceName, final String fieldName, final String startDate, final String valueRequirementName, final String scheduleName,
      final String samplingFunctionName) {
    this(dataSourceName, fieldName, LocalDate.parse(startDate), valueRequirementName, ScheduleCalculatorFactory.getSchedule(scheduleName), TimeSeriesSamplingFunctionFactory
        .getFunction(samplingFunctionName));
  }

  public OptionGreekUnderlyingPriceSeriesFunction(final String dataSourceName, final String fieldName, final LocalDate startDate, final String valueRequirementName,
      final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction) {
    Validate.notNull(dataSourceName, "data source name");
    Validate.notNull(fieldName, "field name");
    Validate.notNull(startDate, "start date");
    Validate.notNull(valueRequirementName, "value requirement name");
    _dataSourceName = dataSourceName;
    _startDate = startDate;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    final Greek greek = AvailableGreeks.getGreekForValueRequirementName(valueRequirementName);
    _underlyings = new ArrayList<Pair<UnderlyingType, String>>();
    final List<UnderlyingType> types = greek.getUnderlying().getUnderlyings();
    for (final UnderlyingType type : types) {
      _underlyings.add(Pair.of(type, ValueRequirementNames.PRICE_SERIES + "_" + type));
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Security security = target.getSecurity();
    final Clock snapshotClock = executionContext.getSnapshotClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalDataSource historicalDataSource = OpenGammaExecutionContext.getHistoricalDataSource(executionContext);
    final SecuritySource securitySource = executionContext.getSecuritySource();
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    for (final Pair<UnderlyingType, String> underlying : _underlyings) {
      final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(underlying.getSecond(), security), getUniqueIdentifier());
      final DoubleTimeSeries<?> ts = UnderlyingTypeToHistoricalTimeSeries.getSeries(historicalDataSource, _dataSourceName, null, securitySource,
          underlying.getFirst(), security);
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
      result.add(new ComputedValue(valueSpecification, resultTS));
    }
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof OptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement> emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
      for (final Pair<UnderlyingType, String> underlying : _underlyings) {
        result.add(new ValueSpecification(new ValueRequirement(underlying.getSecond(), target.getSecurity()), getUniqueIdentifier()));
      }
      return result;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SampledOptionGreekUnderlyingPrice";
  }
}

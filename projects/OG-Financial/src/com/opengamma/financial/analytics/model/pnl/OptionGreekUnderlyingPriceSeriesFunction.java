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

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.analytics.model.riskfactor.option.UnderlyingTimeSeriesProvider;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class OptionGreekUnderlyingPriceSeriesFunction extends AbstractFunction.NonCompiledInvoker {
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
      _underlyings.add(Pair.of(type, ValueRequirementNames.PRICE_SERIES + "_" + type));
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final SecuritySource securitySource = executionContext.getSecuritySource();
    final UnderlyingTimeSeriesProvider underlyingTimeSeriesProvider = new UnderlyingTimeSeriesProvider(historicalSource, _resolutionKey, securitySource);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final Greek greek = AvailableGreeks.getGreekForValueRequirementName(VALUE_REQUIREMENT_NAME);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
//    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(underlying.getSecond(), security), getUniqueId());      
//    final DoubleTimeSeries<?> ts = underlyingTimeSeriesProvider.getSeries(greek, security);
//    if (ts == null) {
//      throw new NullPointerException("Could not get price series for security " + security);
//    }
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
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement>emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
      for (final Pair<UnderlyingType, String> underlying : _underlyings) {
        result.add(new ValueSpecification(new ValueRequirement(underlying.getSecond(), target.getSecurity()), getUniqueId()));
      }
      return result;
    }
    return null;
  }

}

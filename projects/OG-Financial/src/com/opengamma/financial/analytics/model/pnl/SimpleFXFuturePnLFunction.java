/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecurityUtils;
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
import com.opengamma.financial.analytics.model.forex.ForexUtils;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunction;
import com.opengamma.financial.analytics.timeseries.sampling.TimeSeriesSamplingFunctionFactory;
import com.opengamma.financial.schedule.Schedule;
import com.opengamma.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class SimpleFXFuturePnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  private final String _resolutionKey;

  public SimpleFXFuturePnLFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final FXFutureSecurity security = (FXFutureSecurity) position.getSecurity();
    final Currency currency = security.getCurrency();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> samplingPeriodName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    final Set<String> scheduleCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> samplingFunctionName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    final ExternalIdBundle underlyingId = getUnderlyingIdentifier(security);
    final Period samplingPeriod = getSamplingPeriod(samplingPeriodName);
    final LocalDate startDate = now.minus(samplingPeriod);
    final Currency payCurrency = security.getNumerator();    
    final Currency receiveCurrency = security.getDenominator();    
    final HistoricalTimeSeries dbTimeSeries = historicalSource.getHistoricalTimeSeries(HistoricalTimeSeriesFields.LAST_PRICE, underlyingId, _resolutionKey, startDate, true, now, true);
    if (dbTimeSeries == null) {
      throw new OpenGammaRuntimeException("Could not get identifier / price series pair for id " + underlyingId + " for " + _resolutionKey + "/" + HistoricalTimeSeriesFields.LAST_PRICE);
    }
    DoubleTimeSeries<?> ts = dbTimeSeries.getTimeSeries();
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price series for id " + underlyingId);
    }
    if (ts.isEmpty()) {
      throw new OpenGammaRuntimeException("Empty price series for id " + underlyingId);
    }
    if (!ForexUtils.isBaseCurrency(payCurrency, receiveCurrency) && receiveCurrency.equals(security.getCurrency())) {
      ts = ts.reciprocal();
    }
    final Object pvObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, security.getUniqueId()));
    if (pvObject == null) {
      throw new OpenGammaRuntimeException("Present value was null");
    }
    double pv = (Double) pvObject;
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    final LocalDate[] schedule = scheduleCalculator.getSchedule(startDate, now, true, false); //REVIEW emcleod should "fromEnd" be hard-coded?
    DoubleTimeSeries<?> pnlSeries = samplingFunction.getSampledTimeSeries(dbTimeSeries.getTimeSeries(), schedule);
    pnlSeries = DIFFERENCE.evaluate(pnlSeries);
    pnlSeries = pnlSeries.multiply(pv);
    final ValueProperties resultProperties = getResultProperties(desiredValue, currency.getCode());
    final ValueSpecification spec = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position, resultProperties), getUniqueId());
    return Collections.singleton(new ComputedValue(spec, pnlSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    final Position position = target.getPosition();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    return security instanceof FXFutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final Position position = target.getPosition();
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode())
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.PAY_CURVE)
      .withAny(ValuePropertyNames.RECEIVE_CURVE)
      .get();
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position, properties), getUniqueId()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
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
    final Set<String> payCurveName = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveName == null || payCurveName.isEmpty() || payCurveName.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveName = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveName == null || receiveCurveName.isEmpty() || receiveCurveName.size() != 1) {
      return null;
    }
    final Position position = target.getPosition();
    final FutureSecurity future = (FutureSecurity) position.getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties pvProperties = ValueProperties.builder()
      .with(ValuePropertyNames.CURRENCY, future.getCurrency().getCode())
      .with(ValuePropertyNames.PAY_CURVE, payCurveName)
      .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, future.getUniqueId(), pvProperties));
    return requirements;
  }
  
  private ExternalIdBundle getUnderlyingIdentifier(final FXFutureSecurity future) {
    ExternalId bloombergId;
    final Currency payCurrency = future.getNumerator();
    final Currency receiveCurrency = future.getDenominator();
    if (ForexUtils.isBaseCurrency(payCurrency, receiveCurrency)) {
      bloombergId = SecurityUtils.bloombergTickerSecurityId(payCurrency.getCode() + receiveCurrency.getCode() + " Curncy");
    } else {
      bloombergId = SecurityUtils.bloombergTickerSecurityId(receiveCurrency.getCode() + payCurrency.getCode() + " Curncy");
    }
    return ExternalIdBundle.of(bloombergId);
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
  
  private ValueProperties getResultProperties(final ValueRequirement desiredValue, final String currency) {
    return createValueProperties()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
      .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
      .with(ValuePropertyNames.PAY_CURVE, desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE))
      .with(ValuePropertyNames.RECEIVE_CURVE, desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE))
      .get();
  }
}

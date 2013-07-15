/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SimpleFXFuturePnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  // TODO: The resolution key isn't used
  public SimpleFXFuturePnLFunction(final String resolutionKey) {
    Validate.notNull(resolutionKey, "resolution key");
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final FXFutureSecurity security = (FXFutureSecurity) position.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = ZonedDateTime.now(snapshotClock).toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> samplingPeriodName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    final Set<String> scheduleCalculatorName = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> samplingFunctionName = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    final Period samplingPeriod = getSamplingPeriod(samplingPeriodName);
    final LocalDate startDate = now.minus(samplingPeriod);
    final Currency payCurrency = security.getNumerator();
    final Currency receiveCurrency = security.getDenominator();
    final HistoricalTimeSeries dbTimeSeries = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    if (dbTimeSeries == null) {
      throw new OpenGammaRuntimeException("Could not get identifier / price series pair for " + security);
    }
    DoubleTimeSeries<?> ts = dbTimeSeries.getTimeSeries();
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price series for " + security);
    }
    if (ts.isEmpty()) {
      throw new OpenGammaRuntimeException("Empty price series for " + security);
    }
    // TODO: If we know which way up we want the time series, don't request it in "convention order" and then lookup the convention again here, request it in
    // the desired order in getRequirements using a CurrencyPair
    final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(security.getNumerator(), security.getDenominator());
    if (!payCurrency.equals(currencyPair.getBase()) && receiveCurrency.equals(security.getCurrency())) {
      ts = ts.reciprocal();
    }
    final Object pvObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, security.getUniqueId()));
    if (pvObject == null) {
      throw new OpenGammaRuntimeException("Present value was null");
    }
    final double pv = (Double) pvObject;
    final Schedule scheduleCalculator = getScheduleCalculator(scheduleCalculatorName);
    final TimeSeriesSamplingFunction samplingFunction = getSamplingFunction(samplingFunctionName);
    final LocalDate[] schedule = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR); //REVIEW emcleod should "fromEnd" be hard-coded?
    DateDoubleTimeSeries<?> pnlSeries = samplingFunction.getSampledTimeSeries(dbTimeSeries.getTimeSeries(), schedule);
    pnlSeries = DIFFERENCE.evaluate(pnlSeries);
    pnlSeries = pnlSeries.multiply(pv);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(spec, pnlSeries));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Position position = target.getPosition();
    final Security security = position.getSecurity();
    return security instanceof FXFutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Position position = target.getPosition();
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode())
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
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
    final Set<String> payCurveName = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveName == null || payCurveName.isEmpty() || payCurveName.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveName = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveName == null || receiveCurveName.isEmpty() || receiveCurveName.size() != 1) {
      return null;
    }
    final Position position = target.getPosition();
    final FXFutureSecurity future = (FXFutureSecurity) position.getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties pvProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, future.getCurrency().getCode())
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, ComputationTargetType.SECURITY, future.getUniqueId(), pvProperties));
    requirements.add(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(future.getNumerator(), future.getDenominator(),
        DateConstraint.VALUATION_TIME.minus(samplingPeriodName.iterator().next()), true,
        DateConstraint.VALUATION_TIME, true));
    return requirements;
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

}

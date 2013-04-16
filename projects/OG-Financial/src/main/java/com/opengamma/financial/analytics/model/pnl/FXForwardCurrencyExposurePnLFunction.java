/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FXForwardCurrencyExposurePnLFunction extends AbstractFunction {

  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final CurrencyPairs _currencyPairs;

    public Compiled(final CurrencyPairs currencyPairs) {
      _currencyPairs = currencyPairs;
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.POSITION;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getPosition().getSecurity();
      return security instanceof FXForwardSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
      if (currencyPair == null) {
        return null;
      }
      final Currency currencyBase = currencyPair.getBase();
      final ValueProperties properties = createValueProperties()
          .withAny(ValuePropertyNames.PAY_CURVE)
          .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
          .withAny(ValuePropertyNames.RECEIVE_CURVE)
          .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
          .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
          .withAny(ValuePropertyNames.SAMPLING_PERIOD)
          .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
          .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
          .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE)
          .get();
      final ComputationTargetSpecification targetSpec = target.toSpecification();
      return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
      if (payCurveNames == null || payCurveNames.size() != 1) {
        return null;
      }
      final Set<String> payCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
      if (payCurveCalculationConfigs == null || payCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
      if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
        return null;
      }
      final Set<String> receiveCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
      if (receiveCurveCalculationConfigs == null || receiveCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
      if (samplingPeriods == null || samplingPeriods.size() != 1) {
        return null;
      }
      final Set<String> scheduleCalculatorSet = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
      if (scheduleCalculatorSet == null || scheduleCalculatorSet.size() != 1) {
        return null;
      }
      final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
      final ValueRequirement fxCurrencyExposureRequirement = new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, ComputationTargetSpecification.of(target.getPosition().getSecurity()),
          ValueProperties.builder()
              .with(ValuePropertyNames.PAY_CURVE, payCurveNames.iterator().next())
              .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigs.iterator().next())
              .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveNames.iterator().next())
              .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigs.iterator().next()).get());
      final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
      final ValueRequirement fxSpotRequirement = ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(payCurrency, receiveCurrency);
      return ImmutableSet.of(fxCurrencyExposureRequirement, fxSpotRequirement);
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final Position position = target.getPosition();
      final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      final String samplingPeriod = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
      final String scheduleCalculator = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
      final String samplingFunction = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
      final FXForwardSecurity security = (FXForwardSecurity) position.getSecurity();
      final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
      final DoubleTimeSeries<?> timeSeries = (DoubleTimeSeries<?>) inputs.getValue(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
      final Currency payCurrency = security.getPayCurrency();
      final Currency receiveCurrency = security.getReceiveCurrency();
      if (timeSeries == null) {
        throw new OpenGammaRuntimeException("Could not get spot FX series for " + payCurrency + " / " + receiveCurrency);
      }
      final LocalDate startDate = now.toLocalDate().minus(Period.parse(samplingPeriod));
      final Schedule schedule = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculator);
      final TimeSeriesSamplingFunction sampling = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunction);
      final CurrencyPairs currencyPairs = OpenGammaExecutionContext.getCurrencyPairsSource(executionContext).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
      final Currency currencyNonBase = currencyPair.getCounter(); // The non-base currency
      final double exposure = mca.getAmount(currencyNonBase);
      DoubleTimeSeries<?> result = getPnLSeries(startDate, now.toLocalDate(), schedule, sampling, timeSeries);
      result = result.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      return Collections.singleton(new ComputedValue(spec, result));
    }

  }

  private DoubleTimeSeries<?> getPnLSeries(final LocalDate startDate, final LocalDate now, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction, final DoubleTimeSeries<?> dbTimeSeries) {
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
    DoubleTimeSeries<?> result = samplingFunction.getSampledTimeSeries(dbTimeSeries, dates);
    result = result.reciprocal(); // Implementation note: to obtain the P/L for one unit of non-base currency expressed in base currency.
    return DIFFERENCE.evaluate(result);
  }
}

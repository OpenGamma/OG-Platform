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
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FXDigitalCallSpreadBlackDeltaPnLFunction extends AbstractFunction {

  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final CurrencyPairs currencyPairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    return new Compiled(currencyPairs);
  }

  /**
   * Compiled form.
   */
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
      return target.getPosition().getSecurity() instanceof FXDigitalOptionSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final Currency currencyBase = _currencyPairs.getCurrencyPair(putCurrency, callCurrency).getBase();
      final ValueProperties properties = createValueProperties()
          .with(ValuePropertyNames.CALCULATION_METHOD, FXDigitalCallSpreadBlackFunction.CALL_SPREAD_BLACK_METHOD)
          .withAny(FXOptionBlackFunction.PUT_CURVE)
          .withAny(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
          .withAny(FXOptionBlackFunction.CALL_CURVE)
          .withAny(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
          .withAny(ValuePropertyNames.SURFACE)
          .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
          .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
          .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
          .withAny(FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE)
          .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
          .withAny(ValuePropertyNames.SAMPLING_PERIOD)
          .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
          .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
          .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE)
          .get();
      return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> putCurveNames = constraints.getValues(FXOptionBlackFunction.PUT_CURVE);
      if (putCurveNames == null || putCurveNames.size() != 1) {
        return null;
      }
      final Set<String> putCurveCalculationConfigs = constraints.getValues(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
      if (putCurveCalculationConfigs == null || putCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> callCurveNames = constraints.getValues(FXOptionBlackFunction.CALL_CURVE);
      if (callCurveNames == null || callCurveNames.size() != 1) {
        return null;
      }
      final Set<String> callCurveCalculationConfigs = constraints.getValues(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
      if (callCurveCalculationConfigs == null || callCurveCalculationConfigs.size() != 1) {
        return null;
      }
      final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
      if (surfaceNames == null || surfaceNames.size() != 1) {
        return null;
      }
      final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
      if (interpolatorNames == null || interpolatorNames.size() != 1) {
        return null;
      }
      final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
      if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
        return null;
      }
      final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
      if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
        return null;
      }
      final Set<String> callSpreads = constraints.getValues(FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE);
      if (callSpreads == null || callSpreads.size() != 1) {
        return null;
      }
      final Set<String> samplingPeriods = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
      if (samplingPeriods == null || samplingPeriods.size() != 1) {
        return null;
      }
      final ValueProperties exposureConstraints = ValueProperties.builder()
          .with(ValuePropertyNames.CALCULATION_METHOD, FXDigitalCallSpreadBlackFunction.CALL_SPREAD_BLACK_METHOD)
          .with(FXOptionBlackFunction.PUT_CURVE, putCurveNames.iterator().next())
          .with(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG, putCurveCalculationConfigs.iterator().next())
          .with(FXOptionBlackFunction.CALL_CURVE, callCurveNames.iterator().next())
          .with(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG, callCurveCalculationConfigs.iterator().next())
          .with(ValuePropertyNames.SURFACE, surfaceNames.iterator().next())
          .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorNames.iterator().next())
          .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorNames.iterator().next())
          .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorNames.iterator().next())
          .with(FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE, callSpreads.iterator().next()).get();
      final ValueRequirement fxCurrencyExposureRequirement = new ValueRequirement(
          ValueRequirementNames.FX_CURRENCY_EXPOSURE, ComputationTargetType.SECURITY, target.getPosition().getSecurity().getUniqueId(), exposureConstraints);
      final FinancialSecurity security = (FXDigitalOptionSecurity) target.getPosition().getSecurity();
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final ValueRequirement fxSpotRequirement = ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(putCurrency, callCurrency);
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
      final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
      final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      final LocalDate startDate = now.toLocalDate().minus(Period.parse(samplingPeriod));
      final Schedule schedule = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculator);
      final TimeSeriesSamplingFunction sampling = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunction);
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
      final Currency currencyNonBase = currencyPair.getCounter(); // The non-base currency
      final double delta = mca.getAmount(currencyNonBase);
      final HistoricalTimeSeries timeSeries = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES);
      DateDoubleTimeSeries<?> result = getPnLSeries(startDate, now.toLocalDate(), schedule, sampling, timeSeries);
      result = result.multiply(position.getQuantity().doubleValue() * delta);
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
      return Collections.singleton(new ComputedValue(spec, result));
    }

  }

  private DateDoubleTimeSeries<?> getPnLSeries(final LocalDate startDate, final LocalDate now, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction, final HistoricalTimeSeries dbTimeSeries) {
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
    LocalDateDoubleTimeSeries result = samplingFunction.getSampledTimeSeries(dbTimeSeries.getTimeSeries(), dates);
    result = result.reciprocal(); // Implementation note: to obtain the P/L for one unit of non-base currency expressed in base currency.
    return DIFFERENCE.evaluate(result);
  }

}

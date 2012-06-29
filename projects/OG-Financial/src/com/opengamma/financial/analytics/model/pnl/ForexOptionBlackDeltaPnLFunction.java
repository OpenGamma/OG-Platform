/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.HolidayDateRemovalFunction;
import com.opengamma.analytics.financial.schedule.Schedule;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunction;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDifferenceOperator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class ForexOptionBlackDeltaPnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HistoricalTimeSeriesSource tsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint(ForexOptionBlackFunction.PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(ForexOptionBlackFunction.CALL_CURVE);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String putCurveConfig = desiredValue.getConstraint(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
    final String callCurveConfig = desiredValue.getConstraint(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final String samplingPeriod = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final String scheduleCalculator = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunction = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final Object ccyExposureObject = inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    if (ccyExposureObject == null) {
      throw new OpenGammaRuntimeException("Could not get currency exposure");
    }
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) ccyExposureObject;
    final LocalDate startDate = now.toLocalDate().minus(Period.parse(samplingPeriod));
    final Schedule schedule = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculator);
    final TimeSeriesSamplingFunction sampling = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunction);
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final Currency currencyNonBase = FXUtils.nonBaseCurrency(putCurrency, callCurrency); // The non-base currency
    final double delta = mca.getAmount(currencyNonBase);
    DoubleTimeSeries<?> result = getPnLSeries(tsSource, startDate, now.toLocalDate(), schedule, sampling, security);
    result = result.multiply(position.getQuantity().doubleValue() * delta);
    final Currency currencyBase = FXUtils.baseCurrency(putCurrency, callCurrency); // The base currency
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
        .with(ForexOptionBlackFunction.PUT_CURVE, putCurveName)
        .with(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG, putCurveConfig)
        .with(ForexOptionBlackFunction.CALL_CURVE, callCurveName)
        .with(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG, callCurveConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunction)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE)
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), resultProperties);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXOptionSecurity || security instanceof FXDigitalOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity security = (FinancialSecurity) target.getPosition().getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final Currency currencyBase = FXUtils.baseCurrency(putCurrency, callCurrency); // The base currency
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
        .withAny(ForexOptionBlackFunction.PUT_CURVE)
        .withAny(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
        .withAny(ForexOptionBlackFunction.CALL_CURVE)
        .withAny(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .with(ValuePropertyNames.CURRENCY, currencyBase.getCode())
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE)
        .get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> putCurveNames = constraints.getValues(ForexOptionBlackFunction.PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveCalculationConfigs = constraints.getValues(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG);
    if (putCurveCalculationConfigs == null || putCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(ForexOptionBlackFunction.CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveCalculationConfigs = constraints.getValues(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG);
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
    final String putCurveName = putCurveNames.iterator().next();
    final String putCurveCalculationConfig = putCurveCalculationConfigs.iterator().next();
    final String callCurveName = callCurveNames.iterator().next();
    final String callCurveCalculationConfig = callCurveCalculationConfigs.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final String interpolatorName = interpolatorNames.iterator().next();
    final String leftExtrapolatorName = leftExtrapolatorNames.iterator().next();
    final String rightExtrapolatorName = rightExtrapolatorNames.iterator().next();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CALCULATION_METHOD, ForexOptionBlackFunction.BLACK_METHOD)
        .with(ForexOptionBlackFunction.PUT_CURVE, putCurveName)
        .with(ForexOptionBlackFunction.PUT_CURVE_CALC_CONFIG, putCurveCalculationConfig)
        .with(ForexOptionBlackFunction.CALL_CURVE, callCurveName)
        .with(ForexOptionBlackFunction.CALL_CURVE_CALC_CONFIG, callCurveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName).get();
    final ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, target.getPosition().getSecurity(), properties);
    return Collections.singleton(requirement);
  }

  private DoubleTimeSeries<?> getPnLSeries(final HistoricalTimeSeriesSource historicalSource, final LocalDate startDate, final LocalDate now, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction, final Security security) {
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
    final ExternalIdBundle id;
    if (security instanceof FXOptionSecurity) {
      id = ExternalIdBundle.of(FXUtils.getSpotIdentifier((FXOptionSecurity) security));
    } else if (security instanceof FXDigitalOptionSecurity) {
      id = ExternalIdBundle.of(FXUtils.getSpotIdentifier((FXDigitalOptionSecurity) security));
    } else {
      throw new OpenGammaRuntimeException("Instrument was not a FX vanilla option or FX digital; should never happen");
    }
    final HistoricalTimeSeries dbTimeSeries = historicalSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, startDate, true, now, true);
    if (dbTimeSeries == null) {
      throw new OpenGammaRuntimeException("Could not get time series for id bundle " + id);
    }
    DoubleTimeSeries<?> result = samplingFunction.getSampledTimeSeries(dbTimeSeries.getTimeSeries(), dates);
    result = result.reciprocal(); // Implementation note: to obtain the P/L for one unit of non-base currency expressed in base currency.
    return DIFFERENCE.evaluate(result);
  }
}

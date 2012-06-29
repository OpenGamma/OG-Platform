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
import com.opengamma.financial.analytics.model.forex.forward.ForexForwardFunction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class ForexForwardCurrencyExposurePnLFunction extends AbstractFunction.NonCompiledInvoker {
  private static final HolidayDateRemovalFunction HOLIDAY_REMOVER = HolidayDateRemovalFunction.getInstance();
  private static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final HistoricalTimeSeriesSource tsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(ForexForwardFunction.PAY_CURVE_CALC_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG);
    final String samplingPeriod = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final String scheduleCalculator = desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunction = desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final Object ccyExposureObject = inputs.getValue(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    if (ccyExposureObject == null) {
      throw new OpenGammaRuntimeException("Could not get currency exposure");
    }
    final FXForwardSecurity security = (FXForwardSecurity) position.getSecurity();
    final MultipleCurrencyAmount mca = (MultipleCurrencyAmount) ccyExposureObject;
    final LocalDate startDate = now.toLocalDate().minus(Period.parse(samplingPeriod));
    final Schedule schedule = ScheduleCalculatorFactory.getScheduleCalculator(scheduleCalculator);
    final TimeSeriesSamplingFunction sampling = TimeSeriesSamplingFunctionFactory.getFunction(samplingFunction);
    final Currency currencyNonBase = FXUtils.nonBaseCurrency(security.getPayCurrency(), security.getReceiveCurrency()); // The non-base currency
    final double exposure = mca.getAmount(currencyNonBase);
    DoubleTimeSeries<?> result = getPnLSeries(tsSource, startDate, now.toLocalDate(), schedule, sampling, security);
    result = result.multiply(position.getQuantity().doubleValue() * exposure); // The P/L time series is in the base currency
    final Currency currencyBase = FXUtils.baseCurrency(security.getPayCurrency(), security.getReceiveCurrency()); // The base currency
    final ValueProperties resultProperties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ForexForwardFunction.PAY_CURVE_CALC_CONFIG, payCurveConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG, receiveCurveConfig)
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
    return security instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final Currency currencyBase = FXUtils.baseCurrency(security.getPayCurrency(), security.getReceiveCurrency()); // The base currency
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ForexForwardFunction.PAY_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG)
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
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationConfigs = constraints.getValues(ForexForwardFunction.PAY_CURVE_CALC_CONFIG);
    if (payCurveCalculationConfigs == null || payCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationConfigs = constraints.getValues(ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG);
    if (receiveCurveCalculationConfigs == null || receiveCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final String payCurveName = payCurveNames.iterator().next();
    final String payCurveCalculationConfig = payCurveCalculationConfigs.iterator().next();
    final String receiveCurveName = receiveCurveNames.iterator().next();
    final String receiveCurveCalculationConfig = receiveCurveCalculationConfigs.iterator().next();
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ForexForwardFunction.PAY_CURVE_CALC_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ForexForwardFunction.RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalculationConfig).get();
    final ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.FX_CURRENCY_EXPOSURE, target.getPosition().getSecurity(), properties);
    return Collections.singleton(requirement);
  }

  private DoubleTimeSeries<?> getPnLSeries(final HistoricalTimeSeriesSource historicalSource, final LocalDate startDate, final LocalDate now, final Schedule scheduleCalculator,
      final TimeSeriesSamplingFunction samplingFunction, final FXForwardSecurity fxForward) {
    final LocalDate[] dates = HOLIDAY_REMOVER.getStrippedSchedule(scheduleCalculator.getSchedule(startDate, now, true, false), WEEKEND_CALENDAR);
    final ExternalIdBundle id = ExternalIdBundle.of(FXUtils.getSpotIdentifier(fxForward));
    final HistoricalTimeSeries dbTimeSeries = historicalSource.getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE, id, null, startDate, true, now, true);
    if (dbTimeSeries == null) {
      throw new OpenGammaRuntimeException("Could not get time series for id " + id);
    }
    DoubleTimeSeries<?> result = samplingFunction.getSampledTimeSeries(dbTimeSeries.getTimeSeries(), dates);
    result = result.reciprocal(); // Implementation note: to obtain the P/L for one unit of non-base currency expressed in base currency.
    return DIFFERENCE.evaluate(result);
  }
}

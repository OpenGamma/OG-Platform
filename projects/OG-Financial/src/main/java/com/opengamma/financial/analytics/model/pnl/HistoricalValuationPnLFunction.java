/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
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
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalValuationFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Calculates a PnL series by performing a full historical valuation over the required period.
 */
public class HistoricalValuationPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueProperties outputConstraints = desiredValue.getConstraints();
    Set<String> desiredCurrencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencies == null || desiredCurrencies.isEmpty()) {
      Collection<Currency> targetCurrencies = FinancialSecurityUtils.getCurrencies(target.getPosition().getSecurity(), context.getSecuritySource());
      // REVIEW jonathan 2013-03-12 -- should we pass through all the currencies and see what it wants to produce?
      desiredCurrencies = ImmutableSet.of(Iterables.get(targetCurrencies, 0).getCode());
    }
    ValueProperties.Builder requirementConstraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.CURRENCY)
        .with(HistoricalValuationFunction.PASSTHROUGH_PREFIX + ValuePropertyNames.CURRENCY, desiredCurrencies)
        .withoutAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
        .withoutAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withoutAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withoutAny(ValuePropertyNames.SAMPLING_PERIOD);
    Set<String> samplingPeriodValues = outputConstraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if ((samplingPeriodValues != null) && !samplingPeriodValues.isEmpty()) {
      requirementConstraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, DateConstraint.VALUATION_TIME.minus(samplingPeriodValues.iterator().next()).toString());
      requirementConstraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, DateConstraint.VALUATION_TIME.toString());
    }
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), requirementConstraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    ValueSpecification tsSpec = Iterables.getOnlyElement(inputs.keySet());
    ValueSpecification valueSpec = getResultSpec(target, tsSpec.getProperties());
    return ImmutableSet.of(valueSpec);
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    ComputedValue valueTsComputedValue = inputs.getComputedValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    LocalDateDoubleTimeSeries valueTs = (LocalDateDoubleTimeSeries) valueTsComputedValue.getValue();
    if (valueTs.isEmpty()) {
      return null;
    }
    int pnlVectorSize = valueTs.size() - 1;
    double[] pnlValues = new double[pnlVectorSize];
    for (int i = 0; i < pnlVectorSize; i++) {
      pnlValues[i] = valueTs.getValueAtIndex(i + 1) - valueTs.getValueAtIndex(i);
    }
    int[] pnlDates = new int[valueTs.size() - 1];
    System.arraycopy(valueTs.timesArrayFast(), 1, pnlDates, 0, pnlDates.length);
    LocalDateDoubleTimeSeries pnlTs = ImmutableLocalDateDoubleTimeSeries.of(pnlDates, pnlValues);
    ValueSpecification valueSpec = getResultSpec(target, valueTsComputedValue.getSpecification().getProperties());
    return ImmutableSet.of(new ComputedValue(valueSpec, pnlTs));
  }

  private ValueSpecification getResultSpec(ComputationTarget target, ValueProperties valueTsProperties) {
    Set<String> currencies = valueTsProperties.getValues(HistoricalValuationFunction.PASSTHROUGH_PREFIX + ValuePropertyNames.CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      throw new OpenGammaRuntimeException("Expected a single currency for historical valuation series but got " + currencies);
    }
    ValueProperties.Builder builder = valueTsProperties.copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .with(ValuePropertyNames.CURRENCY, currencies)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Full")
        .withoutAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .withoutAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, ScheduleCalculatorFactory.DAILY)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, TimeSeriesSamplingFunctionFactory.NO_PADDING);
    final DateConstraint startDate = DateConstraint.parse(valueTsProperties.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY).iterator().next());
    builder.with(ValuePropertyNames.SAMPLING_PERIOD, startDate.periodUntil(DateConstraint.VALUATION_TIME).toString());
    return new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), builder.get());
  }

}

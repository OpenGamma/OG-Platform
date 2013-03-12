/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

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
    ValueProperties outputConstraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.CURRENCY)
        .withoutAny(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS)
        .withoutAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withoutAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .get();
    ValueProperties.Builder builder = outputConstraints.copy();
    adjustStartDate(outputConstraints, builder, 1);
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), builder.get()));
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
    int pnlVectorSize = valueTs.size() - 1;
    double[] pnlValues = new double[pnlVectorSize];
    for (int i = 0; i < pnlVectorSize; i++) {
      pnlValues[i] = valueTs.getValueAt(i + 1) - valueTs.getValueAt(i);
    }
    FastArrayIntDoubleTimeSeries fastTs = (FastArrayIntDoubleTimeSeries) valueTs.getFastSeries();
    int[] pnlDates = new int[fastTs.size() - 1];
    System.arraycopy(fastTs.timesArrayFast(), 1, pnlDates, 0, pnlDates.length);
    FastIntDoubleTimeSeries fastPnlTs = fastTs.newInstanceFast(pnlDates, pnlValues);
    LocalDateDoubleTimeSeries pnlTs = new ArrayLocalDateDoubleTimeSeries(valueTs.getConverter(), fastPnlTs);

    ValueSpecification valueSpec = getResultSpec(target, valueTsComputedValue.getSpecification().getProperties());
    return ImmutableSet.of(new ComputedValue(valueSpec, pnlTs));
  }

  private ValueSpecification getResultSpec(ComputationTarget target, ValueProperties valueTsProperties) {
    Currency currency = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity());
    ValueProperties.Builder builder = valueTsProperties.copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .with(ValuePropertyNames.CURRENCY, currency.getCode())
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, "Full")
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, ScheduleCalculatorFactory.DAILY)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, TimeSeriesSamplingFunctionFactory.NO_PADDING);
    adjustStartDate(valueTsProperties, builder, -1);
    return new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), builder.get());
  }
  
  private void adjustStartDate(ValueProperties baseConstraints, ValueProperties.Builder builder, int expansionDays) {
    Set<String> desiredPeriod = baseConstraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (desiredPeriod != null) {
      Period valuationPeriod = Period.parse(Iterables.getOnlyElement(desiredPeriod));
      valuationPeriod = valuationPeriod.plusDays(expansionDays);
      builder.withoutAny(ValuePropertyNames.SAMPLING_PERIOD).with(ValuePropertyNames.SAMPLING_PERIOD, valuationPeriod.toString());
    }
    Set<String> desiredStartDate = baseConstraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (desiredStartDate != null) {
      LocalDate valuationStartDate = LocalDate.parse(Iterables.getOnlyElement(desiredStartDate));
      valuationStartDate = valuationStartDate.minusDays(expansionDays);
      builder.withoutAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY).with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, valuationStartDate.toString());
    }
  }
  
}

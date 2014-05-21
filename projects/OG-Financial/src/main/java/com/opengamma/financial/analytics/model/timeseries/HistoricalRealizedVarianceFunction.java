/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.timeseries;

import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_FX_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.REALIZED_VARIANCE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_REALIZED_VARIANCE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_VARIANCE_END;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_VARIANCE_START;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_REALIZED_VARIANCE_METHOD;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.volatilityswap.RealizedVolatilityCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.UnorderedCurrencyPair;


/**
 *
 */
public class HistoricalRealizedVarianceFunction extends AbstractFunction.NonCompiledInvoker {
  /** The historical variance calculator */
  private static final RealizedVolatilityCalculator CALCULATOR = new RealizedVolatilityCalculator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints().copy().get();
    final LocalDateDoubleTimeSeries ts = (LocalDateDoubleTimeSeries) inputs.getValue(HISTORICAL_FX_TIME_SERIES);
    final String startDateConstraint = desiredValue.getConstraint(HISTORICAL_VARIANCE_START);
    final String endDateConstraint = desiredValue.getConstraint(HISTORICAL_VARIANCE_END);
    final LocalDate startDate, endDate;
    if (startDateConstraint != null) {
      startDate = LocalDate.parse(desiredValue.getConstraint(HISTORICAL_VARIANCE_START));
    } else {
      startDate = ts.getEarliestTime();
    }
    if (endDateConstraint != null) {
      endDate = LocalDate.parse(desiredValue.getConstraint(HISTORICAL_VARIANCE_END));
    } else {
      endDate = ts.getLatestTime();
    }
    final LocalDateDoubleTimeSeries history = ts.subSeries(startDate, endDate);
    final ValueSpecification spec = new ValueSpecification(REALIZED_VARIANCE, target.toSpecification(), properties);
    if (history.isEmpty()) {
      return Collections.singleton(new ComputedValue(spec, 0.));
    }
    final double variance = CALCULATOR.getRealizedVariance(history.valuesArrayFast());
    return Collections.singleton(new ComputedValue(spec, variance));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_REALIZED_VARIANCE_METHOD, HISTORICAL_REALIZED_VARIANCE)
        .withAny(HISTORICAL_VARIANCE_START)
        .withAny(HISTORICAL_VARIANCE_END)
        .get();
    return Collections.singleton(new ValueSpecification(REALIZED_VARIANCE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> varianceCalculationMethods = constraints.getValues(PROPERTY_REALIZED_VARIANCE_METHOD);
    if (varianceCalculationMethods == null || varianceCalculationMethods.size() != 1) {
      return null;
    }
    final UnorderedCurrencyPair currencyPair = (UnorderedCurrencyPair) target.getValue();
    return Collections.singleton(ConventionBasedFXRateFunction.getHistoricalTimeSeriesRequirement(currencyPair)); //TODO ignoring dates
  }

}


/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.engine.view.cache.MissingMarketDataSentinel;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesAdjustment;

/**
 * Function to source the latest time series data point from a {@link HistoricalTimeSeriesSource} attached to the execution context.
 */
public class HistoricalTimeSeriesLatestValueFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    Object value = timeSeriesSource.getLatestDataPoint(target.getUniqueId());
    if (value == null) {
      value = MissingMarketDataSentinel.getInstance();
    } else {
      final String adjusterString = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
      final HistoricalTimeSeriesAdjustment htsa = HistoricalTimeSeriesAdjustment.parse(adjusterString);
      value = htsa.adjust((Double) value);
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()), value));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    Set<String> values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), desiredValue.getConstraints().copy()
          .withoutAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, "").get()));
    } else if (values.size() > 1) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), desiredValue.getConstraints().copy()
          .withoutAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, values.iterator().next()).get()));
    }
    return Collections.emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.isEmpty()) {
      // Use full results - graph builder will compose correctly against the desired value
      return getResults(context, target);
    } else {
      // Use the substituted result
      return inputs.keySet();
    }
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;

/**
 * Function to source the latest historical time-series data point for a security.
 */
public class HistoricalTimeSeriesLatestSecurityValueFunction extends AbstractFunction.NonCompiledInvoker {
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue latestHtsValue = inputs.getComputedValue(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
    return Collections.singleton(new ComputedValue(getSpecification(target, latestHtsValue.getSpecification()), latestHtsValue.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return getTargetType().equals(target.getType());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final Set<String> dataFieldConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    if (dataFieldConstraints.size() > 1) {
      return null;
    }
    final String dataField = dataFieldConstraints.isEmpty() ? null : Iterables.getOnlyElement(dataFieldConstraints);
    HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(target.getSecurity().getExternalIdBundle(), null, null, null, dataField, null);
    if (resolutionResult == null) {
      return null;
    }
    UniqueId htsId = resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId();
    ValueRequirement valueRequirement = new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, htsId, desiredValue.getConstraints());
    return Collections.singleton(valueRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification spec = getSpecification(target);
    return Collections.singleton(spec);
  }

  private ValueSpecification getSpecification(final ComputationTarget target) {
    final ValueProperties props = createValueProperties().withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY).get();
    return new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.getSecurity(), props), getUniqueId());
  }
  
  private ValueSpecification getSpecification(final ComputationTarget target, final ValueSpecification inputSpec) {
    final ValueProperties props = createValueProperties()
        .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, inputSpec.getProperty(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)).get();
    return new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.getSecurity(), props), getUniqueId());
  }

}

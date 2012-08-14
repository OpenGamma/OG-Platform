/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.position.ManageablePosition;

/**
 * Function to source the latest historical time-series data point for a position.
 */
public class HistoricalTimeSeriesLatestPositionProviderIdValueFunction extends AbstractFunction.NonCompiledInvoker {
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue latestHtsValue = inputs.getComputedValue(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(
        new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()), latestHtsValue.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return getTargetType().equals(target.getType());
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties props = createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY).get();
    return Collections.singleton(new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.getPosition(), props), getUniqueId()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final Set<String> dataFieldConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    if (dataFieldConstraints.size() > 1) {
      return null;
    }
    final String dataField = dataFieldConstraints.isEmpty() ? null : Iterables.getOnlyElement(dataFieldConstraints);
    String providerIdValue = target.getPosition().getAttributes().get(ManageablePosition.meta().providerId().name());
    if (providerIdValue == null) {
      return null;
    }
    ExternalId providerId = ExternalId.parse(providerIdValue);
    HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(ExternalIdBundle.of(providerId), null, null, null, dataField, null);
    if (resolutionResult == null) {
      return null;
    }
    UniqueId htsId = resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId();
    ValueRequirement valueRequirement = new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, htsId, desiredValue.getConstraints());
    return Collections.singleton(valueRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    ValueProperties properties = inputSpec.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), properties);
    return ImmutableSet.of(outputSpec);
  }
  
}

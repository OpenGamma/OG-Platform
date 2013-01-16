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
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), latestHtsValue.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties props = createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), props));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final Set<String> dataFieldConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    final String dataField;
    if ((dataFieldConstraints == null) || dataFieldConstraints.isEmpty()) {
      dataField = null;
    } else if (dataFieldConstraints.size() == 1) {
      dataField = Iterables.getOnlyElement(dataFieldConstraints);
    } else {
      return null;
    }
    final String providerIdValue = target.getPosition().getAttributes().get(ManageablePosition.meta().providerId().name());
    if (providerIdValue == null) {
      return null;
    }
    final ExternalId providerId = ExternalId.parse(providerIdValue);
    final HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(ExternalIdBundle.of(providerId), null, null, null, dataField, null);
    if (resolutionResult == null) {
      return null;
    }
    final UniqueId htsId = resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId();
    final ValueRequirement valueRequirement = new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetType.PRIMITIVE, htsId, desiredValue.getConstraints());
    return Collections.singleton(valueRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification inputSpec = Iterables.getOnlyElement(inputs.keySet());
    final ValueProperties properties = inputSpec.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), properties);
    return ImmutableSet.of(outputSpec);
  }

}

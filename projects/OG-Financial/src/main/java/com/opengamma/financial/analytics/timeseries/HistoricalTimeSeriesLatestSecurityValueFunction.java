/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
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
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), latestHtsValue.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    return super.createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final Set<String> dataFieldConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    final String dataField;
    if ((dataFieldConstraints == null) || dataFieldConstraints.isEmpty()) {
      dataField = null;
    } else {
      dataField = dataFieldConstraints.iterator().next();
    }
    final Set<String> resolutionKeyConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    final String resolutionKey;
    if ((resolutionKeyConstraints == null) || resolutionKeyConstraints.isEmpty()) {
      resolutionKey = null;
    } else {
      resolutionKey = resolutionKeyConstraints.iterator().next();
    }
    final HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(target.getSecurity().getExternalIdBundle(), null, null, null, dataField, resolutionKey);
    if (resolutionResult == null) {
      return null;
    }
    UniqueId htsId = resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId();
    final ValueProperties.Builder constraints = ValueProperties.builder();
    final Set<String> ageLimitConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
    if (ageLimitConstraints != null) {
      if (ageLimitConstraints.isEmpty()) {
        if (desiredValue.getConstraints().isOptional(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY)) {
          constraints.withOptional(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
        } else {
          constraints.withAny(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
        }
      } else {
        constraints.with(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY, ageLimitConstraints);
        if (desiredValue.getConstraints().isOptional(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY)) {
          constraints.withOptional(HistoricalTimeSeriesFunctionUtils.AGE_LIMIT_PROPERTY);
        }
      }
    }
    // Add adjuster / normalisation constraint
    final HistoricalTimeSeriesAdjuster adjuster = resolutionResult.getAdjuster();
    final String adjustment = (adjuster == null) ? "" : adjuster.getAdjustment(resolutionResult.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle()).toString();
    constraints.with(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY, adjustment);
    
    ValueRequirement valueRequirement = new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetType.PRIMITIVE, htsId, constraints.get());
    return Collections.singleton(valueRequirement);
  }

}

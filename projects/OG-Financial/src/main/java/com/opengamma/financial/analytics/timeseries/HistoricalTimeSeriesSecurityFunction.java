/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
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
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;

/**
 * Function to source the latest historical time-series data point for a security.
 */
public class HistoricalTimeSeriesSecurityFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesSecurityFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ComputedValue htsValue = inputs.getComputedValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    if (htsValue == null) {
      s_logger.warn("Cannot get time series for {}", target);
    }
    final ManageableHistoricalTimeSeries mhts = (ManageableHistoricalTimeSeries) htsValue.getValue();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(
        new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), mhts.getTimeSeries()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties props = createValueProperties()
        .withAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.ADJUST_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), props));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final Set<String> dataFieldConstraints = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    if ((dataFieldConstraints != null) && (dataFieldConstraints.size() > 1)) {
      return null;
    }
    final String dataField = ((dataFieldConstraints == null) || dataFieldConstraints.isEmpty()) ? null : Iterables.getOnlyElement(dataFieldConstraints);
    final HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(target.getSecurity().getExternalIdBundle(), null, null, null, dataField, null);
    if (resolutionResult == null) {
      return null;
    }
    final UniqueId htsId = resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId();
    final ValueRequirement valueRequirement = new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, ComputationTargetType.PRIMITIVE, htsId, desiredValue.getConstraints());
    return Collections.singleton(valueRequirement);
  }

}

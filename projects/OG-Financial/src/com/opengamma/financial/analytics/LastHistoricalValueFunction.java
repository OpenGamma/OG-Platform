/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
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
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunction;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;

/**
 *
 */
public class LastHistoricalValueFunction extends AbstractFunction.NonCompiledInvoker {

  private static HashMap<String, String> s_marketDataRequirementNamesMap;

  static {
    s_marketDataRequirementNamesMap = new HashMap<String, String>();
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_VOLUME, "VOLUME");
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_APPLIED_BETA, "APPLIED_BETA");
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_MARKET_CAP, "CUR_MKT_CAP");
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_PRICE, "PX_LAST");
  }

  private final String _requirementName;

  public LastHistoricalValueFunction(final String requirementName) {
    Validate.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(target.getSecurity().getExternalIdBundle(), null, null, null, s_marketDataRequirementNamesMap.get(_requirementName),
        null);
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(), ValueProperties
        .with(HistoricalTimeSeriesFunction.START_DATE_PROPERTY, "-P7D")
        .with(HistoricalTimeSeriesFunction.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunction.YES_VALUE)
        .with(HistoricalTimeSeriesFunction.END_DATE_PROPERTY, "-P1D")
        .with(HistoricalTimeSeriesFunction.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunction.YES_VALUE).get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(_requirementName, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public String getShortName() {
    return "LastHistoricalValue for " + _requirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final HistoricalTimeSeries hts = (HistoricalTimeSeries) inputs.getAllValues().iterator().next();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()), hts.getTimeSeries()
        .getLatestValue()));
  }

}

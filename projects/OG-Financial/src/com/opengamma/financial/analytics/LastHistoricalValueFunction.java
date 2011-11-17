/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 */
public class LastHistoricalValueFunction extends PropertyPreservingFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(LastHistoricalValueFunction.class);
  
  private static HashMap<String, String> s_marketDataRequirementNamesMap;

  @Override
  protected Collection<String> getPreservedProperties() {
    // TODO [PLAT-1356] PositionScalingFunction should propagate everything
    return Arrays.asList(
        ValuePropertyNames.CUBE,
        ValuePropertyNames.CURRENCY,
        ValuePropertyNames.CURVE,
        ValuePropertyNames.CURVE_CURRENCY,
        YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        YieldCurveFunction.PROPERTY_FUNDING_CURVE,
        ValuePropertyNames.CURVE_CALCULATION_METHOD);
  }
  
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  
  static {
    s_marketDataRequirementNamesMap = new HashMap<String, String>();
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_VOLUME, "VOLUME");
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_APPLIED_BETA, "APPLIED_BETA");
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_MARKET_CAP, "CUR_MKT_CAP");
    s_marketDataRequirementNamesMap.put(ValueRequirementNames.DAILY_PRICE, "PX_LAST");   
  }

  @Override
  protected Collection<String> getOptionalPreservedProperties() {
    return Collections.emptySet();
  }

  private final String _requirementName;

  public LastHistoricalValueFunction(final String requirementName) {
    Validate.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.SECURITY;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    //final Position position = target.getPosition();
    //final Security security = position.getSecurity();
    //final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, security.getUniqueId(), getInputConstraint(desiredValue));
    return Collections.emptySet(); //singleton(requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(specification);
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
    final ValueSpecification specification = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    final HistoricalTimeSeriesSource tss = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final LocalDate yesterday = executionContext.getValuationClock().today().minusDays(1);
    final LocalDate weekAgo = executionContext.getValuationClock().today().minusDays(7);
    HistoricalTimeSeries timeSeries = tss.getHistoricalTimeSeries(s_marketDataRequirementNamesMap.get(_requirementName), target.getSecurity().getExternalIdBundle(), 
                                                                  RESOLUTION_KEY, weekAgo, true, yesterday, true);
    if (timeSeries.getTimeSeries() != null && !timeSeries.getTimeSeries().isEmpty()) {
      Double doubleValue = timeSeries.getTimeSeries().getLatestValue();
      ComputedValue computedValue = new ComputedValue(specification, doubleValue);
      return Collections.singleton(computedValue);
    } else {
      s_logger.warn("Couldn't find time series data for " + target + " field=" + _requirementName);
    }
    return Collections.emptySet();
  }
}

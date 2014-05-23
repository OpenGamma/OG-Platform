/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.sensitivities.RawSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.security.RawSecurity;

/**
 * The Standard Equity Model Function simply returns the market value for any cash Equity security.
 */
public class ExternallyProvidedSecurityMarkFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final RawSecurity security = (RawSecurity) target.getSecurity();
    final SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
    final Object latestDataPointObject = inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
    if (latestDataPointObject == null) {
      throw new OpenGammaRuntimeException("Could not get latest data point");
    }
    final Double price = (Double) latestDataPointObject / 100d; // REVIEW: jim 9/11/2012 -- this needs to go into the normalisation map.

    return Collections.<ComputedValue>singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties().with(
        ValuePropertyNames.CURRENCY, securityEntryData.getCurrency().getCode()).get()), price));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return RawSecurityUtils.isExternallyProvidedSensitivitiesSecurity(target.getSecurity());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final RawSecurity security = (RawSecurity) target.getSecurity();
    final SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(securityEntryData.getId().toBundle(), null, "LITHIUM", null, "PX_LAST", null);
    if (timeSeries == null) {
      return null;
    }
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetType.PRIMITIVE,
        timeSeries.getHistoricalTimeSeriesInfo().getUniqueId(), ValueProperties.none()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final RawSecurity security = (RawSecurity) target.getSecurity();
    final SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
    return Collections.<ValueSpecification>singleton(new ValueSpecification(ValueRequirementNames.MARK, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY,
        securityEntryData.getCurrency().getCode()).get()));
  }

  @Override
  public String getShortName() {
    return "ExternallyProvidedSecurityMarkFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.RAW_SECURITY;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.sensitivities.RawSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * The Standard Equity Model Function simply returns the market value for any cash Equity security.
 */
public class ExternallyProvidedSecurityMarkFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSecurityMarkFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    final SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
    final UniqueId uid = UniqueId.of(securityEntryData.getId().getScheme().getName(), securityEntryData.getId().getValue());
    //    final double price = (Double) inputs.getValue(
    //        new ValueRequirement(
    //            MarketDataRequirementNames.MARKET_VALUE,
    //            ComputationTargetType.PRIMITIVE,
    //            uid));
    final HistoricalTimeSeriesSource htsSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final Pair<LocalDate, Double> latestDataPoint = htsSource.getLatestDataPoint("PX_LAST", securityEntryData.getId().toBundle(), null);
    if (latestDataPoint == null) {
      throw new OpenGammaRuntimeException("Couldn't get last Market_Value data point for " + securityEntryData.getId());
    }
    final double price = latestDataPoint.getValue();
    //s_logger.warn("Price for " + securityEntryData.getId() + " was " + price);
    return Collections.<ComputedValue>singleton(
        new ComputedValue(
            new ValueSpecification(
                new ValueRequirement(ValueRequirementNames.MARK, ComputationTargetType.POSITION, target.getPosition().getUniqueId(),
                    ValueProperties.with(ValuePropertyNames.CURRENCY, securityEntryData.getCurrency().getCode()).get()),
                    getUniqueId()),
                    price));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.POSITION) {
      return false;
    }
    return RawSecurityUtils.isExternallyProvidedSensitivitiesSecurity(target.getPosition().getSecurity());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      //      final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
      //      final SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
      //      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      //      UniqueId uid = UniqueId.of(securityEntryData.getId().getScheme().getName(), securityEntryData.getId().getValue());
      //      requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, uid));
      return Collections.emptySet();//requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
      final SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
      final UniqueId uid = UniqueId.of(securityEntryData.getId().getScheme().getName(), securityEntryData.getId().getValue());
      return Collections.<ValueSpecification>singleton(
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARK,
              ComputationTargetType.POSITION,
              target.getPosition().getUniqueId(),
              ValueProperties.with(ValuePropertyNames.CURRENCY,
                  securityEntryData.getCurrency().getCode()).get()),
                  getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "ExternallyProvidedSecurityMarkFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}

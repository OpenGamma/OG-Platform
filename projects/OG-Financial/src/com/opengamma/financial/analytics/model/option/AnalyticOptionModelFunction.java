/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;

import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.analytics.greeks.AvailableGreeks;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 *
 */
public abstract class AnalyticOptionModelFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    final StandardOptionDataBundle data = getDataBundle(executionContext.getSecuritySource(), executionContext.getSnapshotClock(), option, inputs);
    final OptionDefinition definition = getOptionDefinition(option);
    final Set<Greek> requiredGreeks = new HashSet<Greek>();
    for (final ValueRequirement dV : desiredValues) {
      final Greek desiredGreek = AvailableGreeks.getGreekForValueRequirement(dV);
      if (desiredGreek == null) {
        throw new IllegalArgumentException("Told to produce " + dV + " but couldn't be mapped to a Greek.");
      }
      requiredGreeks.add(desiredGreek);
    }
    final GreekResultCollection greeks = getModel().getGreeks(definition, data, requiredGreeks);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final ValueRequirement dV : desiredValues) {
      final Greek greek = AvailableGreeks.getGreekForValueRequirement(dV);
      assert greek != null : "Should have thrown IllegalArgumentException above.";
      final Double greekResult = greeks.get(greek);
      final ComputedValue resultValue = new ComputedValue(getResultSpecification(dV.getValueName(), target, option), greekResult);
      results.add(resultValue);
    }
    return results;
  }

  protected ValueSpecification getResultSpecification(final String valueName, final ComputationTarget target, final OptionSecurity security) {
    // REVIEW 2010-10-28 Andrew -- Do all values produced have a currency? Aren't the derivitive greeks unitless?
    return new ValueSpecification(valueName, target.toSpecification(), createValueProperties().with(ValuePropertyNames.CURRENCY, security.getCurrency().getISOCode()).get());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final OptionSecurity security = (OptionSecurity) target.getSecurity();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    for (final String valueName : AvailableGreeks.getAllGreekNames()) {
      results.add(getResultSpecification(valueName, target, security));
    }
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected ValueRequirement getUnderlyingMarketDataRequirement(final UniqueIdentifier uid) {
    // TODO 2010-10-28 Andrew -- We're assuming the underlying is in the same currency as the PUT/CALL price. Detect if it's different and act accordingly.
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, uid);
  }

  protected ValueRequirement getYieldCurveMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid);
  }

  protected ValueRequirement getCostOfCarryMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.COST_OF_CARRY, ComputationTargetType.SECURITY, uid);
  }

  protected ValueRequirement getVolatilitySurfaceMarketDataRequirement(final OptionSecurity security) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, security.getUniqueId(), ValueProperties.with(ValuePropertyNames.CURRENCY,
        security.getCurrency().getISOCode()).get());
  }

  protected abstract <S extends OptionDefinition, T extends StandardOptionDataBundle> AnalyticOptionModel<S, T> getModel();

  protected abstract OptionDefinition getOptionDefinition(OptionSecurity option);

  protected abstract <S extends StandardOptionDataBundle> S getDataBundle(SecuritySource secMaster, Clock relevantTime, OptionSecurity option, FunctionInputs inputs);
}

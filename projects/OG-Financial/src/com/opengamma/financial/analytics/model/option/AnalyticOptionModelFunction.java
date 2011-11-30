/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 *
 */
public abstract class AnalyticOptionModelFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final EquityOptionSecurity option = (EquityOptionSecurity) target.getSecurity();
    final StandardOptionDataBundle data = getDataBundle(executionContext.getSecuritySource(), executionContext.getValuationClock(), option, inputs);
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
      final ComputedValue resultValue = new ComputedValue(getResultSpecification(dV.getValueName(), target, option, dV.getConstraint(ValuePropertyNames.CURVE)), greekResult);
      results.add(resultValue);
    }
    return results;
  }

  protected ValueSpecification getResultSpecification(final String valueName, final ComputationTarget target, final EquityOptionSecurity security, final String curveName) {
    // REVIEW 2010-10-28 Andrew -- Do all values produced have a currency? Aren't the derivitive greeks unitless?
    final ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode());
    if (curveName != null) {
      properties.with(ValuePropertyNames.CURVE, curveName);
    } else {
      properties.withAny(ValuePropertyNames.CURVE);
    }
    return new ValueSpecification(valueName, target.toSpecification(), properties.get());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final EquityOptionSecurity security = (EquityOptionSecurity) target.getSecurity();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    for (final String valueName : AvailableGreeks.getAllGreekNames()) {
      results.add(getResultSpecification(valueName, target, security, null));
    }
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected ValueRequirement getUnderlyingMarketDataRequirement(final UniqueId uid) {
    // TODO 2010-10-28 Andrew -- We're assuming the underlying is in the same currency as the PUT/CALL price. Detect if it's different and act accordingly.
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, uid);
  }

  protected ValueRequirement getYieldCurveMarketDataRequirement(final UniqueId uid, final String curveName) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
  }

  protected ValueRequirement getCostOfCarryMarketDataRequirement(final UniqueId uid, final String curveName) {
    return new ValueRequirement(ValueRequirementNames.COST_OF_CARRY, ComputationTargetType.SECURITY, uid, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
  }

  protected ValueRequirement getVolatilitySurfaceMarketDataRequirement(final EquityOptionSecurity security, final String curveName) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, security.getUniqueId(), ValueProperties.with(ValuePropertyNames.CURRENCY,
        security.getCurrency().getCode()).with(ValuePropertyNames.CURVE, curveName).get());
  }

  protected abstract <S extends OptionDefinition, T extends StandardOptionDataBundle> AnalyticOptionModel<S, T> getModel();

  protected abstract OptionDefinition getOptionDefinition(EquityOptionSecurity option);

  protected abstract <S extends StandardOptionDataBundle> S getDataBundle(SecuritySource secMaster, Clock relevantTime, EquityOptionSecurity option, FunctionInputs inputs);
}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 *
 * @author emcleod
 */
public abstract class AnalyticOptionModelFunction extends AbstractFunction implements FunctionInvoker {
  private static final Map<String, Greek> AVAILABLE_GREEKS;

  static {
    AVAILABLE_GREEKS = new TreeMap<String, Greek>();
    AVAILABLE_GREEKS.put(ValueRequirementNames.FAIR_VALUE, Greek.FAIR_PRICE);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA, Greek.DELTA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA_BLEED, Greek.DELTA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.STRIKE_DELTA, Greek.STRIKE_DELTA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DRIFTLESS_DELTA, Greek.DRIFTLESS_THETA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA, Greek.GAMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_P, Greek.GAMMA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.STRIKE_GAMMA, Greek.STRIKE_GAMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_BLEED, Greek.GAMMA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_P_BLEED, Greek.GAMMA_P_BLEED);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA, Greek.VEGA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA_P, Greek.VEGA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VEGA, Greek.VARIANCE_VEGA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA_BLEED, Greek.VEGA_BLEED);

    AVAILABLE_GREEKS.put(ValueRequirementNames.THETA, Greek.THETA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.RHO, Greek.RHO);
    AVAILABLE_GREEKS.put(ValueRequirementNames.CARRY_RHO, Greek.CARRY_RHO);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ZETA, Greek.ZETA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.ZETA_BLEED, Greek.ZETA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DZETA_DVOL, Greek.DZETA_DVOL);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ELASTICITY, Greek.ELASTICITY);
    AVAILABLE_GREEKS.put(ValueRequirementNames.PHI, Greek.PHI);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ZOMMA, Greek.ZOMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.ZOMMA_P, Greek.ZOMMA_P);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ULTIMA, Greek.ULTIMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_ULTIMA, Greek.VARIANCE_ULTIMA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.SPEED, Greek.SPEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.SPEED_P, Greek.SPEED_P);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VANNA, Greek.VANNA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VANNA, Greek.VARIANCE_VANNA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DVANNA_DVOL, Greek.DVANNA_DVOL);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VOMMA, Greek.VOMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VOMMA_P, Greek.VOMMA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VOMMA, Greek.VARIANCE_VOMMA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    final StandardOptionDataBundle data = getDataBundle(executionContext.getSnapshotClock(), option, inputs);
    final OptionDefinition definition = getOptionDefinition(option);
    final Set<Greek> requiredGreeks = new HashSet<Greek>();
    for (final ValueRequirement dV : desiredValues) {
      final Greek desiredGreek = AVAILABLE_GREEKS.get(dV.getValueName());
      if (desiredGreek == null) {
        throw new IllegalArgumentException("Told to produce " + dV + " but couldn't be mapped to a Greek.");
      }
      requiredGreeks.add(desiredGreek);
    }
    final GreekResultCollection greeks = getModel().getGreeks(definition, data, requiredGreeks);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final ValueRequirement dV : desiredValues) {
      final Greek greek = AVAILABLE_GREEKS.get(dV.getValueName());
      assert greek != null : "Should have thrown IllegalArgumentException above.";
      final GreekResult<?> greekResult = greeks.get(greek);
      final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(dV.getValueName(), option));
      final ComputedValue resultValue = new ComputedValue(resultSpecification, greekResult.getResult());
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final OptionSecurity security = (OptionSecurity) target.getSecurity();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    for (final String valueName : AVAILABLE_GREEKS.keySet()) {
      results.add(new ValueSpecification(new ValueRequirement(valueName, security)));
    }
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected ValueRequirement getUnderlyingMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, uid);
  }

  protected ValueRequirement getDiscountCurveMarketDataRequirement(final Identifier id) {
    return new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, id);
  }

  protected ValueRequirement getCostOfCarryMarketDataRequirement() {
    // TODO
    return null;
  }

  protected ValueRequirement getVolatilitySurfaceMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, uid);
  }

  protected abstract <S extends OptionDefinition, T extends StandardOptionDataBundle> AnalyticOptionModel<S, T> getModel();

  protected abstract OptionDefinition getOptionDefinition(OptionSecurity option);

  protected abstract StandardOptionDataBundle getDataBundle(Clock relevantTime, OptionSecurity option, FunctionInputs inputs);
}

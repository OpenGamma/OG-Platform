/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.future.definition.FXFutureDataBundle;
import com.opengamma.financial.model.future.definition.FutureDefinition;
import com.opengamma.financial.model.future.pricing.FXFutureAsForwardModel;
import com.opengamma.financial.model.future.pricing.FutureModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 * 
 */
public class FXFutureAsForwardModelFunction extends AbstractFunction.NonCompiledInvoker {

  private final FutureModel<FXFutureDataBundle> _model = new FXFutureAsForwardModel();
  private static final Map<String, Greek> AVAILABLE_GREEKS;

  static {
    AVAILABLE_GREEKS = new TreeMap<String, Greek>();
    AVAILABLE_GREEKS.put(ValueRequirementNames.FAIR_VALUE, Greek.FAIR_PRICE);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA, Greek.DELTA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FXFutureSecurity future = (FXFutureSecurity) target.getSecurity();
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final YieldAndDiscountCurve domesticCurve = null; // getDiscountCurveMarketDataRequirement();
    final YieldAndDiscountCurve foreignCurve = null; // getDiscountCurveMarketDataRequirement();
    final double spot = 0; // getUnderlyingMarketDataRequirement();
    final FXFutureDataBundle data = new FXFutureDataBundle(domesticCurve, foreignCurve, spot, now);
    final FutureDefinition definition = new FutureDefinition(future.getExpiry());
    final Set<Greek> requiredGreeks = new HashSet<Greek>();
    Greek greek;
    for (final ValueRequirement v : desiredValues) {
      greek = AVAILABLE_GREEKS.get(v.getValueName());
      if (greek == null) {
        throw new IllegalArgumentException("Told to calculate " + v + " but could not be mapped to a greek");
      }
      requiredGreeks.add(greek);
    }
    final GreekResultCollection greeks = _model.getGreeks(definition, data, requiredGreeks);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final ValueRequirement v : desiredValues) {
      greek = AVAILABLE_GREEKS.get(v.getValueName());
      assert greek != null : "Should have thrown IllegalArgumentException above.";
      final Double greekResult = greeks.get(greek);
      final ValueSpecification resultSpecification = new ValueSpecification(
          new ValueRequirement(v.getValueName(), ComputationTargetType.SECURITY, future.getUniqueId()),
          getUniqueIdentifier());
      final ComputedValue resultValue = new ComputedValue(resultSpecification, greekResult);
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof FXFutureSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final FXFutureSecurity future = (FXFutureSecurity) target.getSecurity();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      final UniqueIdentifier fx = null;
      requirements.add(getUnderlyingMarketDataRequirement(fx));
      requirements.add(getDiscountCurveMarketDataRequirement(future.getNumerator().getUniqueId()));
      requirements.add(getDiscountCurveMarketDataRequirement(future.getDenominator().getUniqueId()));
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final FXFutureSecurity future = (FXFutureSecurity) target.getSecurity();
      final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
      for (final String name : AVAILABLE_GREEKS.keySet()) {
        results.add(new ValueSpecification(
            new ValueRequirement(name, ComputationTargetType.SECURITY, future.getUniqueId()),
            getUniqueIdentifier()));
      }
      return results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "FXFutureAsForwardModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  protected ValueRequirement getUnderlyingMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, uid);
  }

  protected ValueRequirement getDiscountCurveMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid);
  }
}

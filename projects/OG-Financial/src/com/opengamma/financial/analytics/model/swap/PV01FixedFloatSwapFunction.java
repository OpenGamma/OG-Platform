/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * 
 */
public class PV01FixedFloatSwapFunction extends FixedFloatSwapFunction {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public PV01FixedFloatSwapFunction(final String currency, final String name) {
    super(currency, name);
  }

  //TODO this only works at the moment because we're using the same curve for forward and funding. Don't know how to get around this
  // with the value requirements working as they do now because I can't see how to distinguish curves by name without needing 
  // a load of extra strings in ValueRequirementName
  public PV01FixedFloatSwapFunction(final Currency currency, final String name) {
    super(currency, name);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final Security security, final Swap swap, final YieldCurveBundle bundle) {
    final Map<String, Double> pv01ForCurve = CALCULATOR.getValue(swap, bundle);
    final String name = getName();
    if (pv01ForCurve.size() == 1 && pv01ForCurve.keySet().contains(name)) {
      final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01, security), getUniqueIdentifier());
      return Sets.newHashSet(new ComputedValue(specification, pv01ForCurve.get(name)));
    }
    throw new NullPointerException("Could not get PV01 for curve " + name);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01, target.getSecurity()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PV01FixedFloatSwapFunction";
  }

}

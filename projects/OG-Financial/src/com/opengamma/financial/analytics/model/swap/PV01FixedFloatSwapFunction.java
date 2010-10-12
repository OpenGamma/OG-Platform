/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.HashSet;
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

  public PV01FixedFloatSwapFunction(final String currency, final String curveName, final String valueRequirementName) {
    super(Currency.getInstance(currency), curveName, valueRequirementName, curveName, valueRequirementName);
  }

  public PV01FixedFloatSwapFunction(final String currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    super(Currency.getInstance(currency), forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  public PV01FixedFloatSwapFunction(final Currency currency, final String name, final String valueRequirementName) {
    super(currency, name, valueRequirementName, name, valueRequirementName);
  }

  public PV01FixedFloatSwapFunction(final Currency currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    super(currency, forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final Security security, final Swap<?, ?> swap, final YieldCurveBundle bundle) {
    final Map<String, Double> pv01ForCurve = CALCULATOR.getValue(swap, bundle);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    if (!(pv01ForCurve.containsKey(getForwardCurveName()) && pv01ForCurve.containsKey(getFundingCurveName()))) {
      throw new NullPointerException("Could not get PV01 for " + getForwardCurveName() + " and " + getFundingCurveName());
    }
    for (final Map.Entry<String, Double> entry : pv01ForCurve.entrySet()) {
      final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01 + "_" + entry.getKey(), security), getUniqueIdentifier());
      result.add(new ComputedValue(specification, entry.getValue()));
    }
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      if (getForwardCurveName().equals(getFundingCurveName())) {
        return Sets.newHashSet(new ValueRequirement(getForwardValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()));
      }
      return Sets.newHashSet(new ValueRequirement(getForwardValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()),
          new ValueRequirement(getFundingValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      if (getForwardCurveName().equals(getFundingCurveName())) {
        return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01 + "_" + getForwardCurveName(), target.getSecurity()), getUniqueIdentifier()));
      }
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01 + "_" + getForwardCurveName(), target.getSecurity()), getUniqueIdentifier()),
          new ValueSpecification(new ValueRequirement(ValueRequirementNames.PV01 + "_" + getFundingCurveName(), target.getSecurity()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PV01FixedFloatSwapFunction";
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * 
 */
public class PresentValueFixedFloatSwapFunction extends FixedFloatSwapFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public PresentValueFixedFloatSwapFunction(final String currency, final String curveName, final String valueRequirementName) {
    super(Currency.getInstance(currency), curveName, valueRequirementName, curveName, valueRequirementName);
  }

  public PresentValueFixedFloatSwapFunction(final String currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    super(Currency.getInstance(currency), forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  public PresentValueFixedFloatSwapFunction(final Currency currency, final String name, final String valueRequirementName) {
    super(currency, name, valueRequirementName, name, valueRequirementName);
  }

  public PresentValueFixedFloatSwapFunction(final Currency currency, final String forwardCurveName, final String forwardValueRequirementName, final String fundingCurveName,
      final String fundingValueRequirementName) {
    super(currency, forwardCurveName, forwardValueRequirementName, fundingCurveName, fundingValueRequirementName);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final Security security, final Swap<?, ?> swap, final YieldCurveBundle bundle) {
    final Double presentValue = CALCULATOR.visit(swap, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, security), createValueProperties().with(ValuePropertyNames.CURRENCY,
        getCurrencyForTarget(security).getISOCode()).get());
    return Sets.newHashSet(new ComputedValue(specification, presentValue));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      if (getForwardCurveName().equals(getFundingCurveName())) {
        return Sets.newHashSet(new ValueRequirement(getForwardValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId()));
      }
      return Sets.newHashSet(new ValueRequirement(getForwardValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId()), new ValueRequirement(
          getFundingValueRequirementName(), ComputationTargetType.PRIMITIVE, getCurrencyForTarget(target).getUniqueId()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, target.getSecurity()), createValueProperties().with(ValuePropertyNames.CURRENCY,
          getCurrencyForTarget(target).getISOCode()).get()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PresentValueFixedFloatSwapFunction";
  }

}

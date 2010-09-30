/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

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
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * 
 */
public class PresentValueFixedFloatSwapFunction extends FixedFloatSwapFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public PresentValueFixedFloatSwapFunction(final String currency, final String name) {
    super(currency, name);
  }

  public PresentValueFixedFloatSwapFunction(final Currency currency, final String name) {
    super(currency, name);
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final Security security, final Swap swap, final YieldCurveBundle bundle) {
    final Double presentValue = CALCULATOR.getValue(swap, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, security), getUniqueIdentifier());
    return Sets.newHashSet(new ComputedValue(specification, presentValue));
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
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, target.getSecurity()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "PresentValueFixedFloatSwapFunction";
  }
}

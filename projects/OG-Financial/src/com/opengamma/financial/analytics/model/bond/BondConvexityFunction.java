/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.interestrate.bond.BondConvexityCalculator;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondConvexityFunction extends BondFunction {
  private static final BondConvexityCalculator CONVEXITY_CALCULATOR = BondConvexityCalculator.getInstance();
  
  public BondConvexityFunction() {
    super(ValueRequirementNames.DIRTY_PRICE);
  }
  
  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final Currency currency, final Security security, final BondDefinition definition, final Object value,
      final LocalDate now, final String yieldCurveName) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.CONVEXITY, security), getUniqueId());
    double dirtyPrice = (Double) value;
    double convexity = CONVEXITY_CALCULATOR.calculate(definition.toDerivative(now, yieldCurveName), dirtyPrice);
    return Sets.newHashSet(new ComputedValue(specification, convexity));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.CONVEXITY, target.getSecurity()), getUniqueId()));
    }
    return Sets.newHashSet();
  }

  @Override
  public String getShortName() {
    return "BondConvexityFunction";
  }
}

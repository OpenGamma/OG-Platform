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
import com.opengamma.financial.interestrate.bond.MacaulayDurationCalculator;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondMacaulayDurationFunction extends BondFunction {
  private static final MacaulayDurationCalculator DURATION_CALCULATOR = MacaulayDurationCalculator.getInstance();
  
  public BondMacaulayDurationFunction() {
    super(ValueRequirementNames.DIRTY_PRICE);
  }
  
  @Override
  protected Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final Currency currency, final Security security, final BondDefinition definition, final Object value,
      final LocalDate now, final String yieldCurveName) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.MACAULAY_DURATION, security), getUniqueId());
    double dirtyPrice = (Double) value;
    double duration = DURATION_CALCULATOR.calculate(definition.toDerivative(now, yieldCurveName), dirtyPrice);
    return Sets.newHashSet(new ComputedValue(specification, duration));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.MACAULAY_DURATION, target.getSecurity()), getUniqueId()));
    }
    return Sets.newHashSet();
  }

  @Override
  public String getShortName() {
    return "BondMacaulayDurationFunction";
  }
}

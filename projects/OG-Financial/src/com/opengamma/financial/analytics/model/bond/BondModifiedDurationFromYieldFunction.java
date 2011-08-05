/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.interestrate.bond.calculator.ModifiedDurationFromYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondModifiedDurationFromYieldFunction extends BondFromYieldFunction {
  private static final ModifiedDurationFromYieldCalculator CALCULATOR = ModifiedDurationFromYieldCalculator.getInstance();

  public BondModifiedDurationFromYieldFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.MODIFIED_DURATION, FROM_YIELD_METHOD);
  }

  public BondModifiedDurationFromYieldFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.MODIFIED_DURATION, FROM_YIELD_METHOD);
  }

  @Override
  protected Set<ComputedValue> calculate(final BondFixedSecurity bond, final Double data, final ComputationTarget target) {
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), CALCULATOR.visit(bond, data)));
  }
}

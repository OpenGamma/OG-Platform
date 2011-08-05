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
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.calculator.ModifiedDurationFromCurvesCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondModifiedDurationFromCurvesFunction extends BondFromCurvesFunction {
  private static final ModifiedDurationFromCurvesCalculator CALCULATOR = ModifiedDurationFromCurvesCalculator.getInstance();

  public BondModifiedDurationFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.MODIFIED_DURATION, FROM_CURVES_METHOD);
  }

  public BondModifiedDurationFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.MODIFIED_DURATION, FROM_CURVES_METHOD);
  }

  @Override
  protected Set<ComputedValue> calculate(final BondFixedSecurity bond, final YieldCurveBundle data, final ComputationTarget target) {
    final Double result = CALCULATOR.visit(bond, data);
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), result));
  }

}

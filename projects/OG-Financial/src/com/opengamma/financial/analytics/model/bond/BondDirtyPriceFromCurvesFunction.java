/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.calculator.DirtyPriceFromCurvesCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondDirtyPriceFromCurvesFunction extends BondFromCurvesFunction {
  private static final DirtyPriceFromCurvesCalculator CALCULATOR = DirtyPriceFromCurvesCalculator.getInstance();

  public BondDirtyPriceFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName);
  }

  public BondDirtyPriceFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName);
  }

  @Override
  protected Set<ComputedValue> calculate(final BondFixedSecurity bond, final YieldCurveBundle data, final ComputationTarget target, final FunctionInputs inputs) {
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), 100 * CALCULATOR.visit(bond, data)));
  }

  @Override
  protected ValueSpecification getResultSpec(final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CALCULATION_METHOD, FROM_CURVES_METHOD).get();
    return new ValueSpecification(ValueRequirementNames.DIRTY_PRICE, target.toSpecification(), properties);
  }
}

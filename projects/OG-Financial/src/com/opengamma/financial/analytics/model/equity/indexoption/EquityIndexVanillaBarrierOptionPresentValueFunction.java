/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Computes the PV of a European Barrier Option by breaking it into a linear and a binary vanilla option,
 * and then modelling the binary as a call spread with provided overhedge and smoothing
 */
public class EquityIndexVanillaBarrierOptionPresentValueFunction extends EquityIndexVanillaBarrierOptionFunction {
  private static final EquityIndexOptionPresentValueCalculator s_calculator = EquityIndexOptionPresentValueCalculator.getInstance();

  public EquityIndexVanillaBarrierOptionPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Object computeValues(Set<EquityIndexOption> vanillaOptions, EquityOptionDataBundle market) {
    double pv = 0.0;
    for (EquityIndexOption derivative : vanillaOptions) {
      pv += s_calculator.visitEquityIndexOption(derivative, market);
    }
    return pv;
  }

}

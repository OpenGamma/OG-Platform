/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionBlackPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Computes the PV of a European Barrier Option by breaking it into a linear and a binary vanilla option,
 * and then modelling the binary as a call spread with provided overhedge and smoothing.
 */
public class EquityVanillaBarrierOptionPresentValueFunction extends EquityVanillaBarrierOptionBlackFunction {
  /** The present value calculator */
  private static final EquityOptionBlackPresentValueCalculator s_calculator = EquityOptionBlackPresentValueCalculator.getInstance();

  /**
   * Default constructor
   */
  public EquityVanillaBarrierOptionPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    double pv = 0.0;
    for (final EquityIndexOption derivative : vanillaOptions) {
      pv += s_calculator.visitEquityIndexOption(derivative, market);
    }
    return Collections.singleton(new ComputedValue(resultSpec, pv));
  }
}

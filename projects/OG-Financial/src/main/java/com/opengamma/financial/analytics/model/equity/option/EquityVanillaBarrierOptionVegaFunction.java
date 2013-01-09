/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the vega (first order sensitivity of the price to the implied volatility) for a vanilla equity barrier option
 * using the Black formula.
 */
public class EquityVanillaBarrierOptionVegaFunction extends EquityVanillaBarrierOptionBlackFunction {

  /**
   * Default constructor
   */
  public EquityVanillaBarrierOptionVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    double sum = 0.0;
    for (final EquityIndexOption derivative : vanillaOptions) {
      sum += model.vega(derivative, market);
    }
    return Collections.singleton(new ComputedValue(resultSpec, sum));
  }

}

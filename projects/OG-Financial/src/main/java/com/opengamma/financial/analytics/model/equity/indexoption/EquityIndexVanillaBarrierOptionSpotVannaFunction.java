/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the vanna (the derivative of the price with respect to spot and implied volatility) of a vanilla equity barrier option
 * using the Black formula.
 */
public class EquityIndexVanillaBarrierOptionSpotVannaFunction extends EquityIndexVanillaBarrierOptionFunction {

  /**
   * Default constructor
   */
  public EquityIndexVanillaBarrierOptionSpotVannaFunction() {
    super(ValueRequirementNames.VALUE_VANNA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ValueSpecification resultSpec) {
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    double sum = 0.0;
    for (final EquityIndexOption derivative : vanillaOptions) {
      sum += model.vannaWrtSpot(derivative, market);
    }
    return Collections.singleton(new ComputedValue(resultSpec, sum));
  }

}

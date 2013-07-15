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
 * The <b>forward</b> value of the index, i.e. the fair strike of a forward agreement paying the index value at maturity,
 * as seen from the selected market data. <p>
 */
public class EquityVanillaBarrierOptionForwardValueFunction extends EquityVanillaBarrierOptionBlackFunction {

  /**
   * Default constructor
   */
  public EquityVanillaBarrierOptionForwardValueFunction() {
    super(ValueRequirementNames.FORWARD);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    final EquityIndexOption firstDerivative = vanillaOptions.iterator().next();
    return Collections.singleton(new ComputedValue(resultSpec, model.forwardIndexValue(firstDerivative, market))); // All derivatives in the set share their forward
  }

  //TODO this function return values unnecessary properties - the surface name, currency, interpolator and calculation method, which are used
  // to construct the market data bundle.
}

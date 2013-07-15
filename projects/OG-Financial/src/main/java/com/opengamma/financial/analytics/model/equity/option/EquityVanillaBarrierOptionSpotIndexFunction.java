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
 * Produces the current value of the underlying index, according to the market data
 */
public class EquityVanillaBarrierOptionSpotIndexFunction extends EquityVanillaBarrierOptionBlackFunction {

  /**
   * Default constructor
   */
  public EquityVanillaBarrierOptionSpotIndexFunction() {
    super(ValueRequirementNames.SPOT);
  }

  @Override
  protected Set<ComputedValue> computeValues(final Set<EquityIndexOption> vanillaOptions, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return Collections.singleton(new ComputedValue(resultSpec, model.spotIndexValue(market)));
  }

  //TODO this function return values unnecessary properties - the surface name, currency, interpolator and calculation method, which are used
  // to construct the market data bundle.

}

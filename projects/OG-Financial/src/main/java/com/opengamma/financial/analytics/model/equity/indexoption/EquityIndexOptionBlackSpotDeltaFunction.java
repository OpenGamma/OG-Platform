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
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;

/**
 * Calculates the spot value delta of an equity index option using the Black formula.
 */
public class EquityIndexOptionBlackSpotDeltaFunction extends EquityIndexOptionFunction {

  /**
   * Default constructor
   */
  public EquityIndexOptionBlackSpotDeltaFunction() {
    super(ValueRequirementNames.VALUE_DELTA, FXOptionBlackFunction.BLACK_METHOD);
  }

  @Override
  protected Set<ComputedValue> computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ValueSpecification resultSpec) {
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return Collections.singleton(new ComputedValue(resultSpec, model.deltaWrtSpot(derivative, market)));
  }

}

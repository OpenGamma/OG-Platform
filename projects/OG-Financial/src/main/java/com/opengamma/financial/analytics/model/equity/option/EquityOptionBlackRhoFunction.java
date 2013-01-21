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
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.option.EquityOptionBlackMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the rho of an equity index or equity option using the Black formula.
 */
public class EquityOptionBlackRhoFunction extends EquityOptionBlackFunction {

  /**
   * Default constructor
   */
  public EquityOptionBlackRhoFunction() {
    super(ValueRequirementNames.VALUE_RHO);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    if (derivative instanceof EquityIndexOption) {
      final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
      return Collections.singleton(new ComputedValue(resultSpec, model.rho((EquityIndexOption) derivative, market)));
    }
    final EquityOptionBlackMethod model = EquityOptionBlackMethod.getInstance();
    return Collections.singleton(new ComputedValue(resultSpec, model.rho((EquityOption) derivative, market)));
  }

}

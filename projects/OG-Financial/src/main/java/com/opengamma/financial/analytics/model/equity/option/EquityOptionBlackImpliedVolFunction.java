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
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the Black implied volatility of an equity index option.
 */
public class EquityOptionBlackImpliedVolFunction extends EquityOptionBlackFunction {

  /**
   * @param valueRequirementName
   */
  public EquityOptionBlackImpliedVolFunction() {
    super(ValueRequirementNames.IMPLIED_VOLATILITY);
  }

  @Override
  protected Set<ComputedValue> computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return Collections.singleton(new ComputedValue(resultSpec, model.impliedVol(derivative, market)));
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return super.createValueProperties(target)
        .withoutAny(ValuePropertyNames.CURRENCY);
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    return super.createValueProperties(target, desiredValue)
        .withoutAny(ValuePropertyNames.CURRENCY);
  }
}

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
 * Calculates the vega of an equity index option using the Black method.
 */
public class EquityOptionBlackVegaFunction extends EquityOptionBlackFunction {
  /** The Black calculator */
  private static final EquityIndexOptionBlackMethod INDEX_MODEL = EquityIndexOptionBlackMethod.getInstance();
  /** The Black calculator */
  private static final EquityOptionBlackMethod EQUITY_MODEL = EquityOptionBlackMethod.getInstance();

  /**
   * Default constructor
   */
  public EquityOptionBlackVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    if (derivative instanceof EquityIndexOption) {
      return Collections.singleton(new ComputedValue(resultSpec, INDEX_MODEL.vega((EquityIndexOption) derivative, market)));
    }
    return Collections.singleton(new ComputedValue(resultSpec, EQUITY_MODEL.vega((EquityOption) derivative, market)));
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class EquityVarianceSwapStaticReplicationPresentValueFunction extends EquityVarianceSwapStaticReplicationFunction {
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();

  public EquityVarianceSwapStaticReplicationPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> computeValues(final ValueSpecification resultSpec, final FunctionInputs inputs, final VarianceSwap derivative, final StaticReplicationDataBundle market) {
    return Collections.singleton(new ComputedValue(resultSpec, PRICER.presentValue(derivative, market)));
  }

}

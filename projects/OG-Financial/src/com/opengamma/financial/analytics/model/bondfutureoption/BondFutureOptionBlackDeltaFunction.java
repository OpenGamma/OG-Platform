/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackDeltaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class BondFutureOptionBlackDeltaFunction extends BondFutureOptionBlackFunction {
  private static final PresentValueBlackDeltaCalculator CALCULATOR = PresentValueBlackDeltaCalculator.getInstance();

  public BondFutureOptionBlackDeltaFunction() {
    super(ValueRequirementNames.VALUE_DELTA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative bondFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final Double gamma = CALCULATOR.visit(bondFutureOption, data);
    return Collections.singleton(new ComputedValue(spec, gamma));
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackDeltaForTransactionCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function computes the {@link ValueRequirementNames#POSITION_DELTA}, first order derivative of {@link Position} price with respect to the futures price,
 * for interest rate future options in the Black world. <p>
 */
public class InterestRateFutureOptionBlackPositionDeltaFunction extends InterestRateFutureOptionBlackFunction {

  /** The calculator to compute the delta value */
  private static final PresentValueBlackDeltaForTransactionCalculator CALCULATOR = PresentValueBlackDeltaForTransactionCalculator.getInstance();
  
  public InterestRateFutureOptionBlackPositionDeltaFunction() {
    super(ValueRequirementNames.POSITION_DELTA);
  }
  
  @Override
  protected Set<ComputedValue> getResult(InstrumentDerivative irFutureOption, YieldCurveWithBlackCubeBundle curveBundle, ValueSpecification spec) {
    final double delta = irFutureOption.accept(CALCULATOR, curveBundle);
    return Collections.singleton(new ComputedValue(spec, delta));
  }

}

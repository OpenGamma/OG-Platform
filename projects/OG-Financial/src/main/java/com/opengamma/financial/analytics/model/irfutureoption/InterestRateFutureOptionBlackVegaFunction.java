/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackTheoreticalVegaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function computes the {@link ValueRequirementNames#VEGA}, first order derivative of {@link Security} price with respect to the implied vol,
 * for interest rate future options in the Black world.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public class InterestRateFutureOptionBlackVegaFunction extends InterestRateFutureOptionBlackFunction {
  /** The calculator to compute the vega */
  private static final PresentValueBlackTheoreticalVegaCalculator CALCULATOR = PresentValueBlackTheoreticalVegaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VEGA}
   */
  public InterestRateFutureOptionBlackVegaFunction() {
    super(ValueRequirementNames.VEGA, false);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOptionTransaction, final YieldCurveWithBlackCubeBundle curveBundle, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final double vega = irFutureOptionTransaction.accept(CALCULATOR, curveBundle);
    return Collections.singleton(new ComputedValue(spec, vega));
  }
}

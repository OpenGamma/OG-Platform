/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackDeltaForSecurityCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingDeltaIRFutureOptionFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Function computes the {@link ValueRequirementNames#DELTA}, first order derivative of {@link Security} price with respect to the futures price,
 * for interest rate future options in the Black world.
 * @deprecated Use {@link BlackDiscountingDeltaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackDeltaFunction extends InterestRateFutureOptionBlackFunction {

  /** The calculator to compute the delta value */
  private static final PresentValueBlackDeltaForSecurityCalculator CALCULATOR = PresentValueBlackDeltaForSecurityCalculator.getInstance();

  public InterestRateFutureOptionBlackDeltaFunction() {
    super(ValueRequirementNames.DELTA);
  }
  
  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOptionTransaction, final YieldCurveWithBlackCubeBundle curveBundle, final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {
    ArgumentChecker.isTrue(irFutureOptionTransaction instanceof InterestRateFutureOptionMarginTransaction,
        "InterestRateFutureOptionMarginTransaction expected. " + irFutureOptionTransaction.getClass().toString() + " found.");
    final InstrumentDerivative irFutureOptionSecurity = ((InterestRateFutureOptionMarginTransaction) irFutureOptionTransaction).getUnderlyingOption();
    final double delta = irFutureOptionSecurity.accept(CALCULATOR, curveBundle);
    return Collections.singleton(new ComputedValue(spec, delta));
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;

import com.opengamma.analytics.financial.provider.calculator.issuer.ModifiedDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * Calculates the modified duration of a bond from yield curves.
 */
public class BondModifiedDurationFromCurvesFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#MODIFIED_DURATION} and
   * the calculator to {@link ModifiedDurationFromCurvesCalculator}.
   */
  public BondModifiedDurationFromCurvesFunction() {
    super(MODIFIED_DURATION, ModifiedDurationFromCurvesCalculator.getInstance());
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof BondSecurity;
  }

}

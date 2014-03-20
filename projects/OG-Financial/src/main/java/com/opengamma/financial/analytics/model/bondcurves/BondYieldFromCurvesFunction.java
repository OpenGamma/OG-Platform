/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.YTM;

import com.opengamma.analytics.financial.provider.calculator.issuer.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the yield of a bond from yield curves.
 */
public class BondYieldFromCurvesFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Double> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#YTM} and
   * the calculator to {@link YieldFromCurvesCalculator}.
   */
  public BondYieldFromCurvesFunction() {
    super(YTM, YieldFromCurvesCalculator.getInstance());
  }

}

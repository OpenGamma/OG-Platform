/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.AccruedInterestFromCurvesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the accrued interest of a bond using yield curves for pricing.
 * @deprecated The parent of this class is deprecated.
 */
@Deprecated
public class BondAccruedInterestFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> getCalculator() {
    return AccruedInterestFromCurvesCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.ACCRUED_INTEREST;
  }
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCurvesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Calculates the bond yield from yield curves.
 * @deprecated The parent class of this function is deprecated.
 */
@Deprecated
public class BondYieldFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> getCalculator() {
    return YieldFromCurvesCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.YTM;
  }
}

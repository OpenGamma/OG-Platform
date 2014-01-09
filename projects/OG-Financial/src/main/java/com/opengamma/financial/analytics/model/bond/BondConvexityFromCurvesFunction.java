/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ConvexityFromCurvesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class BondConvexityFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> getCalculator() {
    return ConvexityFromCurvesCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.CONVEXITY;
  }
}

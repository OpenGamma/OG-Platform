/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromCurvesCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class BondModifiedDurationFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> getCalculator() {
    return ModifiedDurationFromCurvesCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.MODIFIED_DURATION;
  }
}

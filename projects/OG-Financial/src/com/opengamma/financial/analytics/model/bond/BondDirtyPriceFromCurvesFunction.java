/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.calculator.DirtyPriceFromCurvesCalculator;

/**
 * 
 */
public class BondDirtyPriceFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> getCalculator() {
    return DirtyPriceFromCurvesCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.DIRTY_PRICE;
  }
}

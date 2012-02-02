/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.bond.calculator.ModifiedDurationFromYieldCalculator;

/**
 * 
 */
public class BondModifiedDurationFromYieldFunction extends BondFromYieldFunction {

  @Override
  protected AbstractInstrumentDerivativeVisitor<Double, Double> getCalculator() {
    return ModifiedDurationFromYieldCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.MODIFIED_DURATION;
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.calculator.MacaulayDurationFromYieldCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * 
 */
public class BondMacaulayDurationFromYieldFunction extends BondFromYieldFunction {

  @Override
  protected AbstractInstrumentDerivativeVisitor<Double, Double> getCalculator() {
    return MacaulayDurationFromYieldCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.MACAULAY_DURATION;
  }
}

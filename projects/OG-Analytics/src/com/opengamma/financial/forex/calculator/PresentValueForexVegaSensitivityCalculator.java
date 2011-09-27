/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.financial.forex.method.PresentValueVolatilityNodeSensitivityDataBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public final class PresentValueForexVegaSensitivityCalculator extends AbstractForexDerivativeVisitor<SmileDeltaTermStructureDataBundle, PresentValueVolatilityNodeSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexVegaSensitivityCalculator INSTANCE = new PresentValueForexVegaSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexVegaSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueForexVegaSensitivityCalculator() {
  }

  @Override
  public PresentValueVolatilityNodeSensitivityDataBundle visitForexOptionVanilla(final ForexOptionVanilla option, final SmileDeltaTermStructureDataBundle data) {
    return ForexOptionVanillaBlackMethod.getInstance().presentValueVolatilityNodeSensitivity(option, data);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.method.PresentValueVolatilityQuoteSensitivityDataBundle;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public final class PresentValueForexVegaQuoteSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<SmileDeltaTermStructureDataBundle, PresentValueVolatilityQuoteSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexVegaQuoteSensitivityCalculator INSTANCE = new PresentValueForexVegaQuoteSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexVegaQuoteSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueForexVegaQuoteSensitivityCalculator() {
  }

  /**
   * The calculator used to compute the vega with respect to the volatilities by strikes.
   */
  private static final PresentValueForexVegaSensitivityCalculator VEGA_CALCULATOR = PresentValueForexVegaSensitivityCalculator.getInstance();

  @Override
  public PresentValueVolatilityQuoteSensitivityDataBundle visit(final InstrumentDerivative derivative, final SmileDeltaTermStructureDataBundle data) {
    return VEGA_CALCULATOR.visit(derivative, data).quoteSensitivity();
  }

}

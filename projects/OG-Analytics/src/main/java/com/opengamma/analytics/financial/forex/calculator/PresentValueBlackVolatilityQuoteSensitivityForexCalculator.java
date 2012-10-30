/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityQuoteSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public final class PresentValueBlackVolatilityQuoteSensitivityForexCalculator extends
    AbstractInstrumentDerivativeVisitor<SmileDeltaTermStructureDataBundle, PresentValueForexBlackVolatilityQuoteSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackVolatilityQuoteSensitivityForexCalculator INSTANCE = new PresentValueBlackVolatilityQuoteSensitivityForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackVolatilityQuoteSensitivityForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackVolatilityQuoteSensitivityForexCalculator() {
  }

  /**
   * The calculator used to compute the vega with respect to the volatilities by strikes.
   */
  private static final PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator VEGA_CALCULATOR = PresentValueBlackVolatilityNodeSensitivityBlackForexCalculator.getInstance();

  @Override
  public PresentValueForexBlackVolatilityQuoteSensitivityDataBundle visit(final InstrumentDerivative derivative, final SmileDeltaTermStructureDataBundle data) {
    return VEGA_CALCULATOR.visit(derivative, data).quoteSensitivity();
  }

}

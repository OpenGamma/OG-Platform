/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.curve.sensitivity.AbstractParameterSensitivityBlockCalculator;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * Computes the optimal hedging portfolio made of reference instruments to hedge a given sensitivity.
 * <p> Reference: Portfolio hedging with reference securities, version 1.0, OG notes, October 2010.
 */
public class PortfolioHedgingCalculator {

  /**
   * Computes the quantity of each reference instrument that optimally hedge a given sensitivity.
   * @param ps The parameter sensitivity of the portfolio to hedge.
   * @param rs The parameter sensitivities of the reference instruments.
   * @param w The related parameters weight matrix.
   * @return The optimal hedging quantities. The quantities are in the same order as the reference instruments sensitivities.
   */
  public static double[] hedgeQuantity(final ParameterSensitivity ps, final ParameterSensitivity[] rs, final DoubleMatrix2D w, final FXMatrix fxMatrix) {
    final Currency ccy = ps.getAllNamesCurrency().iterator().next().getSecond();
    // Implementation note: currency used for the conversion in a common currency. Any currency is fine.
    return null;
  }

  /**
   * Computes the quantity of each reference instrument that optimally hedge a given sensitivity.
   * @param ps The parameter sensitivity of the portfolio to hedge.
   * @param ins The reference instruments.
   * @param psc The parameter sensitivity calculator.
   * @param w The related parameters weight matrix.
   * @return The optimal hedging quantities. The quantities are in the same order as the reference instruments sensitivities.
   */
  public static double[] hedgeQuantity(final ParameterSensitivity ps, final InstrumentDerivative[] ins, final AbstractParameterSensitivityBlockCalculator psc, final DoubleMatrix2D w) {
    return null;
  }

}

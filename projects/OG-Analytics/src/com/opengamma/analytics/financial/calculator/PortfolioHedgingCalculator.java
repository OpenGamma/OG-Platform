/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionResult;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;

/**
 * Computes the optimal hedging portfolio made of reference instruments to hedge a given sensitivity.
 * <p> Reference: Portfolio hedging with reference securities, version 1.0, OG notes, October 2010.
 */
public class PortfolioHedgingCalculator {

  /**
   * The matrix algebra used (mainly multiplying matrices and solving systems).
   */
  private static final CommonsMatrixAlgebra MATRIX = new CommonsMatrixAlgebra();
  private static final SVDecompositionCommons DECOMPOSITION = new SVDecompositionCommons();

  /**
   * Computes the quantity of each reference instrument that optimally hedge a given sensitivity.
   * @param ps The parameter sensitivity of the portfolio to hedge.
   * @param rs The parameter sensitivities of the reference instruments.
   * @param w The related parameters weight matrix. // TODO: order of curves in w? Should we use the yieldCurveBundle to have the order (and all curves).
   * @param fxMatrix The matrix with exchange rates.
   * @return The optimal hedging quantities. The quantities are in the same order as the reference instruments sensitivities.
   * Note that the output is the optimal hedge quantity and not the portoflio equivalent. The hedge has the opposite sign of wrt the equivalent.
   */
  public static double[] hedgeQuantity(final ParameterSensitivity ps, final ParameterSensitivity[] rs, final DoubleMatrix2D w, final FXMatrix fxMatrix) {
    final Currency ccy = ps.getAllNamesCurrency().iterator().next().getSecond();
    // Implementation note: currency used for the conversion in a common currency. Any currency is fine.
    final int nbReference = rs.length;
    final ParameterSensitivity psConverted = ps.converted(fxMatrix, ccy);
    final ParameterSensitivity[] rsConverted = new ParameterSensitivity[nbReference];
    for (int loopref = 0; loopref < nbReference; loopref++) {
      rsConverted[loopref] = rs[loopref].converted(fxMatrix, ccy);
    }
    // Implementation note: converting the ParameterSensitivity into a matrix.
    DoubleMatrix1D p = psConverted.toMatrix();
    final double[][] rsArray = new double[nbReference][];
    for (int loopref = 0; loopref < nbReference; loopref++) {
      rsArray[loopref] = rsConverted[loopref].toMatrix().getData();
    }
    DoubleMatrix2D r = new DoubleMatrix2D(rsArray);
    DoubleMatrix2D wtW = (DoubleMatrix2D) MATRIX.multiply(MATRIX.getTranspose(w), w);
    DoubleMatrix2D rWtW = (DoubleMatrix2D) MATRIX.multiply(r, wtW);
    DoubleMatrix2D rWtWRt = (DoubleMatrix2D) MATRIX.multiply(rWtW, MATRIX.getTranspose(r));
    DoubleMatrix1D rWtWP = ((DoubleMatrix2D) MATRIX.scale(MATRIX.multiply(rWtW, p), -1.0)).getColumnVector(0);
    SVDecompositionResult dec = DECOMPOSITION.evaluate(rWtWRt);
    DoubleMatrix1D q = dec.solve(rWtWP);
    return q.getData();
  }

  // TODO: do we need the second version?
  //  /**
  //   * Computes the quantity of each reference instrument that optimally hedge a given sensitivity.
  //   * @param ps The parameter sensitivity of the portfolio to hedge.
  //   * @param ins The reference instruments.
  //   * @param psc The parameter sensitivity calculator.
  //   * @param w The related parameters weight matrix.
  //   * @return The optimal hedging quantities. The quantities are in the same order as the reference instruments sensitivities.
  //   */
  //  public static double[] hedgeQuantity(final ParameterSensitivity ps, final InstrumentDerivative[] ins, final AbstractParameterSensitivityBlockCalculator psc, final DoubleMatrix2D w) {
  //    return null;
  //  }

}

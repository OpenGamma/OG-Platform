/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import java.util.LinkedHashSet;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionResult;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Computes the optimal hedging portfolio made of reference instruments to hedge a given sensitivity.
 * <p> Reference: Portfolio hedging with reference securities, version 1.0, OG notes, October 2010.
 */
public class PortfolioHedgingCalculator {

  /**
   * The matrix algebra used (mainly multiplying matrices and solving systems).
   */
  private static final CommonsMatrixAlgebra MATRIX = new CommonsMatrixAlgebra();
  /**
   * The decomposition method used.
   */
  private static final SVDecompositionCommons DECOMPOSITION = new SVDecompositionCommons();

  /**
   * Computes the quantity of each reference instrument that optimally hedge a given sensitivity.
   * @param ps The parameter sensitivity of the portfolio to hedge.
   * @param rs The parameter sensitivities of the reference instruments.
   * @param w The related parameters weight matrix. The order of the curve should be their "natural" order.
   * @param order The ordered set of name.
   * @param fxMatrix The matrix with exchange rates.
   * @return The optimal hedging quantities. The quantities are in the same order as the reference instruments sensitivities.
   * Note that the output is the optimal hedge quantity and not the portfolio equivalent. The hedge has the opposite sign of wrt the equivalent.
   */
  public static double[] hedgeQuantity(final MultipleCurrencyParameterSensitivity ps, final MultipleCurrencyParameterSensitivity[] rs, final DoubleMatrix2D w,
      final LinkedHashSet<Pair<String, Integer>> order, final FXMatrix fxMatrix) {
    final Currency ccy = ps.getAllNamesCurrency().iterator().next().getSecond();
    // Implementation note: currency used for the conversion in a common currency. Any currency is fine.
    final int nbReference = rs.length;
    final MultipleCurrencyParameterSensitivity psConverted = ps.converted(fxMatrix, ccy);
    final MultipleCurrencyParameterSensitivity[] rsConverted = new MultipleCurrencyParameterSensitivity[nbReference];
    for (int loopref = 0; loopref < nbReference; loopref++) {
      rsConverted[loopref] = rs[loopref].converted(fxMatrix, ccy);
    }
    // Implementation note: converting the ParameterSensitivity into a matrix.
    final DoubleMatrix1D p = toMatrix(psConverted, order);
    final double[][] rsArray = new double[nbReference][];
    for (int loopref = 0; loopref < nbReference; loopref++) {
      rsArray[loopref] = toMatrix(rsConverted[loopref], order).getData();
    }
    final DoubleMatrix2D r = new DoubleMatrix2D(rsArray);
    final DoubleMatrix2D wtW = (DoubleMatrix2D) MATRIX.multiply(MATRIX.getTranspose(w), w);
    final DoubleMatrix2D rWtW = (DoubleMatrix2D) MATRIX.multiply(r, wtW);
    final DoubleMatrix2D rWtWRt = (DoubleMatrix2D) MATRIX.multiply(rWtW, MATRIX.getTranspose(r));
    final DoubleMatrix1D rWtWP = ((DoubleMatrix2D) MATRIX.scale(MATRIX.multiply(rWtW, p), -1.0)).getColumnVector(0);
    final SVDecompositionResult dec = DECOMPOSITION.evaluate(rWtWRt);
    final DoubleMatrix1D q = dec.solve(rWtWP);
    return q.getData();
  }

  /**
   * Convert the parameter sensitivity into a matrix (DoubleMatrix1D). All the sensitivities should be in the same currency.
   * The matrix is composed of the sensitivity vectors (currency is ignored) one after the other.
   * The matrix order is the one of the set.
   * @param sensi The sensitivity.
   * @param order The ordered set of name.
   * @return The sensitivity matrix.
   */
  public static DoubleMatrix1D toMatrix(final MultipleCurrencyParameterSensitivity sensi, final LinkedHashSet<Pair<String, Integer>> order) {
    double[] psArray = new double[0];
    final Currency ccy = sensi.getAllNamesCurrency().iterator().next().getSecond();
    // Implementation note: all the currencies are supposed to be the same, we choose any of them.
    for (final Pair<String, Integer> nameSize : order) {
      if (sensi.getSensitivities().containsKey(ObjectsPair.of(nameSize.getFirst(), ccy))) {
        psArray = ArrayUtils.addAll(psArray, sensi.getSensitivity(nameSize.getFirst(), ccy).getData());
      } else { // When curve is not in the sensitivity, add zeros.
        psArray = ArrayUtils.addAll(psArray, new double[nameSize.getSecond()]);
      }
    }
    return new DoubleMatrix1D(psArray);
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the portfolio hedging calculator, with simplified examples and then with full scale data.
 */
public class PortfolioHedgingCalculatorTest {

  private static final double TOLERANCE = 1.0E-8;

  @Test
  /**
   * Test the hedging portfolio with reference instruments equal to the curve construction instruments. 
   */
  public void exactSolution() {
    double[] sensi = {1.0, 2.0, 3.0, 0.4 };
    int nbSensi = sensi.length;
    double[] sensiOpposite = new double[nbSensi];
    for (int loopnode = 0; loopnode < nbSensi; loopnode++) {
      sensiOpposite[loopnode] = -sensi[loopnode];
    }
    Pair<String, Currency> nameCcy = new ObjectsPair<String, Currency>("Curve", Currency.AUD);
    FXMatrix fxMatrix = new FXMatrix(nameCcy.getSecond());
    ParameterSensitivity ps = new ParameterSensitivity();
    ps = ps.plus(nameCcy, new DoubleMatrix1D(sensi));
    ParameterSensitivity[] rs = new ParameterSensitivity[nbSensi];
    for (int loopnode = 0; loopnode < nbSensi; loopnode++) {
      rs[loopnode] = new ParameterSensitivity();
      double[] r = new double[nbSensi];
      r[loopnode] = 1.0;
      rs[loopnode] = rs[loopnode].plus(nameCcy, new DoubleMatrix1D(r));
    }
    // Unit weights
    DoubleMatrix2D w1 = new DoubleMatrix2D(nbSensi, nbSensi);
    for (int loopnode = 0; loopnode < nbSensi; loopnode++) {
      w1.getData()[loopnode][loopnode] = 1.0;
    }
    double[] hedging1 = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w1, fxMatrix);
    assertArrayEquals("PortfolioHedgingCalculator: ", sensiOpposite, hedging1, TOLERANCE);
    // Non-uniform diagonal weights
    DoubleMatrix2D w2 = new DoubleMatrix2D(nbSensi, nbSensi);
    for (int loopnode = 0; loopnode < nbSensi; loopnode++) {
      w2.getData()[loopnode][loopnode] = loopnode + 1.0;
    }
    double[] hedging2 = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w2, fxMatrix);
    assertArrayEquals("PortfolioHedgingCalculator: ", sensiOpposite, hedging2, TOLERANCE);
    // Tri-diagonal weights
    double[][] w3Array = { {1.0, 0.5, 0, 0 }, {0.5, 1.0, 0.5, 0 }, {0, 0.5, 1.0, 0.5 }, {0, 0, 0.5, 1.0 } };
    DoubleMatrix2D w3 = new DoubleMatrix2D(w3Array);
    double[] hedging3 = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w3, fxMatrix);
    assertArrayEquals("PortfolioHedgingCalculator: ", sensiOpposite, hedging3, TOLERANCE);
  }

}

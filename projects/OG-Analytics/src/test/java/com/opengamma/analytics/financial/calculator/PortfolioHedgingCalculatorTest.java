/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests the portfolio hedging calculator, with simplified examples and then with full scale data.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioHedgingCalculatorTest {

  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final String NAME_1 = "Dsc";
  private static final String NAME_2 = "Fwd";
  private static final Pair<String, Currency> NAME_1_EUR = Pairs.of(NAME_1, EUR);
  private static final Pair<String, Currency> NAME_1_USD = Pairs.of(NAME_1, USD);
  private static final Pair<String, Currency> NAME_2_EUR = Pairs.of(NAME_2, EUR);
  private static final double EUR_USD = 1.25;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR, USD, EUR_USD);

  private static final double[] SENSI_1 = {1.0, 2.0, 3.0, 0.4};
  private static final int NB_SENSI_1 = SENSI_1.length;
  private static final double[] SENSI_2 = {0.5, 1.0, 0.5, 1.0, 0.5};
  private static final int NB_SENSI_2 = SENSI_2.length;

  private static final LinkedHashSet<Pair<String, Integer>> ORDER = new LinkedHashSet<>();
  static {
    ORDER.add(Pairs.of(NAME_1, NB_SENSI_1));
    ORDER.add(Pairs.of(NAME_2, NB_SENSI_2));
  }

  private static final CommonsMatrixAlgebra MATRIX = new CommonsMatrixAlgebra();

  private static final double TOLERANCE = 1.0E-8;

  /**
   * Test the hedging portfolio with reference instruments equal to the curve construction instruments.
   */
  public void exactSolution() {
    final LinkedHashSet<Pair<String, Integer>> order = new LinkedHashSet<>();
    order.add(Pairs.of(NAME_1, NB_SENSI_1));
    final double[] sensiOpposite = new double[NB_SENSI_1];
    for (int loopnode = 0; loopnode < NB_SENSI_1; loopnode++) {
      sensiOpposite[loopnode] = -SENSI_1[loopnode];
    }
    MultipleCurrencyParameterSensitivity ps = new MultipleCurrencyParameterSensitivity();
    ps = ps.plus(NAME_1_EUR, new DoubleMatrix1D(SENSI_1));
    final MultipleCurrencyParameterSensitivity[] rs = new MultipleCurrencyParameterSensitivity[NB_SENSI_1];
    for (int loopnode = 0; loopnode < NB_SENSI_1; loopnode++) {
      rs[loopnode] = new MultipleCurrencyParameterSensitivity();
      final double[] r = new double[NB_SENSI_1];
      r[loopnode] = 1.0;
      rs[loopnode] = rs[loopnode].plus(NAME_1_EUR, new DoubleMatrix1D(r));
    }
    // Unit weights
    final DoubleMatrix2D w1 = new DoubleMatrix2D(NB_SENSI_1, NB_SENSI_1);
    for (int loopnode = 0; loopnode < NB_SENSI_1; loopnode++) {
      w1.getData()[loopnode][loopnode] = 1.0;
    }
    final double[] hedging1 = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w1, order, FX_MATRIX);
    assertArrayEquals("PortfolioHedgingCalculator: ", sensiOpposite, hedging1, TOLERANCE);
    // Non-uniform diagonal weights
    final DoubleMatrix2D w2 = new DoubleMatrix2D(NB_SENSI_1, NB_SENSI_1);
    for (int loopnode = 0; loopnode < NB_SENSI_1; loopnode++) {
      w2.getData()[loopnode][loopnode] = loopnode + 1.0;
    }
    final double[] hedging2 = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w2, order, FX_MATRIX);
    assertArrayEquals("PortfolioHedgingCalculator: ", sensiOpposite, hedging2, TOLERANCE);
    // Tri-diagonal weights
    final double[][] w3Array = { {1.0, 0.5, 0, 0}, {0.5, 1.0, 0.5, 0}, {0, 0.5, 1.0, 0.5}, {0, 0, 0.5, 1.0}};
    final DoubleMatrix2D w3 = new DoubleMatrix2D(w3Array);
    final double[] hedging3 = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w3, order, FX_MATRIX);
    assertArrayEquals("PortfolioHedgingCalculator: ", sensiOpposite, hedging3, TOLERANCE);
  }

  /**
   * Test the hedging portfolio. The answer is perturbed to check that it is at a minimum.
   */
  public void checkMin() {
    final int nbSensi = NB_SENSI_1 + NB_SENSI_2;
    MultipleCurrencyParameterSensitivity ps = new MultipleCurrencyParameterSensitivity();
    ps = ps.plus(NAME_1_EUR, new DoubleMatrix1D(SENSI_1));
    ps = ps.plus(NAME_1_USD, new DoubleMatrix1D(SENSI_1));
    ps = ps.plus(NAME_2_EUR, new DoubleMatrix1D(SENSI_2));
    final int nbReference = 4;
    final MultipleCurrencyParameterSensitivity[] rs = new MultipleCurrencyParameterSensitivity[nbReference];
    rs[0] = new MultipleCurrencyParameterSensitivity();
    rs[0] = rs[0].plus(NAME_1_EUR, new DoubleMatrix1D(new double[] {1.0, 0.0, 0.0, 0.0}));
    rs[0] = rs[0].plus(NAME_2_EUR, new DoubleMatrix1D(new double[] {1.0, 0.0, 0.0, 0.0, 0.0}));
    rs[1] = new MultipleCurrencyParameterSensitivity();
    rs[1] = rs[1].plus(NAME_1_EUR, new DoubleMatrix1D(new double[] {0.0, 0.5, 0.0, 0.0}));
    rs[1] = rs[1].plus(NAME_2_EUR, new DoubleMatrix1D(new double[] {0.0, 1.0, 0.0, 0.0, 0.0}));
    rs[2] = new MultipleCurrencyParameterSensitivity();
    rs[2] = rs[2].plus(NAME_1_EUR, new DoubleMatrix1D(new double[] {0.0, 0.0, 1.0, 2.0}));
    rs[2] = rs[2].plus(NAME_2_EUR, new DoubleMatrix1D(new double[] {0.0, 0.0, 0.0, 0.0, 0.0}));
    rs[3] = new MultipleCurrencyParameterSensitivity();
    rs[3] = rs[3].plus(NAME_1_EUR, new DoubleMatrix1D(new double[] {0.0, 0.0, 0.0, 0.0}));
    rs[3] = rs[3].plus(NAME_2_EUR, new DoubleMatrix1D(new double[] {0.0, 0.0, 1.0, 1.0, 1.0}));
    // Weights: tridiagonal + sum by curve
    final DoubleMatrix2D w = new DoubleMatrix2D(nbSensi + 2, nbSensi);
    for (int loopnode = 0; loopnode < nbSensi; loopnode++) {
      w.getData()[loopnode][loopnode] = 1.0;
    }
    for (int loopnode = 0; loopnode < nbSensi - 1; loopnode++) {
      w.getData()[loopnode][loopnode + 1] = 0.5;
      w.getData()[loopnode + 1][loopnode] = 0.5;
    }
    w.getData()[NB_SENSI_1 + NB_SENSI_2] = new double[] {1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}; // Order?
    w.getData()[NB_SENSI_1 + NB_SENSI_2 + 1] = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    final DoubleMatrix2D wtW = (DoubleMatrix2D) MATRIX.multiply(MATRIX.getTranspose(w), w);
    // Hedging
    final double[] hedging = PortfolioHedgingCalculator.hedgeQuantity(ps, rs, w, ORDER, FX_MATRIX);
    MultipleCurrencyParameterSensitivity psMin = new MultipleCurrencyParameterSensitivity();
    psMin = psMin.plus(ps);
    for (int loopref = 0; loopref < nbReference; loopref++) { // To created the hedge portfolio
      psMin = psMin.plus(rs[loopref].multipliedBy(hedging[loopref]));
    }
    final DoubleMatrix1D psMinMatrix = PortfolioHedgingCalculator.toMatrix(psMin.converted(FX_MATRIX, EUR), ORDER);
    final DoubleMatrix2D psMinMatrixT = new DoubleMatrix2D(new double[][] {psMinMatrix.getData()});
    final double penalty = ((DoubleMatrix2D) MATRIX.multiply(psMinMatrixT, MATRIX.multiply(wtW, psMinMatrix))).getEntry(0, 0);

    final double shift = 0.01;
    final double[] penaltyPlus = new double[nbReference];
    final double[] penaltyMinus = new double[nbReference];
    for (int loopref = 0; loopref < nbReference; loopref++) { // Shift on each quantity
      MultipleCurrencyParameterSensitivity psPertPlus = new MultipleCurrencyParameterSensitivity();
      psPertPlus = psPertPlus.plus(psMin);
      psPertPlus = psPertPlus.plus(rs[loopref].multipliedBy(shift));
      final DoubleMatrix1D psPertPlusMat = PortfolioHedgingCalculator.toMatrix(psPertPlus.converted(FX_MATRIX, EUR), ORDER);
      final DoubleMatrix2D psPertPlusMatT = new DoubleMatrix2D(new double[][] {psPertPlusMat.getData()});
      penaltyPlus[loopref] = ((DoubleMatrix2D) MATRIX.multiply(psPertPlusMatT, MATRIX.multiply(wtW, psPertPlusMat))).getEntry(0, 0);
      assertTrue("PortfolioHedgingCalculator: minimum", penalty < penaltyPlus[loopref]);

      MultipleCurrencyParameterSensitivity psPertMinus = new MultipleCurrencyParameterSensitivity();
      psPertMinus = psPertMinus.plus(psMin);
      psPertMinus = psPertMinus.plus(rs[loopref].multipliedBy(-shift));
      final DoubleMatrix1D psPertMinusMat = PortfolioHedgingCalculator.toMatrix(psPertMinus.converted(FX_MATRIX, EUR), ORDER);
      final DoubleMatrix2D psPertMinusMatT = new DoubleMatrix2D(new double[][] {psPertMinusMat.getData()});
      penaltyMinus[loopref] = ((DoubleMatrix2D) MATRIX.multiply(psPertMinusMatT, MATRIX.multiply(wtW, psPertMinusMat))).getEntry(0, 0);
      assertTrue("PortfolioHedgingCalculator: minimum " + loopref, penalty < penaltyMinus[loopref]);
    }

  }

  private static final DoubleMatrix1D SENSITIVITY_1_1 = new DoubleMatrix1D(4.0, 2.0, 5.0, 1.5);
  private static final DoubleMatrix1D SENSITIVITY_2_1 = new DoubleMatrix1D(5.0, 1.0, 2.0, 5.0, 1.5);

  public void testToMatrix() {
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map1 = Maps.newLinkedHashMap();
    map1.put(NAME_1_EUR, SENSITIVITY_1_1);
    map1.put(NAME_2_EUR, SENSITIVITY_2_1);
    final MultipleCurrencyParameterSensitivity sensitivity1 = MultipleCurrencyParameterSensitivity.of(map1);
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map2 = Maps.newLinkedHashMap();
    map2.put(NAME_1_EUR, SENSITIVITY_1_1);
    map2.put(NAME_2_EUR, SENSITIVITY_1_1);
    final MultipleCurrencyParameterSensitivity sensitivity2 = MultipleCurrencyParameterSensitivity.of(map2);
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map3 = Maps.newLinkedHashMap();
    map3.put(NAME_2_EUR, SENSITIVITY_2_1);
    map3.put(NAME_1_EUR, SENSITIVITY_1_1);
    final MultipleCurrencyParameterSensitivity sensitivity3 = MultipleCurrencyParameterSensitivity.of(map3);
    final double[] total1 = new double[SENSITIVITY_1_1.getNumberOfElements() + SENSITIVITY_2_1.getNumberOfElements()];
    int j = 0;
    for (int i = 0; i < SENSITIVITY_1_1.getNumberOfElements(); i++, j++) {
      total1[j] = SENSITIVITY_1_1.getEntry(i);
    }
    for (int i = 0; i < SENSITIVITY_2_1.getNumberOfElements(); i++, j++) {
      total1[j] = SENSITIVITY_2_1.getEntry(i);
    }
    final DoubleMatrix1D expectedMatrix1 = new DoubleMatrix1D(total1);
    assertEquals("PortfolioHedgingCalculator: toMatrix", expectedMatrix1, PortfolioHedgingCalculator.toMatrix(sensitivity1, ORDER));
    assertEquals("PortfolioHedgingCalculator: toMatrix", expectedMatrix1, PortfolioHedgingCalculator.toMatrix(sensitivity3, ORDER));
    assertFalse("Test toMatrix, unequal sensitivities: ", expectedMatrix1.equals(PortfolioHedgingCalculator.toMatrix(sensitivity2, ORDER)));
  }

}

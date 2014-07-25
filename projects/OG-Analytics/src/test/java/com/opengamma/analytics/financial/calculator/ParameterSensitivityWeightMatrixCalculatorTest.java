/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.testng.annotations.Test;

import cern.colt.list.IntArrayList;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ParameterSensitivityWeightMatrixCalculatorTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final CommonsMatrixAlgebra MA = new CommonsMatrixAlgebra();

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();

  /**
   * Use multicurve in {@link MulticurveProviderDiscountDataSets}
   */
  @Test
  public void fullSampleTest() {
    ParameterSensitivityWeightMatrixCalculator calc = new ParameterSensitivityWeightMatrixCalculator();
    //    YieldAndDiscountCurve curve = MULTICURVES.getCurve("USD Dsc");
    String name1 = MULTICURVES.getName(Currency.USD);
    String name2 = MULTICURVES.getName(USDLIBOR3M);
    YieldCurve curve1 = (YieldCurve) MULTICURVES.getCurve(name1);
    YieldCurve curve2 = (YieldCurve) MULTICURVES.getCurve(name2);
    //    YieldAndDiscountCurve curve99 = MULTICURVES.getCurve("USD Dsc");

    LinkedHashSet<Pair<String, Integer>> order = new LinkedHashSet<>();
    order.add(Pairs.of(name1, MULTICURVES.getNumberOfParameters(name1)));
    order.add(Pairs.of(name2, MULTICURVES.getNumberOfParameters(name2)));

    Double[] nodes1 = curve1.getCurve().getXData();
    Double[] nodes2 = curve2.getCurve().getXData();
    double[] objNodes = new double[] {nodes1[1], nodes1[5], nodes1[10], 55. };

    /**
     * Tests for reduction method
     */
    DoubleMatrix2D matrix = calc.reduceCurveNodes(MULTICURVES, order, objNodes);

    int nNodes1 = nodes1.length;
    int nNodes2 = nodes2.length;
    int expectedNumCols = nNodes1 + nNodes2;

    IntArrayList list = new IntArrayList();
    for (int i = 0; i < objNodes.length - 1; ++i) {
      list.add(Arrays.binarySearch(nodes1, objNodes[i]));
      int position = Arrays.binarySearch(nodes2, objNodes[i]);
      if (position >= 0) {
        list.add(nNodes1 + position);
      }
    }
    int expectedNumRows = list.size();
    int[] positionArray = new int[expectedNumRows];
    double[][] expectedMatrix = new double[expectedNumRows][expectedNumCols];
    for (int i = 0; i < expectedNumRows; ++i) {
      positionArray[i] = list.get(i);
      expectedMatrix[i][positionArray[i]] = 1.0;
    }

    double[] nodes1p = new double[nNodes1];
    double[] nodes2p = new double[nNodes2];
    for (int i = 0; i < nNodes1; ++i) {
      nodes1p[i] = nodes1[i];
    }
    for (int i = 0; i < nNodes2; ++i) {
      nodes2p[i] = nodes2[i];
    }
    Iterator<Pair<String, Integer>> itr = order.iterator();
    assertEquals(itr.next().getValue().intValue(), nNodes1);
    assertEquals(itr.next().getValue().intValue(), nNodes2);

    assertMatrix(new DoubleMatrix2D(expectedMatrix), matrix, 1.e-14);

    DoubleMatrix2D matSq = (DoubleMatrix2D) MA.multiply(MA.getTranspose(matrix), matrix);
    assertMatrix(matSq, (DoubleMatrix2D) MA.multiply(matSq, matSq), 1.e-14);

    DoubleMatrix2D matrixFromCurves = calc.reduceCurveNodes(new YieldAndDiscountCurve[] {curve1, curve2 }, objNodes);
    assertMatrix(matrix, matrixFromCurves, 1.e-14);
    double[] combinedNodes = new double[expectedNumCols];
    System.arraycopy(nodes1p, 0, combinedNodes, 0, nNodes1);
    System.arraycopy(nodes2p, 0, combinedNodes, nNodes1, nNodes2);
    DoubleMatrix2D matrixFromTotalNodes = calc.reduceCurveNodes(combinedNodes, objNodes);
    assertMatrix(matrix, matrixFromTotalNodes, 1.e-14);

    /**
     * Indirect tests for projection method
     */
    DoubleMatrix2D matrixProject = calc.projectCurveNodes(MULTICURVES, order, objNodes);
    int nRows = matrixProject.getNumberOfRows();
    int nCols = matrixProject.getNumberOfColumns();
    assertEquals(matrix.getNumberOfRows(), nRows);
    assertEquals(matrix.getNumberOfColumns(), nCols);
    Arrays.sort(positionArray);
    for (int i = 0; i < expectedNumRows; ++i) {
      assertTrue(matrixProject.getData()[i][positionArray[i]] == 1.0);
    }
    DoubleMatrix2D matSqP = (DoubleMatrix2D) MA.multiply(MA.getTranspose(matrixProject), matrixProject);
    for (int i = 0; i < nCols; ++i) {
      assertEquals(1.0, matSqP.getData()[i][i]);
      for (int j = 0; j < nCols; ++j) {
        assertTrue(matSqP.getData()[i][j] == 0.0 || matSqP.getData()[i][j] == 1.0);
      }
    }

    /**
     * Indirect tests for projection method
     */
    LinkedHashSet<Pair<String, Integer>> orderId = new LinkedHashSet<>();
    orderId.add(Pairs.of(name1, MULTICURVES.getNumberOfParameters(name1)));
    orderId.add(Pairs.of(name2, MULTICURVES.getNumberOfParameters(name2)));
    double[] objNodesId = new double[] {1., 5., 10. };
    DoubleMatrix2D matrixProjectId = calc.projectCurvesAndNodes(MULTICURVES, orderId, objNodesId);
    int nRowsId = matrixProjectId.getNumberOfRows();
    int nColsId = matrixProjectId.getNumberOfColumns();
    assertEquals(objNodesId.length, nRowsId);
    assertEquals(MULTICURVES.getNumberOfParameters(name1) + MULTICURVES.getNumberOfParameters(name2), nColsId);
    DoubleMatrix2D matSqPId = (DoubleMatrix2D) MA.multiply(MA.getTranspose(matrixProjectId), matrixProjectId);
    for (int i = 0; i < nColsId; ++i) {
      assertEquals(1.0, matSqPId.getData()[i][i]);
      for (int j = 0; j < nColsId; ++j) {
        assertTrue(matSqPId.getData()[i][j] == 0.0 || matSqPId.getData()[i][j] == 1.0);
      }
    }

    /**
     * Exception expected
     */
    double[] poorObjNodes = new double[] {21., 33., 39. };
    try {
      calc.reduceCurveNodes(MULTICURVES, order, poorObjNodes);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("None of the objective nodes are found in curve nodes", e.getMessage());
    }
    LinkedHashSet<Pair<String, Integer>> orderEmp = new LinkedHashSet<>();
    try {
      calc.reduceCurveNodes(MULTICURVES, orderEmp, objNodes);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("order should not be empty", e.getMessage());
    }
    try {
      calc.projectCurveNodes(MULTICURVES, orderEmp, objNodes);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("order should not be empty", e.getMessage());
    }
    try {
      calc.projectCurvesAndNodes(MULTICURVES, orderEmp, objNodesId);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("order should not be empty", e.getMessage());
    }

    try {
      calc.projectCurvesAndNodes(MULTICURVES, order, objNodes);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("All of the elements in objNodes should be found in the curves for this method", e.getMessage());
    }
  }

  private void assertMatrix(final DoubleMatrix2D matrixExp, final DoubleMatrix2D matrixRes, final double eps) {
    double[][] matExp = matrixExp.getData();
    double[][] matRes = matrixRes.getData();
    int nRows = matExp.length;
    int nCols = matExp[0].length;
    assertEquals(nRows, matRes.length);
    assertEquals(nCols, matRes[0].length);

    for (int i = 0; i < nRows; ++i) {
      for (int j = 0; j < nCols; ++j) {
        assertEquals(matExp[i][j], matRes[i][j], eps);
      }
    }
  }
}

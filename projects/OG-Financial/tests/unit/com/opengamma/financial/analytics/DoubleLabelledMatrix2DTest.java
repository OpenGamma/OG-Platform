/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

/**
 * 
 */
public class DoubleLabelledMatrix2DTest {
  private static final Double[] SORTED_X_KEYS = new Double[] {1., 2., 3., 4., 5.};
  private static final Double[] SORTED_Y_KEYS = new Double[] {1., 2., 3.};
  private static final Object[] SORTED_X_LABELS1 = new String[] {"1.0", "2.0", "3.0", "4.0", "5.0"};
  private static final Object[] SORTED_Y_LABELS1 = new String[] {"1.0", "2.0", "3.0"};
  private static final Object[] SORTED_X_LABELS2 = new String[] {"A", "B", "C", "D", "E"};
  private static final Object[] SORTED_Y_LABELS2 = new String[] {"A", "B", "C"};
  private static final double[][] SORTED_VALUES = new double[][] {new double[] {1, 2, 3, 4, 5}, new double[] {2, 4, 6, 8, 10}, new double[] {3, 6, 9, 12, 15}};
  private static final Double[] X_KEYS = new Double[] {2., 1., 3., 5., 4.};
  private static final Double[] Y_KEYS = new Double[] {2., 3., 1.};
  private static final Object[] X_LABELS1 = new Object[] {"2.0", "1.0", "3.0", "5.0", "4.0"};
  private static final Object[] Y_LABELS1 = new Object[] {"2.0", "3.0", "1.0"};
  private static final Object[] X_LABELS2 = new Object[] {"B", "A", "C", "E", "D"};
  private static final Object[] Y_LABELS2 = new Object[] {"B", "C", "A"};
  private static final double[][] VALUES = new double[][] {new double[] {4, 2, 6, 10, 8}, new double[] {6, 3, 9, 15, 12}, new double[] {2, 1, 3, 5, 4}};

  @Test
  public void test() {
    DoubleLabelledMatrix2D result = new DoubleLabelledMatrix2D(X_KEYS, Y_KEYS, VALUES);
    testResult(result, SORTED_X_LABELS1, SORTED_Y_LABELS1);
    result = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS1, Y_KEYS, Y_LABELS1, VALUES);
    testResult(result, SORTED_X_LABELS1, SORTED_Y_LABELS1);
    result = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS2, Y_KEYS, Y_LABELS2, VALUES);
    testResult(result, SORTED_X_LABELS2, SORTED_Y_LABELS2);
  }

  private void testResult(DoubleLabelledMatrix2D result, Object[] xLabels, Object[] yLabels) {
    Double[] xKeysResult = result.getXKeys();
    assertArrayEquals(SORTED_X_KEYS, xKeysResult);
    Object[] xLabelsResult = result.getXLabels();
    assertArrayEquals(xLabels, xLabelsResult);
    Double[] yKeysResult = result.getYKeys();
    assertArrayEquals(SORTED_Y_KEYS, yKeysResult);
    Object[] yLabelsResult = result.getYLabels();
    assertArrayEquals(yLabels, yLabelsResult);
    double[][] valuesResult = result.getValues();
    for (int i = 0; i < yKeysResult.length; i++) {
      for (int j = 0; j < xKeysResult.length; j++) {
        assertEquals(valuesResult[i][j], SORTED_VALUES[i][j]);
      }
    }
  }

}

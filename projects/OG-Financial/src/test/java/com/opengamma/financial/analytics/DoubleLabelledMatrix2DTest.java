/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
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
  private static final String X_TITLE = "x";
  private static final Double[] Y_KEYS = new Double[] {2., 3., 1.};
  private static final String Y_TITLE = "y";
  private static final Object[] X_LABELS1 = new Object[] {"2.0", "1.0", "3.0", "5.0", "4.0"};
  private static final Object[] Y_LABELS1 = new Object[] {"2.0", "3.0", "1.0"};
  private static final Object[] X_LABELS2 = new Object[] {"B", "A", "C", "E", "D"};
  private static final Object[] Y_LABELS2 = new Object[] {"B", "C", "A"};
  private static final double[][] VALUES = new double[][] {new double[] {4, 2, 6, 10, 8}, new double[] {6, 3, 9, 15, 12}, new double[] {2, 1, 3, 5, 4}};
  private static final String VALUES_TITLE = "values";

  @Test
  public void test() {
    DoubleLabelledMatrix2D result = new DoubleLabelledMatrix2D(X_KEYS, Y_KEYS, VALUES);
    testResult(result, SORTED_X_LABELS1, SORTED_Y_LABELS1);
    result = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS1, Y_KEYS, Y_LABELS1, VALUES);
    testResult(result, SORTED_X_LABELS1, SORTED_Y_LABELS1);
    result = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS2, Y_KEYS, Y_LABELS2, VALUES);
    testResult(result, SORTED_X_LABELS2, SORTED_Y_LABELS2);
  }
  
  @Test
  public void testTitles() {
    DoubleLabelledMatrix2D withTitles = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS1, X_TITLE, Y_KEYS, Y_LABELS1, Y_TITLE, VALUES, VALUES_TITLE);
    assertEquals(X_TITLE, withTitles.getXTitle());
    assertEquals(Y_TITLE, withTitles.getYTitle());
    assertEquals(VALUES_TITLE, withTitles.getValuesTitle());
    DoubleLabelledMatrix2D withoutTitles = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS1, Y_KEYS, Y_LABELS1, VALUES);
    assertFalse(withTitles.equals(withoutTitles));
  }

  @Test
  public void testAdd() {
    Double[] otherX = new Double[] {6., 7.};
    Double[] otherY = new Double[] {4., 5., 6., 7., 8.};
    Object[] otherXLabels = new Object[] {"F", "G"};
    Object[] otherYLabels = new Object[] {"D", "E", "F", "G", "H"};
    double[][] values = new double[][] {new double[] {24, 28}, new double[] {30, 35}, new double[] {36, 42}, new double[] {42, 49}, new double[] {48, 56}};
    final DoubleLabelledMatrix2D originalMatrix = new DoubleLabelledMatrix2D(SORTED_X_KEYS, SORTED_X_LABELS2, SORTED_Y_KEYS, SORTED_Y_LABELS2, SORTED_VALUES);
    DoubleLabelledMatrix2D otherMatrix = new DoubleLabelledMatrix2D(otherX, otherXLabels, otherY, otherYLabels, values);
    LabelledMatrix2D<Double, Double> sum = originalMatrix.add(otherMatrix, 0.0001, 0.0001);
    double[][] expectedValues = new double[][] {new double[] {1, 2, 3, 4, 5, 0, 0},
                                                new double[] {2, 4, 6, 8, 10, 0, 0},
                                                new double[] {3, 6, 9, 12, 15, 0, 0},
                                                new double[] {0, 0, 0, 0, 0, 24, 28},
                                                new double[] {0, 0, 0, 0, 0, 30, 35},
                                                new double[] {0, 0, 0, 0, 0, 36, 42},
                                                new double[] {0, 0, 0, 0, 0, 42, 49},
                                                new double[] {0, 0, 0, 0, 0, 48, 56}};
    LabelledMatrix2D<Double, Double> expectedSum = new DoubleLabelledMatrix2D(new Double[] {1., 2., 3., 4., 5., 6., 7.},
                                                                              new Object[] {"A", "B", "C", "D", "E", "F", "G"},
                                                                              new Double[] {1., 2., 3., 4., 5., 6., 7., 8.},
                                                                              new Object[] {"A", "B", "C", "D", "E", "F", "G", "H"},
                                                                              expectedValues);
    assertArrayEquals(expectedSum.getXKeys(), sum.getXKeys());
    assertArrayEquals(expectedSum.getXLabels(), sum.getXLabels());
    assertArrayEquals(expectedSum.getYKeys(), sum.getYKeys());
    assertArrayEquals(expectedSum.getYLabels(), sum.getYLabels());
    for (int i = 0; i < expectedValues.length; i++) {
      for (int j = 0; j < expectedValues[0].length; j++) {
        assertEquals(expectedValues[i][j], sum.getValues()[i][j]);
      }
    }
    otherX = new Double[] {1., 2.};
    otherY = new Double[] {1., 2., 3., 4., 5.};
    otherXLabels = new Object[] {"A", "B"};
    otherYLabels = new Object[] {"A", "B", "C", "D", "E"};
    values = new double[][] {new double[] {10, 100}, new double[] {20, 200}, new double[] {30, 300}, new double[] {40, 400}, new double[] {50, 500}};
    otherMatrix = new DoubleLabelledMatrix2D(otherX, otherXLabels, otherY, otherYLabels, values);
    sum = originalMatrix.add(otherMatrix, 0.0001, 0.0001);
    expectedValues = new double[][] {new double[] {11, 102, 3, 4, 5},
                                     new double[] {22, 204, 6, 8, 10},
                                     new double[] {33, 306, 9, 12, 15},
                                     new double[] {40, 400, 0, 0, 0},
                                     new double[] {50, 500, 0, 0, 0}};
    expectedSum = new DoubleLabelledMatrix2D(new Double[] {1., 2., 3., 4., 5.},
                                             new Object[] {"A", "B", "C", "D", "E"},
                                             new Double[] {1., 2., 3., 4., 5.},
                                             new Object[] {"A", "B", "C", "D", "E"},
                                             expectedValues);
    assertArrayEquals(expectedSum.getXKeys(), sum.getXKeys());
    assertArrayEquals(expectedSum.getXLabels(), sum.getXLabels());
    assertArrayEquals(expectedSum.getYKeys(), sum.getYKeys());
    assertArrayEquals(expectedSum.getYLabels(), sum.getYLabels());
    for (int i = 0; i < expectedValues.length; i++) {
      for (int j = 0; j < expectedValues[0].length; j++) {
        assertEquals(expectedValues[i][j], sum.getValues()[i][j]);
      }
    }
  }

  @Test
  public void testAddWithTolerance() {
    final Double[] otherX = new Double[] {1.2, 6.1, 7.1};
    final Double[] otherY = new Double[] {0.9, 1.8, 2.3, 6.9, 8.1};
    final Object[] otherXLabels = new Object[] {"X", "F", "G"};
    final Object[] otherYLabels = new Object[] {"H", "I", "J", "K", "L"};
    final double[][] values = new double[][] {new double[] {-10, 10, 100}, new double[] {-20, 20, 200}, new double[] {-30, 31, 301}, new double[] {-40, 40, 400}, new double[] {-50, 50, 500}};
    final DoubleLabelledMatrix2D originalMatrix = new DoubleLabelledMatrix2D(SORTED_X_KEYS, SORTED_X_LABELS2, SORTED_Y_KEYS, SORTED_Y_LABELS2, SORTED_VALUES);
    final DoubleLabelledMatrix2D otherMatrix = new DoubleLabelledMatrix2D(otherX, otherXLabels, otherY, otherYLabels, values);
    LabelledMatrix2D<Double, Double> sum = originalMatrix.add(otherMatrix, 0.0001, 0.0001);
    double[][] expectedValues = new double[][] {new double[] {0, -10, 0, 0, 0, 0, 10, 100},
                                                new double[] {1, 0, 2, 3, 4, 5, 0, 0},
                                                new double[] {0, -20, 0, 0, 0, 0, 20, 200},
                                                new double[] {2, 0, 4, 6, 8, 10, 0, 0},
                                                new double[] {0, -30, 0, 0, 0, 0, 31, 301},
                                                new double[] {3, 0, 6, 9, 12, 15, 0, 0},
                                                new double[] {0, -40, 0, 0, 0, 0, 40, 400},
                                                new double[] {0, -50, 0, 0, 0, 0, 50, 500}};
    LabelledMatrix2D<Double, Double> expectedSum = new DoubleLabelledMatrix2D(new Double[] {1., 1.2, 2., 3., 4., 5., 6.1, 7.1},
                                                                              new Object[] {"A", "X", "B", "C", "D", "E", "F", "G"},
                                                                              new Double[] {0.9, 1., 1.8, 2., 2.3, 3., 6.9, 8.1},
                                                                              new Object[] {"H", "A", "I", "B", "J", "C", "K", "L"},
                                                                              expectedValues);
    assertArrayEquals(expectedSum.getXKeys(), sum.getXKeys());
    assertArrayEquals(expectedSum.getXLabels(), sum.getXLabels());
    assertArrayEquals(expectedSum.getYKeys(), sum.getYKeys());
    assertArrayEquals(expectedSum.getYLabels(), sum.getYLabels());
    for (int i = 0; i < expectedValues.length; i++) {
      for (int j = 0; j < expectedValues[0].length; j++) {
        assertEquals(expectedValues[i][j], sum.getValues()[i][j]);
      }
    }
    sum = originalMatrix.add(otherMatrix, 0.4, 0.4);
    expectedValues = new double[][] {new double[] {-9, 2, 3, 4, 5, 10, 100},
                                     new double[] {-48, 4, 6, 8, 10, 51, 501},
                                     new double[] {3, 6, 9, 12, 15, 0, 0},
                                     new double[] {-40, 0, 0, 0, 0, 40, 400},
                                     new double[] {-50, 0, 0, 0, 0, 50, 500}};
    expectedSum = new DoubleLabelledMatrix2D(new Double[] {1., 2., 3., 4., 5., 6.1, 7.1},
                                             new Object[] {"A", "B", "C", "D", "E", "F", "G"},
                                             new Double[] {1., 2., 3., 6.9, 8.1},
                                             new Object[] {"A", "B", "C", "K", "L"},
                                             expectedValues);
    assertArrayEquals(expectedSum.getXKeys(), sum.getXKeys());
    assertArrayEquals(expectedSum.getXLabels(), sum.getXLabels());
    assertArrayEquals(expectedSum.getYKeys(), sum.getYKeys());
    assertArrayEquals(expectedSum.getYLabels(), sum.getYLabels());
    for (int i = 0; i < expectedValues.length; i++) {
      for (int j = 0; j < expectedValues[0].length; j++) {
        assertEquals(expectedValues[i][j], sum.getValues()[i][j]);
      }
    }
  }

  @Test
  public void testAddTitlesPreserved() {
    DoubleLabelledMatrix2D base = new DoubleLabelledMatrix2D(X_KEYS, X_LABELS1, X_TITLE, Y_KEYS, Y_LABELS1, Y_TITLE, VALUES, VALUES_TITLE);
    DoubleLabelledMatrix2D operand = new DoubleLabelledMatrix2D(X_KEYS, Y_KEYS, VALUES);
    LabelledMatrix2D<Double, Double> result = base.add(operand, 0.0001, 0.0001);
    assertEquals(X_TITLE, result.getXTitle());
    assertEquals(Y_TITLE, result.getYTitle());
    assertEquals(VALUES_TITLE, result.getValuesTitle());
  }
  
  private void testResult(final DoubleLabelledMatrix2D result, final Object[] xLabels, final Object[] yLabels) {
    final Double[] xKeysResult = result.getXKeys();
    assertArrayEquals(SORTED_X_KEYS, xKeysResult);
    final Object[] xLabelsResult = result.getXLabels();
    assertArrayEquals(xLabels, xLabelsResult);
    final Double[] yKeysResult = result.getYKeys();
    assertArrayEquals(SORTED_Y_KEYS, yKeysResult);
    final Object[] yLabelsResult = result.getYLabels();
    assertArrayEquals(yLabels, yLabelsResult);
    final double[][] valuesResult = result.getValues();
    for (int i = 0; i < yKeysResult.length; i++) {
      for (int j = 0; j < xKeysResult.length; j++) {
        assertEquals(valuesResult[i][j], SORTED_VALUES[i][j]);
      }
    }
  }

}
